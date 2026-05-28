package com.example.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.*
import com.example.ui.components.CanvasSolarMap
import com.example.viewmodel.AppViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboard(
    viewModel: AppViewModel,
    onLogout: () -> Unit,
    onRequestSystemLocation: (onLocationFetched: (Double, Double) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val latestAttendance by viewModel.latestAttendance.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()

    // Screen Tabs
    var activeSubTab by remember { mutableStateOf("Attendance") } // "Attendance" or "Site Survey"

    // Raw input states
    val allCustomers by viewModel.allCustomers.collectAsState()
    val sitesForCustomer by viewModel.sitesForSelectedCustomer.collectAsState()

    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val selectedSite by viewModel.selectedSite.collectAsState()

    val roofLength by viewModel.roofLength.collectAsState()
    val roofWidth by viewModel.roofWidth.collectAsState()
    val roofPitch by viewModel.roofPitch.collectAsState()
    val orientation by viewModel.orientation.collectAsState()
    val selectedInverter by viewModel.selectedInverter.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val surveyPhotoPath by viewModel.surveyPhotoPath.collectAsState()

    // Attendance specific coordinates states
    var latText by remember { mutableStateOf("30.2672") } // Default Austin, TX (Tesla Giga)
    var lngText by remember { mutableStateOf("-97.7431") }
    var locationLookupMode by remember { mutableStateOf("Simulated Coordinates") } // "Simulated Coordinates" or "System GPS"

    // Survey dropdown expand flags
    var dropCustomerExp by remember { mutableStateOf(false) }
    var dropSiteExp by remember { mutableStateOf(false) }
    var dropInverterExp by remember { mutableStateOf(false) }
    var dropOrientationExp by remember { mutableStateOf(false) }

    // Physical structural validation checklist states
    var isWindProofVerified by remember { mutableStateOf(false) }
    var isCorrosionResistantVerified by remember { mutableStateOf(false) }
    var isConcreteBallastVerified by remember { mutableStateOf(false) }

    // Gyroscope real-time measurement simulations
    var gyroPitchAngle by remember { mutableStateOf(18) }
    var isGyroActive by remember { mutableStateOf(false) }

    // Quick City Simulation profiles for beautiful developer testing
    val coordinateSimulationProfiles = listOf(
        Triple("Austin, TX (Tesla Giga)", 30.2672, -97.7431),
        Triple("Sunnyvale, CA (Acme)", 37.3688, -122.0363),
        Triple("San Jose, CA (Plaza Wing)", 37.3382, -121.8863),
        Triple("Perth, Australia (Active Solar)", -31.9505, 115.8605)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FIELD SURVEYOR HUB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = currentUser?.username ?: "Field Staff",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Quick stats display
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (latestAttendance?.status == "Checked In") Color(0xFFD1E7DD)
                                else Color(0xFFF8D7DA)
                            )
                            .border(
                                1.dp,
                                if (latestAttendance?.status == "Checked In") Color(0xAABBFFBB)
                                else Color(0xAAFFBBBB),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (latestAttendance?.status == "Checked In") Color(0xFF198754)
                                        else Color(0xFFDC3545)
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = latestAttendance?.status ?: "Checked Out",
                                fontSize = 11.sp,
                                color = if (latestAttendance?.status == "Checked In") Color(0xFF0F5132) else Color(0xFF842029),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Log Out",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Elegant Tab Bar
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeSubTab == "Attendance",
                    onClick = { activeSubTab = "Attendance" },
                    icon = { Icon(Icons.Default.HomeWork, contentDescription = null) },
                    label = { Text("Attendance") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )

                NavigationBarItem(
                    selected = activeSubTab == "Site Survey",
                    onClick = { activeSubTab = "Site Survey" },
                    icon = { Icon(Icons.Default.SolarPower, contentDescription = null) },
                    label = { Text("Site Details") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background // Smooth dynamic background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeSubTab) {
                "Attendance" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Current status banner
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "GEOLOCATION ATTENDANCE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = if (latestAttendance?.status == "Checked In") "Active Shift Checked-In" else "Off-Duty, Please Check In",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 15.sp
                                        )
                                        latestAttendance?.let {
                                            Text(
                                                text = "Last: ${SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault()).format(Date(it.timestamp))} @ [${"%.4f".format(it.latitude)}, ${"%.4f".format(it.longitude)}]",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Geo controller form
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "CLOCK IN CONTROLLERS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    // Location selector mode
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        listOf("System GPS", "Simulated Coordinates").forEach { m ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (locationLookupMode == m) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                                    .clickable { locationLookupMode = m },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = m,
                                                    color = if (locationLookupMode == m) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    if (locationLookupMode == "System GPS") {
                                        // Real hardware GPS trigger
                                        Button(
                                            onClick = {
                                                onRequestSystemLocation { lat, lng ->
                                                    latText = lat.toString()
                                                    lngText = lng.toString()
                                                    Toast.makeText(context, "Retrieved GPS: $lat, $lng", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.MyLocation, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("QUERY PHONE HARDWARE GPS", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                        }
                                    } else {
                                        // Coordinate preset buttons
                                        Text(
                                            text = "QUICK SIMULATION PRESETS:",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.LightGray,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            coordinateSimulationProfiles.take(2).forEach { p ->
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                                        .clickable {
                                                            latText = p.second.toString()
                                                            lngText = p.third.toString()
                                                            Toast.makeText(context, "Loaded ${p.first}", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .padding(8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = p.first.substringBefore(" ("),
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            coordinateSimulationProfiles.drop(2).forEach { p ->
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                                        .clickable {
                                                            latText = p.second.toString()
                                                            lngText = p.third.toString()
                                                            Toast.makeText(context, "Loaded ${p.first}", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .padding(8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = p.first.substringBefore(" ("),
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Display/Edit fields
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = latText,
                                            onValueChange = { latText = it },
                                            label = { Text("Latitude") },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )

                                        OutlinedTextField(
                                            value = lngText,
                                            onValueChange = { lngText = it },
                                            label = { Text("Longitude") },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // ACTION BUTTONS: Clock In / Clock Out
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val lat = latText.toDoubleOrNull() ?: 0.0
                                                val lng = lngText.toDoubleOrNull() ?: 0.0
                                                viewModel.recordAttendance(
                                                    latitude = lat,
                                                    longitude = lng,
                                                    isSimulated = locationLookupMode == "Simulated Coordinates",
                                                    status = "Checked In"
                                                )
                                                Toast.makeText(context, "Checked In Successfully!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("CHECK IN", fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                val lat = latText.toDoubleOrNull() ?: 0.0
                                                val lng = lngText.toDoubleOrNull() ?: 0.0
                                                viewModel.recordAttendance(
                                                    latitude = lat,
                                                    longitude = lng,
                                                    isSimulated = locationLookupMode == "Simulated Coordinates",
                                                    status = "Checked Out"
                                                )
                                                Toast.makeText(context, "Checked Out Successfully!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("CHECK OUT", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // My checks logs
                        item {
                            Text(
                                text = "PERSONAL LOGS HISTORY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Render user elements
                        val myUser = currentUser
                        val myLogs = allAttendance.filter { it.userId == myUser?.id }

                        if (myLogs.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No historical attendance entries yet.", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        } else {
                            items(myLogs) { log ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (log.status == "Checked In") Icons.Default.CheckCircle else Icons.Default.RemoveCircle,
                                                tint = if (log.status == "Checked In") Color(0xFF4CAF50) else Color(0xFFE53935),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(text = log.status, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp)),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${"%.4f".format(log.latitude)}, ${"%.4f".format(log.longitude)}",
                                                color = Color(0xFFFFB300),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = if (log.isSimulated) "Simulated Presets" else "Real Hardware GPS",
                                                color = Color.Gray,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Site Survey" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title header CARD
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SolarPower,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "SOLAR SITE DESIGNS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Structural Roof & Inverter Survey",
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Form controls card
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
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "SITE SURVEY SCHEMA METRICS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB300),
                                        fontFamily = FontFamily.Monospace
                                    )

                                    // 1. Selector Customer Dropdown
                                    Box {
                                        OutlinedTextField(
                                            value = selectedCustomer?.name ?: "Tap to select Customer",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Customer Client") },
                                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFFFB300)) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { dropCustomerExp = true },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            enabled = false
                                        )

                                        DropdownMenu(
                                            expanded = dropCustomerExp,
                                            onDismissRequest = { dropCustomerExp = false },
                                            modifier = Modifier.fillMaxWidth(0.85f).background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        ) {
                                            allCustomers.forEach { c ->
                                                DropdownMenuItem(
                                                    text = { Text(c.name, color = MaterialTheme.colorScheme.onSurface) },
                                                    onClick = {
                                                        viewModel.updateSelectedCustomer(c)
                                                        dropCustomerExp = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // 2. Selector Site Dropdown (Associated to selected Customer)
                                    Box {
                                        OutlinedTextField(
                                            value = selectedSite?.siteName ?: "Tap to select Site",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Associated Property Site") },
                                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFFFB300)) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { if (selectedCustomer != null) dropSiteExp = true },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            enabled = false
                                        )

                                        DropdownMenu(
                                            expanded = dropSiteExp,
                                            onDismissRequest = { dropSiteExp = false },
                                            modifier = Modifier.fillMaxWidth(0.85f).background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        ) {
                                            if (sitesForCustomer.isEmpty()) {
                                                DropdownMenuItem(
                                                    text = { Text("No sites for this customer! Add one in admin.", color = Color.Gray) },
                                                    onClick = { dropSiteExp = false }
                                                )
                                            } else {
                                                sitesForCustomer.forEach { s ->
                                                    DropdownMenuItem(
                                                        text = { Text(s.siteName, color = MaterialTheme.colorScheme.onSurface) },
                                                        onClick = {
                                                            viewModel.updateSelectedSite(s)
                                                            dropSiteExp = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Display structural location / site address if selected
                                    selectedSite?.let {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(10.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(text = "Survey Location: ${it.address}", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    // 3. Roof Dimensions (Side by Side length/width)
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = roofLength,
                                            onValueChange = { viewModel.updateRoofDimensions(it, roofWidth) },
                                            label = { Text("Roof Length (m)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )

                                        OutlinedTextField(
                                            value = roofWidth,
                                            onValueChange = { viewModel.updateRoofDimensions(roofLength, it) },
                                            label = { Text("Roof Width (m)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                    }

                                    // 4. Roof Pitch (Degrees) and Orientation (Dropdown)
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = roofPitch,
                                            onValueChange = { viewModel.updateRoofPitch(it) },
                                            label = { Text("Pitch Angle (°)") },
                                            placeholder = { Text("e.g. 15") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )

                                        Box(modifier = Modifier.weight(1f)) {
                                            OutlinedTextField(
                                                value = orientation,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Orientation Face") },
                                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFFFB300)) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { dropOrientationExp = true },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                enabled = false
                                            )

                                            DropdownMenu(
                                                expanded = dropOrientationExp,
                                                onDismissRequest = { dropOrientationExp = false },
                                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                            ) {
                                                listOf("South", "North", "East", "West", "South-East", "South-West").forEach { d ->
                                                    DropdownMenuItem(
                                                        text = { Text(d, color = MaterialTheme.colorScheme.onSurface) },
                                                        onClick = {
                                                            viewModel.updateOrientation(d)
                                                            dropOrientationExp = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Real-Time Gyroscope Sensor simulation
                                    if (isGyroActive) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text("LIVE HARDWARE INCLINOMETER", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                                                        Text("Slope: $gyroPitchAngle° (For 175 km/h structures)", fontSize = 11.sp, color = Color.Gray)
                                                    }
                                                }
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(onClick = { 
                                                        gyroPitchAngle = (gyroPitchAngle - 1).coerceAtLeast(0) 
                                                        viewModel.updateRoofPitch(gyroPitchAngle.toString())
                                                    }) {
                                                        Text("-", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                    }
                                                    IconButton(onClick = { 
                                                        gyroPitchAngle = (gyroPitchAngle + 1).coerceAtMost(90)
                                                        viewModel.updateRoofPitch(gyroPitchAngle.toString())
                                                    }) {
                                                        Text("+", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                    }
                                                    Button(
                                                        onClick = { 
                                                            isGyroActive = false 
                                                            Toast.makeText(context, "Pitch calibrated to $gyroPitchAngle°", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text("Lock", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = { 
                                                isGyroActive = true
                                                viewModel.updateRoofPitch(gyroPitchAngle.toString())
                                                Toast.makeText(context, "Calibrating built-in gyroscopic sensor...", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer, 
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("READ FROM PHONE GYRO / INCLINOMETER", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // 5. Inverter Choice Selector
                                    Box {
                                        OutlinedTextField(
                                            value = selectedInverter,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Select Inverter Model") },
                                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFFFB300)) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { dropInverterExp = true },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledBorderColor = MaterialTheme.colorScheme.outlineVariant, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                            enabled = false
                                        )

                                        DropdownMenu(
                                            expanded = dropInverterExp,
                                            onDismissRequest = { dropInverterExp = false },
                                            modifier = Modifier.fillMaxWidth(0.85f).background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        ) {
                                            listOf(
                                                "Fronius Primo 5.0 (Single-Phase)",
                                                "Growatt MIN 5000TL-X",
                                                "SMA Sunny Boy 3.5-US",
                                                "Enphase IQ8+ Microinverters Array",
                                                "SolarEdge SE7600H-US"
                                            ).forEach { inv ->
                                                DropdownMenuItem(
                                                    text = { Text(inv, color = MaterialTheme.colorScheme.onSurface) },
                                                    onClick = {
                                                        viewModel.updateSelectedInverter(inv)
                                                        dropInverterExp = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // AI VOICE COPILOT MULTI-TRANSCRIPT AUTOCOMPLETE
                                    var showVoiceDialog by remember { mutableStateOf(false) }
                                    val isVoiceParsing by viewModel.isVoiceParsing.collectAsState()
                                    var transcriptText by remember { mutableStateOf("") }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                              ) {
                                                Icon(
                                                    imageVector = Icons.Default.SettingsVoice,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = "AI SPEECH COPILOT",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = "Voice-to-field checklist autocomplete",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Button(
                                                onClick = { 
                                                    transcriptText = ""
                                                    showVoiceDialog = true 
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Talk", fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    if (showVoiceDialog) {
                                        AlertDialog(
                                            onDismissRequest = { showVoiceDialog = false },
                                            containerColor = Color(0xFF151218),
                                            title = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.SettingsVoice, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(24.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("AI SPEECH AUTOCAMP", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                }
                                            },
                                            text = {
                                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    Text("Choose a preset scenario below to simulate verbal checklist transcription, or type custom speech notes:", color = Color.Gray, fontSize = 12.sp)
                                                    
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        listOf(
                                                            "A: South" to "Rooftop length 12.5 meters, width 8.4, tilt pitch 22 degrees, facing South. Testing with SMA Sunny Boy inverter. All shingles are clear, no major shade obstructions.",
                                                            "B: Flat" to "Rooftop dimensions are span ten with width six meters, roof slant feels quite flat with twelve degrees, facing East. Needs Enphase microinverters. Tiled with light shade.",
                                                            "C: Steep" to "Sixteen meters length, eleven wide. Pitch angle is 35 degrees, orientation North-West. SolarEdge inverter. Shading exists from chimney structures."
                                                        ).forEach { (label, value) ->
                                                            AssistChip(
                                                                onClick = { transcriptText = value },
                                                                label = { Text(label, fontSize = 11.sp, color = Color(0xFFFFB300)) },
                                                                colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.05f))
                                                            )
                                                        }
                                                    }

                                                    OutlinedTextField(
                                                        value = transcriptText,
                                                        onValueChange = { transcriptText = it },
                                                        placeholder = { Text("Speak check... \"Rooftop length is 14 meters, width...\"", color = Color.DarkGray) },
                                                        modifier = Modifier.fillMaxWidth().height(100.dp),
                                                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                                                        maxLines = 4,
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedTextColor = Color.White,
                                                            unfocusedTextColor = Color.LightGray,
                                                            focusedBorderColor = Color(0xFFFFB300),
                                                             unfocusedBorderColor = Color.DarkGray
                                                        )
                                                    )
                                                }
                                            },
                                            confirmButton = {
                                                Button(
                                                    onClick = {
                                                        if (transcriptText.isNotBlank()) {
                                                            viewModel.parseVoiceInputs(transcriptText) { rLen, rWid, rPitch, rOrient, rInverter, rNotes ->
                                                                if (rLen != null && rWid != null) viewModel.updateRoofDimensions(rLen.toString(), rWid.toString())
                                                                if (rPitch != null) viewModel.updateRoofPitch(rPitch.toString())
                                                                if (rOrient != null) viewModel.updateOrientation(rOrient)
                                                                if (rInverter != null) viewModel.updateSelectedInverter(rInverter)
                                                                if (rNotes != null) viewModel.updateNotes(rNotes)
                                                            }
                                                            showVoiceDialog = false
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                                    enabled = !isVoiceParsing
                                                ) {
                                                    if (isVoiceParsing) {
                                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                                                    } else {
                                                         Text("Parse & Fill", color = Color.Black, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { showVoiceDialog = false }) {
                                                    Text("Cancel", color = Color.LightGray)
                                                }
                                            }
                                        )
                                    }

                                    // Notes text input
                                    OutlinedTextField(
                                        value = notes,
                                        onValueChange = { viewModel.updateNotes(it) },
                                        label = { Text("Structural Survey Notes") },
                                        placeholder = { Text("Check shade factors, roof tiles condition, mounting line structure...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 3,
                                        colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                    )
                                }
                            }
                        }

                        // Wind-Proof Structural Checklist Card
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
                                        text = "175 KM/H WIND-PROOF ADVANCED AUDIT",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E676),
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Guaranteed structural clearance parameters for Chennai monsoon cyclone proofing:",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    
                                    StaffStructuralAuditCard(
                                        checkTitle = "Corrosion-Proof Galvanized Coating",
                                        checkDescription = "Verify high zinc coating for marine salt-mist protection",
                                        isVerified = isCorrosionResistantVerified,
                                        onVerifyChange = { isCorrosionResistantVerified = it }
                                    )
                                    
                                    StaffStructuralAuditCard(
                                        checkTitle = "175 km/h Wind Load Rating",
                                        checkDescription = "Aluminum clamping setups rated for cyclone wind-bursts",
                                        isVerified = isWindProofVerified,
                                        onVerifyChange = { isWindProofVerified = it }
                                    )
                                    
                                    StaffStructuralAuditCard(
                                        checkTitle = "Concrete Ballast Foundation Rails",
                                        checkDescription = "Loads are secured safely with mechanical non-penetrating grips",
                                        isVerified = isConcreteBallastVerified,
                                        onVerifyChange = { isConcreteBallastVerified = it }
                                    )
                                }
                            }
                        }

                        // Real-Time Canvas Preview
                        item {
                            val rLen = roofLength.toDoubleOrNull() ?: 0.0
                            val rWid = roofWidth.toDoubleOrNull() ?: 0.0
                            val rPitch = roofPitch.toIntOrNull() ?: 0

                            CanvasSolarMap(
                                roofLength = rLen,
                                roofWidth = rWid,
                                roofPitch = rPitch,
                                orientation = orientation
                            )
                        }

                        // Dynamic camera / technical site snapshot captures
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "SITE SURVEY SCHEMA PHOTO ATTACHMENT",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB300),
                                        modifier = Modifier.fillMaxWidth(),
                                        fontFamily = FontFamily.Monospace
                                    )

                                    if (surveyPhotoPath != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(2.dp, Color(0xFFFFB300), RoundedCornerShape(8.dp))
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(File(surveyPhotoPath!!)),
                                                contentDescription = "Site Survey Photo",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .background(Color.Black.copy(alpha = 0.6f))
                                                    .padding(6.dp)
                                            ) {
                                                Text("Blueprint Saved!", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                                Text("No Tech Snapshot Captured", color = Color.Gray, fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    // Generator action (Captures beautiful technical overlay drawing blueprint!)
                                    Button(
                                        onClick = {
                                            // Capture vector overlay bitmap to local cache file
                                            val rLen = roofLength.toDoubleOrNull() ?: 10.0
                                            val rWid = roofWidth.toDoubleOrNull() ?: 12.0
                                            val rPitch = roofPitch.toIntOrNull() ?: 15
                                            val invModel = selectedInverter.split("(").first().trim()

                                            val file = generateMockTechnicalPhoto(
                                                context = context,
                                                siteName = selectedSite?.siteName ?: "Unspecified Site",
                                                roofLength = rLen,
                                                roofWidth = rWid,
                                                roofPitch = rPitch,
                                                orientation = orientation,
                                                inverter = invModel
                                            )
                                            if (file != null) {
                                                viewModel.updatePhotoPath(file.absolutePath)
                                                Toast.makeText(context, "Tech Photo Built & Saved locally!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Error saving snapshot file.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300).copy(alpha = 0.2f), contentColor = Color(0xFFFFB300)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("BUILT-IN MOCK CAMERA OVERLAY", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Submit bar
                        item {
                            Button(
                                onClick = {
                                    viewModel.submitSurvey { success ->
                                        if (success) {
                                            Toast.makeText(context, "Site Survey Submitted Successfully!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Fill length/width & select standard customer+site!", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300), contentColor = Color(0xFF11202A)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("submit_survey_button")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SUBMIT SURVEY TO DATA", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Generate blueprint image internally and save it to the cache directory
private fun generateMockTechnicalPhoto(
    context: android.content.Context,
    siteName: String,
    roofLength: Double,
    roofWidth: Double,
    roofPitch: Int,
    orientation: String,
    inverter: String
): File? {
    try {
        val width = 600
        val height = 400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Palette definitions
        val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#0F1C25") }
        val gridPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1F2F3B")
            strokeWidth = 2f
        }
        val blueprintLines = Paint().apply {
            color = android.graphics.Color.parseColor("#1E88E5")
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        val sunPaint = Paint().apply { color = android.graphics.Color.parseColor("#FBC02D") }
        val textPaintHeader = Paint().apply {
            color = android.graphics.Color.parseColor("#FFB300")
            textSize = 20f
            isAntiAlias = true
            typeface = android.graphics.Typeface.MONOSPACE
        }
        val textPaintBody = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 15f
            isAntiAlias = true
        }

        // Draw backdrop
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw Grid
        for (i in 0..width step 40) {
            canvas.drawLine(i.toFloat(), 0f, i.toFloat(), height.toFloat(), gridPaint)
        }
        for (j in 0..height step 40) {
            canvas.drawLine(0f, j.toFloat(), width.toFloat(), j.toFloat(), gridPaint)
        }

        // Draw isometric technical solar array panel area lines
        canvas.drawRect(80f, 100f, 520f, 320f, blueprintLines)
        // Diagonal cross line to look technical
        canvas.drawLine(80f, 100f, 520f, 320f, gridPaint)
        canvas.drawLine(520f, 100f, 80f, 320f, gridPaint)

        // Draw technical text overlays
        canvas.drawText("TECHNICAL BLUEPRINT - SURVEY LOG", 30f, 40f, textPaintHeader)
        canvas.drawText("Site: $siteName", 30f, 75f, textPaintBody)
        canvas.drawText("Dims: ${roofWidth}m x ${roofLength}m  Pitch: $roofPitch deg ($orientation)", 30f, 355f, textPaintBody)
        canvas.drawText("Inverter Target: $inverter", 30f, 380f, textPaintBody)

        // Draw mini solar illustration nodes inside array
        val cellPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0D47A1")
            style = Paint.Style.FILL
        }
        canvas.drawRect(120f, 140f, 220f, 200f, cellPaint)
        canvas.drawRect(240f, 140f, 340f, 200f, cellPaint)
        canvas.drawRect(360f, 140f, 460f, 200f, cellPaint)

        canvas.drawRect(120f, 220f, 220f, 280f, cellPaint)
        canvas.drawRect(240f, 220f, 340f, 280f, cellPaint)
        canvas.drawRect(360f, 220f, 460f, 280f, cellPaint)

        // Highlight cells borders
        val cellBorder = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRect(120f, 140f, 220f, 200f, cellBorder)
        canvas.drawRect(240f, 140f, 340f, 200f, cellBorder)
        canvas.drawRect(360f, 140f, 460f, 200f, cellBorder)
        canvas.drawRect(120f, 220f, 220f, 280f, cellBorder)
        canvas.drawRect(240f, 220f, 340f, 280f, cellBorder)
        canvas.drawRect(360f, 220f, 460f, 280f, cellBorder)

        // Save to cache dir
        val directory = context.cacheDir
        val filename = "technical_blueprint_${System.currentTimeMillis()}.png"
        val file = File(directory, filename)
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        out.flush()
        out.close()
        return file
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

@Composable
fun StaffStructuralAuditCard(
    checkTitle: String,
    checkDescription: String,
    isVerified: Boolean,
    onVerifyChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1F2C2D35), RoundedCornerShape(16.dp))
            .border(1.dp, if(isVerified) Color(0x3300E676) else Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = checkTitle, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = checkDescription, color = Color.Gray, fontSize = 11.sp)
        }
        // Tactile switch built for rapid field use
        Switch(
            checked = isVerified,
            onCheckedChange = onVerifyChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00E676),
                checkedTrackColor = Color(0x3300E676),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0x1AFFFFFF)
            )
        )
    }
}

