package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.*
import com.example.viewmodel.AppViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    viewModel: AppViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    // Screen Subtabs
    var activeAdminTab by remember { mutableStateOf("Overview") } // "Overview", "Staff Logs", "Completed Surveys", "Manage Schema"

    // Raw datasets
    val users by viewModel.allUsers.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val sites by viewModel.allSites.collectAsState()
    val surveys by viewModel.allSurveys.collectAsState()
    val attendance by viewModel.allAttendance.collectAsState()

    // Forms to add Customers/Sites
    var newCustName by remember { mutableStateOf("") }
    var newCustPhone by remember { mutableStateOf("") }
    var newCustEmail by remember { mutableStateOf("") }
    var newCustCompany by remember { mutableStateOf("") }
    var newCustAddress by remember { mutableStateOf("") }

    var newSiteName by remember { mutableStateOf("") }
    var newSiteAddress by remember { mutableStateOf("") }
    var selectedCustForSite by remember { mutableStateOf<Customer?>(null) }
    var dropCustExp by remember { mutableStateOf(false) }

    // Direct Administration Corrective states
    var editingLogId by remember { mutableStateOf<Int?>(null) }
    var correctedAttendanceStatus by remember { mutableStateOf("Checked In") }
    var correctedAttendanceSimulated by remember { mutableStateOf(false) }

    var editingSurveyId by remember { mutableStateOf<Int?>(null) }
    var correctedLength by remember { mutableStateOf("") }
    var correctedWidth by remember { mutableStateOf("") }
    var correctedPitch by remember { mutableStateOf("") }

    var editingCustomerId by remember { mutableStateOf<Int?>(null) }
    var correctedPhone by remember { mutableStateOf("") }
    var correctedEmail by remember { mutableStateOf("") }
    var correctedCompany by remember { mutableStateOf("") }
    var correctedAddress by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ADMIN COMMAND CENTER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = currentUser?.username ?: "Administrator",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Admin bottom tabs
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                windowInsets = WindowInsets.navigationBars
            ) {
                listOf(
                    Triple("Overview", Icons.Default.Assessment, "Overview"),
                    Triple("Staff Logs", Icons.Default.Badge, "Staff Logs"),
                    Triple("Surveys", Icons.Default.SolarPower, "Surveys"),
                    Triple("Add Data", Icons.Default.AddBusiness, "Add Data"),
                    Triple("AI Alert Hub", Icons.Default.Warning, "AI Alerts")
                ).forEach { tab ->
                    NavigationBarItem(
                        selected = activeAdminTab == tab.first,
                        onClick = { activeAdminTab = tab.first },
                        icon = { Icon(tab.second, contentDescription = null) },
                        label = { Text(tab.third, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeAdminTab) {
                "Overview" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "SYSTEM TOTALS METRICS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Statistics Cards Grid
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    title = "Field Engineers",
                                    value = users.count { it.role == "Field Staff" }.toString(),
                                    icon = Icons.Default.Engineering,
                                    color = Color(0xFF64B5F6),
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Target Clients",
                                    value = customers.size.toString(),
                                    icon = Icons.Default.People,
                                    color = Color(0xFF81C784),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    title = "Design Sites",
                                    value = sites.size.toString(),
                                    icon = Icons.Default.HomeWork,
                                    color = Color(0xFFFFD54F),
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Surveys Done",
                                    value = surveys.size.toString(),
                                    icon = Icons.Default.Assessment,
                                    color = Color(0xFFE57373),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            AdminTelemetryChart(
                                surveysCount = surveys.size,
                                sitesCount = sites.size
                            )
                        }

                        item {
                            AdminSubsidyTelemetryCard(
                                pendingApprovals = surveys.count { it.technicalRiskFlagged == true || it.cleanDashboardMessage == null },
                                disbursedSubsidiesAmount = (surveys.count { it.technicalRiskFlagged == false } * 78000L).coerceAtLeast(156000L)
                            )
                        }

                        item {
                            BiMonthlyRevenueProjectionMatrix()
                        }

                        // Recent active check-ins list header
                        item {
                            Text(
                                text = "RECENT STAFF ATTENDANCE TRACKER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (attendance.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No check-in operations recorded in schema.", color = Color.LightGray)
                                }
                            }
                        } else {
                            // Display top 4 recent check-ins
                            items(attendance.take(4)) { att ->
                                Card(
                                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                     border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = att.userName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(
                                                text = SimpleDateFormat("dd/MM/yyyy • hh:mm a", Locale.getDefault()).format(Date(att.timestamp)),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (att.status == "Checked In") Color(0xFF2E7D32).copy(alpha = 0.15f)
                                                        else Color(0xFFC62828).copy(alpha = 0.15f)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = att.status,
                                                    fontSize = 11.sp,
                                                    color = if (att.status == "Checked In") Color(0xFF81C784) else Color(0xFFE57373),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = "[${"%.3f".format(att.latitude)}, ${"%.3f".format(att.longitude)}]",
                                                color = Color(0xFFFFB300),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Staff Logs" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "COMPLETE GEOLOCATION HISTORY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (attendance.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No staff checkout or checkins logs recorded.", color = Color.Gray)
                                }
                            }
                        } else {
                            items(attendance) { log ->
                                val isEditing = editingLogId == log.id
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, if (isEditing) Color(0xFFFF8F00) else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(text = log.userName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(
                                                            if (log.status == "Checked In") Color(0xFF2E7D32)
                                                            else Color(0xFFC62828)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(text = log.status.uppercase(), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                }

                                                if (!isEditing) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "CORRECT",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFFF8F00),
                                                        modifier = Modifier
                                                            .clickable {
                                                                editingLogId = log.id
                                                                correctedAttendanceStatus = log.status
                                                                correctedAttendanceSimulated = log.isSimulated
                                                            }
                                                            .border(1.dp, Color(0xFFFF8F00), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (isEditing) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFFFFDE7))
                                                    .padding(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "CORRECT ATTENDANCE RECORD ERROR",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFD84315)
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    listOf("Checked In", "Checked Out").forEach { stat ->
                                                        val sel = correctedAttendanceStatus == stat
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(if (sel) Color(0xFFFFD54F) else Color.White)
                                                                .clickable { correctedAttendanceStatus = stat }
                                                                .padding(vertical = 6.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(text = stat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = correctedAttendanceSimulated,
                                                        onCheckedChange = { correctedAttendanceSimulated = it }
                                                    )
                                                    Text(text = "Overwrite as Simulated GPS", fontSize = 12.sp)
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    TextButton(onClick = { editingLogId = null }) {
                                                        Text("CANCEL", color = Color.Gray, fontSize = 12.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Button(
                                                        onClick = {
                                                            viewModel.adminFixAttendance(log.id, correctedAttendanceStatus, correctedAttendanceSimulated)
                                                            editingLogId = null
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00)),
                                                        contentPadding = PaddingValues(horizontal = 10.dp)
                                                    ) {
                                                        Text("OVERWRITE RECORD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "GPS: [${log.latitude}, ${log.longitude}]",
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = if (log.isSimulated) "Simulated Presets" else "Device Hardware",
                                                    color = Color.Gray,
                                                    fontSize = 11.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = SimpleDateFormat("EEEE, dd MMM yyyy • hh:mm a", Locale.getDefault()).format(Date(log.timestamp)),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Surveys" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "ENGINEER COMPLETED FIELD SURVEYS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (surveys.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No site surveys submitted yet.", color = Color.Gray)
                                }
                            }
                        } else {
                            items(surveys) { s ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(text = s.siteName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text(text = "Client: ${s.customerName}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                            }

                                            // Calculated system potential indicator
                                            val estPower = ((s.roofLength * s.roofWidth / 1.7) * 410) / 1000.0
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(6.dp)
                                            ) {
                                                Text(
                                                    text = "${"%.2f".format(estPower)} kWp",
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 12.dp))

                                        // Specs grid
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column {
                                                Text("ROOF DIMENSIONS", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Text("${s.roofWidth}m x ${s.roofLength}m", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Column {
                                                Text("TILT PITCH", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Text("${s.roofPitch}° (${s.orientation})", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Column {
                                                Text("SELECTED INVERTER", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Text(s.inverterModel.split("(").first(), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, maxLines = 1, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        if (!s.notes.isBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .padding(8.dp)
                                            ) {
                                                Text(text = "Notes: ${s.notes}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                            }
                                        }

                                        // Display saved snapshot blueprint
                                        s.photoPath?.let { path ->
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(140.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                            ) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(File(path)),
                                                    contentDescription = "Saved technical blueprint",
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Engineer: ${s.engineerName}",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )

                                            val isEditingSurvey = editingSurveyId == s.id
                                            if (!isEditingSurvey) {
                                                Text(
                                                    text = "CORRECT TYPO",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFFF8F00),
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .clickable {
                                                            editingSurveyId = s.id
                                                            correctedLength = s.roofLength.toString()
                                                            correctedWidth = s.roofWidth.toString()
                                                            correctedPitch = s.roofPitch.toString()
                                                        }
                                                        .border(1.dp, Color(0xFFFF8F00), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Text(
                                                text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(s.createdAt)),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }

                                        // Inline Survey Correction Box
                                        if (editingSurveyId == s.id) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFFFFDE7))
                                                    .padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "CORRECT SURVEYOR RECORD TYPOS",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFD84315)
                                                )

                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedTextField(
                                                        value = correctedLength,
                                                        onValueChange = { correctedLength = it },
                                                        label = { Text("Length (m)") },
                                                        modifier = Modifier.weight(1f),
                                                        singleLine = true
                                                    )
                                                    OutlinedTextField(
                                                        value = correctedWidth,
                                                        onValueChange = { correctedWidth = it },
                                                        label = { Text("Width (m)") },
                                                        modifier = Modifier.weight(1f),
                                                        singleLine = true
                                                    )
                                                }

                                                OutlinedTextField(
                                                    value = correctedPitch,
                                                    onValueChange = { correctedPitch = it },
                                                    label = { Text("Roof Tilt Pitch (deg)") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    singleLine = true
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    TextButton(onClick = { editingSurveyId = null }) {
                                                        Text("CANCEL", color = Color.Gray, fontSize = 12.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Button(
                                                        onClick = {
                                                            val lengthVal = correctedLength.toDoubleOrNull() ?: s.roofLength
                                                            val widthVal = correctedWidth.toDoubleOrNull() ?: s.roofWidth
                                                            val pitchVal = correctedPitch.toIntOrNull() ?: s.roofPitch

                                                            viewModel.adminFixSurvey(
                                                                s.copy(
                                                                    roofLength = lengthVal,
                                                                    roofWidth = widthVal,
                                                                    roofPitch = pitchVal
                                                                )
                                                            )
                                                            editingSurveyId = null
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00)),
                                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                                    ) {
                                                        Text("SAVE SPEC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Add Data" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title
                        item {
                            Text(
                                text = "EXPAND DATABASE SCHEMAS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Form 1: Add Customer
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "REGISTER NEW CLIENT CUSTOMER",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    OutlinedTextField(
                                        value = newCustName,
                                        onValueChange = { newCustName = it },
                                        label = { Text("Customer Name") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = newCustPhone,
                                            onValueChange = { newCustPhone = it },
                                            label = { Text("Phone") },
                                            modifier = Modifier.weight(1.2f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )

                                        OutlinedTextField(
                                            value = newCustCompany,
                                            onValueChange = { newCustCompany = it },
                                            label = { Text("Company") },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                    }

                                    OutlinedTextField(
                                        value = newCustEmail,
                                        onValueChange = { newCustEmail = it },
                                        label = { Text("Email Contact") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = newCustAddress,
                                        onValueChange = { newCustAddress = it },
                                        label = { Text("Head Office / Billing Address") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            viewModel.addCustomer(
                                                name = newCustName,
                                                phone = newCustPhone,
                                                email = newCustEmail,
                                                company = newCustCompany,
                                                address = newCustAddress
                                            )
                                            // Reset customer fields
                                            newCustName = ""
                                            newCustPhone = ""
                                            newCustEmail = ""
                                            newCustCompany = ""
                                            newCustAddress = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SAVE CUSTOMER RECORD", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Corrective client registry amendment list
                        item {
                            Text(
                                text = "EXISTING CLIENT DIRECTORY & DATA AMENDMENT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }

                        if (customers.isEmpty()) {
                            item {
                                Text("No registered clients yet.", color = Color.Gray, fontSize = 12.sp)
                            }
                        } else {
                            items(customers) { cust ->
                                val isEditingCust = editingCustomerId == cust.id
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cust.name,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF3E2723),
                                                fontSize = 14.sp
                                            )

                                            if (!isEditingCust) {
                                                Text(
                                                    text = "CORRECT DATA",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFF8F00),
                                                    modifier = Modifier
                                                        .clickable {
                                                            editingCustomerId = cust.id
                                                            correctedPhone = cust.phoneNumber
                                                            correctedEmail = cust.email
                                                            correctedCompany = cust.company
                                                            correctedAddress = cust.address
                                                        }
                                                        .border(1.dp, Color(0xFFFF8F00), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        if (isEditingCust) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                    value = correctedCompany,
                                                    onValueChange = { correctedCompany = it },
                                                    label = { Text("Company Name") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = correctedPhone,
                                                    onValueChange = { correctedPhone = it },
                                                    label = { Text("Phone Number") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = correctedEmail,
                                                    onValueChange = { correctedEmail = it },
                                                    label = { Text("Email Contact") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = correctedAddress,
                                                    onValueChange = { correctedAddress = it },
                                                    label = { Text("Address Details") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    TextButton(onClick = { editingCustomerId = null }) {
                                                        Text("CANCEL", color = Color.Gray, fontSize = 12.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Button(
                                                        onClick = {
                                                            viewModel.adminFixCustomer(
                                                                cust.copy(
                                                                    company = correctedCompany,
                                                                    phoneNumber = correctedPhone,
                                                                    email = correctedEmail,
                                                                    address = correctedAddress
                                                                )
                                                            )
                                                            editingCustomerId = null
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00))
                                                    ) {
                                                        Text("SAVE AMENDMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = "${cust.company} • ${cust.phoneNumber}",
                                                fontSize = 12.sp,
                                                color = Color.DarkGray
                                            )
                                            Text(
                                                text = "Email: ${cust.email}\nAddress: ${cust.address}",
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Form 2: Add Site Site
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "ADD HIGH-VALUE POWER STATION SITE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace
                                    )

                                    // Customer choice selection
                                    Box {
                                        OutlinedTextField(
                                            value = selectedCustForSite?.name ?: "Tap to select Customer Relation",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Customer Parent Client") },
                                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { dropCustExp = true },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            enabled = false
                                        )

                                        DropdownMenu(
                                            expanded = dropCustExp,
                                            onDismissRequest = { dropCustExp = false },
                                            modifier = Modifier.fillMaxWidth(0.8f).background(MaterialTheme.colorScheme.surfaceContainer)
                                        ) {
                                            customers.forEach { cust ->
                                                DropdownMenuItem(
                                                    text = { Text(cust.name, color = MaterialTheme.colorScheme.onSurface) },
                                                    onClick = {
                                                        selectedCustForSite = cust
                                                        dropCustExp = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = newSiteName,
                                        onValueChange = { newSiteName = it },
                                        label = { Text("Site / Project Name") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = newSiteAddress,
                                        onValueChange = { newSiteAddress = it },
                                        label = { Text("Physical Site GPS Address") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                        )

                                    Button(
                                        onClick = {
                                            val parentCust = selectedCustForSite
                                            if (parentCust != null && !newSiteName.isBlank()) {
                                                viewModel.addSite(
                                                    customerId = parentCust.id,
                                                    siteName = newSiteName,
                                                    address = newSiteAddress
                                                )
                                                newSiteName = ""
                                                newSiteAddress = ""
                                                selectedCustForSite = null
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SAVE TARGET PROJECT SITE", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                "AI Alert Hub" -> {
                    AdminAiAlertHub(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AdminTelemetryChart(surveysCount: Int, sitesCount: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SURVEY PERFORMANCE TELEMETRY",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFFFCA28),
                letterSpacing = 1.sp
            )
            Text(
                text = "Target Realization Trend",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Progress Gauge
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progressRatio = if (sitesCount > 0) surveysCount.toFloat() / sitesCount.toFloat() else 0.72f
                    val animatedProgress by animateFloatAsState(
                        targetValue = minOf(1.0f, progressRatio),
                        animationSpec = tween(1200, easing = FastOutSlowInEasing),
                        label = "gaugeProgress"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Background track
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            style = Stroke(width = 6.dp.toPx())
                        )
                        // Foreground Track
                        drawArc(
                            color = Color(0xFF00BFA5),
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00BFA5)
                        )
                        Text(
                            text = "DONE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Spline Line Chart representational grid
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 8.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Grid dividing horizontal lines
                    for (i in 0..3) {
                        val rowY = h * (i / 3f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, rowY),
                            end = Offset(w, rowY),
                            strokeWidth = 1f
                        )
                    }

                    // Bezier spline coordinates representing week audits/surveys peaks
                    val points = listOf(
                        Offset(0f, h * 0.82f),
                        Offset(w * 0.2f, h * 0.5f),
                        Offset(w * 0.4f, h * 0.72f),
                        Offset(w * 0.6f, h * 0.25f),
                        Offset(w * 0.8f, h * 0.44f),
                        Offset(w, h * 0.12f)
                    )

                    val linePath = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (idx in 1 until points.size) {
                            val prev = points[idx - 1]
                            val curr = points[idx]
                            cubicTo(
                                prev.x + (curr.x - prev.x) / 2f, prev.y,
                                prev.x + (curr.x - prev.x) / 2f, curr.y,
                                curr.x, curr.y
                            )
                        }
                    }

                    // Stroke line
                    drawPath(
                        path = linePath,
                        color = Color(0xFFFFB300),
                        style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )

                    // Node circles
                    points.forEach { pt ->
                        drawCircle(
                            color = Color(0xFFFFCA28),
                            radius = 3.5f.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color(0xFF0F0E13),
                            radius = 1.5f.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSubsidyTelemetryCard(
    pendingApprovals: Int,
    disbursedSubsidiesAmount: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSyncing by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16151B)),
        border = BorderStroke(1.dp, Color(0x3300E5FF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TNEB SUBSIDY LEDGER", 
                        color = Color(0xFF00E5FF), 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Bold, 
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${"%,d".format(disbursedSubsidiesAmount)} Disbursed", 
                        color = Color.White, 
                        fontSize = 22.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
                // High-visibility alert badge for pending government paperwork
                Surface(
                    shape = RoundedCornerShape(8.dp), 
                    color = Color(0x26FFFFB300),
                    border = BorderStroke(1.dp, Color(0x33FFFFB300))
                ) {
                    Text(
                        text = "$pendingApprovals Pending TNEB Checks",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = Color(0xFFFFB300),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Quick-action TNEB approval shortcut row
            Button(
                onClick = {
                    isSyncing = true
                    // Simulate triggering bulk sync
                    android.widget.Toast.makeText(context, "Initiating Bulk TNEB TANGEDCO Submission...", android.widget.Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F12141C)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isSyncing) "SYNCHRONIZING WITH TANGEDCO MASTER DB..." else "TRIGGER BULK TNEB REGISTRATION SYNC", 
                    color = Color(0xFF00E5FF), 
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun BiMonthlyRevenueProjectionMatrix(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0E13)),
        border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.25f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "BI-MONTHLY UTILITY REVENUES & TARIFF BRACKETS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFFFB300),
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Macro Installation Ratios (Chennai Bills)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Render 3 bars for each group: Under ₹2,000 | ₹2,000 - ₹5,000 | Above ₹5,000
            val brackets = listOf(
                Triple("Domestic under ₹2,000", 35, Color(0xFF81C784)),
                Triple("TNEB Tariff bracket ₹2k-₹5k", 95, Color(0xFFFFB300)),
                Triple("Commercial above ₹5,000", 175, Color(0xFF00E5FF))
            )

            brackets.forEach { (label, count, barColor) ->
                Column(modifier = Modifier.padding(vertical = 5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = label, color = Color.LightGray, fontSize = 12.sp)
                        Text(text = "$count Active Installations", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = count / 200f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

    }
}

@Composable
fun AdminAiAlertHub(viewModel: AppViewModel) {
    val appointments by viewModel.allAppointments.collectAsState()
    val attendanceList by viewModel.allAttendance.collectAsState()
    val surveys by viewModel.allSurveys.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Set up infinite transition for high-contrast neon flashing effect!
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Compute dynamic anomalies
    val anomalies = remember(appointments, attendanceList, surveys) {
        val list = mutableListOf<AdminAnomaly>()
        
        // 1. Check for Simulated Coordinates
        attendanceList.forEach { log ->
            if (log.isSimulated && log.status == "Checked In") {
                list.add(
                    AdminAnomaly(
                        id = "sim_${log.id}",
                        type = "FIELD COMPLIANCE WARNING",
                        title = "Simulated GPS Coordinate Override",
                        details = "Surveyor '${log.userName}' checked in with simulated coordinates [Lat: ${log.latitude}, Lng: ${log.longitude}]. Compliance threshold warning.",
                        severity = "HIGH",
                        actionLabel = "DECLARE COMPLIANT MATCH",
                        onResolve = {
                            viewModel.adminFixAttendance(log.id, log.status, false)
                        }
                    )
                )
            }
        }

        // 2. Out-of-bounds roof specifications in site surveys
        surveys.forEach { survey ->
            val pitch = survey.roofPitch
            if (pitch > 40 || pitch < 10) {
                list.add(
                    AdminAnomaly(
                        id = "survey_${survey.id}",
                        type = "STRUCTURAL PARAMETER ANOMALY",
                        title = "Abnormal Pitch Angulation",
                        details = "Customer ${survey.customerName}'s survey log shows a slope pitch of ${pitch}° (standard residential is 15°-35°). Review structural integrity.",
                        severity = "MEDIUM",
                        actionLabel = "FORCE NOMINAL 22° PITCH",
                        onResolve = {
                            viewModel.adminFixSurvey(survey.copy(roofPitch = 22))
                        }
                    )
                )
            }
        }

        // AI-Flagged Structural Risks from surveys
        surveys.forEach { survey ->
            if (survey.technicalRiskFlagged == true) {
                list.add(
                    AdminAnomaly(
                        id = "risk_${survey.id}",
                        type = "AI STRUCTURAL RISK WARNING",
                        title = "Critical Roof Degradation Detected",
                        details = "Google AI Studio's structured output parser flagged critical degradation risk for customer '${survey.customerName}'. Notes: \"${survey.notes}\".",
                        severity = "CRITICAL",
                        actionLabel = "OVERRIDE & CLEAR RISK",
                        onResolve = {
                            viewModel.adminFixSurvey(survey.copy(technicalRiskFlagged = false))
                        }
                    )
                )
            }
        }

        // 3. Delinquent appointments pending matching for more than 5 days
        appointments.forEach { appt ->
            if (appt.status == "Pending") {
                list.add(
                    AdminAnomaly(
                        id = "appt_${appt.id}",
                        type = "DELINQUENT COORDINATION DELAY",
                        title = "Awaiting Regional Staff Dispatch",
                        details = "Appointment booked for customer '${appt.customerName}' on address '${appt.address}' remains in Pending status with no assigned engineer.",
                        severity = "CRITICAL",
                        actionLabel = "ASSIGN PRIMARY DAVE",
                        onResolve = {
                            viewModel.coordinateAssignment(appt.id, "Dave", "Confirmed")
                        }
                    )
                )
            }
        }

        list
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131118)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF3D00).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "🛰️ COGNITIVE EXCEPTION ALERTS HUB",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFF5252)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Automated sensor-scan monitoring for compliance warnings, abnormal site dimensions, database discrepancies, and staffing delays.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            }
        }

        item {
            Text(
                text = "ACTIVE CRITICAL ANOMALIES (${anomalies.size})",
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        if (anomalies.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Operational Status Nominal", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(anomalies) { anomaly ->
                val severityColor = when (anomaly.severity) {
                    "CRITICAL" -> Color(0xFFFF1744)
                    "HIGH" -> Color(0xFFFF9100)
                    else -> Color(0xFFFFD600)
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1921)),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.5.dp,
                        color = severityColor.copy(alpha = pulseAlpha)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(severityColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = anomaly.type,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = severityColor,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(severityColor.copy(alpha = pulseAlpha))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "LIVE EXCEPTION",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = severityColor.copy(alpha = pulseAlpha)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = anomaly.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = anomaly.details,
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                anomaly.onResolve()
                                android.widget.Toast.makeText(context, "Anomalies correction updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = severityColor),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = anomaly.actionLabel,
                                color = if (anomaly.severity == "MEDIUM") Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

data class AdminAnomaly(
    val id: String,
    val type: String,
    val title: String,
    val details: String,
    val severity: String,
    val actionLabel: String,
    val onResolve: () -> Unit
)
