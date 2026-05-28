package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteLeaderDashboard(
    viewModel: AppViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allStaff by viewModel.allUsers.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val appointments by viewModel.allAppointments.collectAsState()
    val attendanceLogs by viewModel.allAttendance.collectAsState()

    var activeTab by remember { mutableStateOf("Coordination") } // "Coordination", "Staff Tracker", "Customer Directory"

    // Coordinating select helper state
    var selectedAppointmentToAssign by remember { mutableStateOf<Appointment?>(null) }
    var selectedStaffForDispatch by remember { mutableStateOf("") }
    var showCoordinationDialog by remember { mutableStateOf(false) }

    val leadershipBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFDF0),
            Color(0xFFFFFCE0),
            Color(0xFFFFECB3)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SOLAR RUN • LEADER COMMAND",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD84315),
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = currentUser?.username ?: "Site Leader Coordinator",
                            fontSize = 15.sp,
                            color = Color(0xFF3E2723),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Log Out",
                            tint = Color(0xFF3E2723)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeTab == "Coordination",
                    onClick = { activeTab = "Coordination" },
                    icon = { Icon(Icons.Default.Engineering, contentDescription = null) },
                    label = { Text("Coordination") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF5D4037),
                        selectedTextColor = Color(0xFFD84315),
                        indicatorColor = Color(0xFFFFCC80)
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "Staff Tracker",
                    onClick = { activeTab = "Staff Tracker" },
                    icon = { Icon(Icons.Default.AssignmentInd, contentDescription = null) },
                    label = { Text("Staff Logs") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF5D4037),
                        selectedTextColor = Color(0xFFD84315),
                        indicatorColor = Color(0xFFFFCC80)
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "Customer Directory",
                    onClick = { activeTab = "Customer Directory" },
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = { Text("Customers") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF5D4037),
                        selectedTextColor = Color(0xFFD84315),
                        indicatorColor = Color(0xFFFFCC80)
                    )
                )
            }
        },
        containerColor = Color(0xFFFFFDE7)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(leadershipBg)
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "Coordination" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header statistics overview card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCC80)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Pending Bookings", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = appointments.count { it.status == "Pending" }.toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD84315)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Confirmed Trips", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = appointments.count { it.status == "Confirmed" }.toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Active Staff", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = allStaff.count { it.role == "Staff" }.toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1565C0)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            ActiveCrewRosterBoard()
                        }

                        item {
                            RegionalProjectClusterViews()
                        }

                        item {
                            Text(
                                text = "ACTIVE 24-HOUR DEPLOYMENT TRACKS",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                SiteLeaderDeploymentTracker(
                                    teamName = "BETA TEAM (South Group)",
                                    assignedSiteName = "Velachery Residential Hub - 5kW Install",
                                    hoursRemaining = 14
                                )
                                SiteLeaderDeploymentTracker(
                                    teamName = "DELTA TEAM (Coast Group)",
                                    assignedSiteName = "Besant Nagar High-Rise Concrete Ballast",
                                    hoursRemaining = 5
                                )
                            }
                        }

                        item {
                            Text(
                                text = "ACTIVE SURVEY DESPATCH QUEUE",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Coordination Dispatch Dialog Helper
                        if (showCoordinationDialog && selectedAppointmentToAssign != null) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF9800)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "COORDINATION SCHEDULER",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFFE65100)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "Assign engineer to survey for customer '${selectedAppointmentToAssign?.customerName}' scheduled on ${selectedAppointmentToAssign?.appointmentDate} at ${selectedAppointmentToAssign?.appointmentTime}.",
                                            fontSize = 13.sp,
                                            color = Color(0xFF5D4037)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "SELECT IN-FIELD STAFF",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )

                                        // Render available field engineers in database for click selection
                                        val staffList = allStaff.filter { it.role == "Staff" }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            staffList.forEach { engr ->
                                                val sel = selectedStaffForDispatch == engr.username
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (sel) Color(0xFFFFB74D) else Color.White)
                                                        .border(
                                                            width = if (sel) 1.5.dp else 1.dp,
                                                            color = if (sel) Color(0xFFE65100) else Color.LightGray,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .clickable { selectedStaffForDispatch = engr.username }
                                                        .padding(8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = engr.username,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (sel) Color.White else Color.Black,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = {
                                                selectedAppointmentToAssign = null
                                                showCoordinationDialog = false
                                            }) {
                                                Text("CANCEL", color = Color.Gray)
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Button(
                                                onClick = {
                                                    if (selectedStaffForDispatch.isBlank()) {
                                                        Toast.makeText(context, "Select a field staff engineer.", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    viewModel.coordinateAssignment(
                                                        appointmentId = selectedAppointmentToAssign!!.id,
                                                        engineer = selectedStaffForDispatch,
                                                        status = "Confirmed"
                                                    )
                                                    Toast.makeText(context, "Survey scheduled & surveyor dispatched!", Toast.LENGTH_SHORT).show()
                                                    selectedAppointmentToAssign = null
                                                    showCoordinationDialog = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                                            ) {
                                                Text("CONFIRM DISPATCH", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Predictive Dispatch Recommendation Card
                        item {
                            val recommendation by viewModel.predictiveDispatchRecommendation.collectAsState()
                            val isLoadingRecommendation by viewModel.isPredictiveDispatchLoading.collectAsState()
                            val allAttendanceList by viewModel.allAttendance.collectAsState()

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131118)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.SettingsVoice,
                                                contentDescription = null,
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "🛰️ COGNITIVE PREDICTIVE DISPATCH",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFFFFB300)
                                              )
                                        }

                                        Button(
                                            onClick = {
                                                val pendingOnly = appointments.filter { it.status == "Pending" }
                                                val staffOnly = allAttendanceList.filter { it.status == "Checked In" }
                                                viewModel.fetchPredictiveDispatch(pendingOnly, if (staffOnly.isNotEmpty()) staffOnly else allAttendanceList)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            enabled = !isLoadingRecommendation,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            if (isLoadingRecommendation) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.Black)
                                            } else {
                                                Text("Run AI Match", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "Automate scheduling matches by grouping active telemetry locations with physical rooftop workloads to find the fastest dispatch path.",
                                        fontSize = 12.sp,
                                        color = Color.LightGray
                                    )

                                    if (recommendation != null) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                recommendation!!.split("\n").forEach { line ->
                                                    if (line.isNotBlank()) {
                                                        val cleanLine = line.replace("*", "").replace("#", "")
                                                        Text(
                                                            text = cleanLine,
                                                            fontSize = 12.sp,
                                                            color = if (line.contains("📍") || line.contains("Assignment")) Color(0xFFFFB300) else Color.White,
                                                            fontFamily = if (line.contains("Assignment")) FontFamily.Monospace else FontFamily.Default,
                                                            fontWeight = if (line.contains("Assignment") || line.contains("📍")) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Main Dispatch Queue
                        if (appointments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No solar appointment bookings found.", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        } else {
                            items(appointments) { appt ->
                                val isPending = appt.status == "Pending"
                                val isConfirmed = appt.status == "Confirmed"

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = if (isPending) Color(0xFFFF9800) else if (isConfirmed) Color(0xFF4CAF50) else Color.LightGray
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "CUSTOMER: ${appt.customerName}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFFD84315)
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isPending) Color(0xFFFFF3E0)
                                                        else if (isConfirmed) Color(0xFFE8F5E9)
                                                        else Color(0xFFECEFF1)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = appt.status.uppercase(),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isPending) Color(0xFFEF6C00) else if (isConfirmed) Color(0xFF2E7D32) else Color.DarkGray
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "Proposed Date: ${appt.appointmentDate} at ${appt.appointmentTime}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF3E2723)
                                        )

                                        Text(
                                            text = "Installation Address: ${appt.address}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )

                                        if (appt.notes.isNotEmpty()) {
                                            Text(
                                                text = "Directives: \"${appt.notes}\"",
                                                fontSize = 12.sp,
                                                color = Color(0xFF7D6B5D),
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Divider(color = Color(0xFFF5F5F5))

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Engineering,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Dispatched Staff: ${appt.assignedEngineer}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF5D4037)
                                            )

                                            if (isPending) {
                                                Spacer(modifier = Modifier.weight(1f))
                                                Button(
                                                    onClick = {
                                                        selectedAppointmentToAssign = appt
                                                        selectedStaffForDispatch = ""
                                                        showCoordinationDialog = true
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text(text = "DISPATCH SURVEY", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Staff Tracker" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "FIELD SURVEYOR ATTENDANCE LOGS",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Display list of clock in / site in / site out updates
                        if (attendanceLogs.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No staff attendance logs in database.", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        } else {
                            items(attendanceLogs) { log ->
                                val isIn = log.status == "Checked In"
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = log.userName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF3E2723)
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isIn) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = log.status.uppercase(),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isIn) Color(0xFF2E7D32) else Color(0xFFC62828)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Position: Lat ${"%.4f".format(log.latitude)}, Lng ${"%.4f".format(log.longitude)} (${if (log.isSimulated) "Simulated GPS" else "Hardware System GPS"})",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Clock Timestamp:",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Customer Directory" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "UP-TO-DATE CUSTOMER DATABASE",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (customers.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No customers registered in database.", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        } else {
                            items(customers) { cust ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Business,
                                                contentDescription = null,
                                                tint = Color(0xFFFF9800),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = cust.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = Color(0xFF3E2723)
                                                )
                                                Text(
                                                    text = "${cust.company} • ${cust.email}",
                                                    fontSize = 12.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFFFFDF7))
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = "Contact Phone: ${cust.phoneNumber}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.DarkGray
                                            )
                                            Text(
                                                text = "Address: ${cust.address}",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
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
}

@Composable
fun SiteLeaderDeploymentTracker(
    teamName: String,
    assignedSiteName: String,
    hoursRemaining: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16151B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(if (hoursRemaining <= 8) Color(0x26FF3333) else Color(0x2600E676), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${hoursRemaining}h",
                    color = if (hoursRemaining <= 8) Color(0xFFFF3333) else Color(0xFF00E676),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = teamName, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = assignedSiteName, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
            }
            Surface(shape = RoundedCornerShape(6.dp), color = Color(0x1F12141C)) {
                Text(
                    text = "24H TARGET",
                    modifier = Modifier.padding(6.dp),
                    color = Color(0xFFFFB300),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ActiveCrewRosterBoard(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0E13)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ACTIVE CREW ROSTER BOARD",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFFFB300),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Beta Team Deployments • 24-Hr SLA Tracking",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            val crews = listOf(
                Triple("BETA TEAM (South Chennai)", "Active (Tambaram site installation)", Color(0xFF00E676)),
                Triple("GAMMA TEAM (Central)", "En route (Adyar survey setup)", Color(0xFFFFB300)),
                Triple("ALPHA TEAM (North Chennai)", "Standby (Awaiting TNEB approval)", Color(0xFF00E5FF))
            )

            crews.forEach { (crewName, statusMsg, dotColor) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(dotColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = crewName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = statusMsg, color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RegionalProjectClusterViews(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "REGIONAL CHENNAI PROJECT CLUSTER",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFD84315),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            val clusters = listOf(
                Pair("Velachery & Tambaram Cluster", 12),
                Pair("Adyar & Besant Nagar Cluster", 8),
                Pair("OMR & Sholinganallur Cluster", 15),
                Pair("Anna Nagar & Ambattur Cluster", 6)
            )

            clusters.forEach { (clusterName, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFD84315),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = clusterName, color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Text(text = "$count sites", color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

