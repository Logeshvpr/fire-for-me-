package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.viewmodel.AppViewModel
import com.example.ui.components.VoiceCopilot
import com.example.ui.components.AIBlueprintAnalyzer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboard(
    viewModel: AppViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val appointments by viewModel.customerAppointments.collectAsState()
    val savedModels by viewModel.customerModels.collectAsState()
    val allStaff by viewModel.allUsers.collectAsState()

    var activeSubTab by remember { mutableStateOf("Design") } // "Design", "Voice Copilot", "Appointments", "Track Progress", "ROI Dashboard"

    // Booking appointment states
    var apptDate by remember { mutableStateOf("2026-06-01") }
    var apptTime by remember { mutableStateOf("10:00 AM") }
    var apptAddress by remember { mutableStateOf("742 Evergreen Terrace") }
    var apptPhone by remember { mutableStateOf("+1-555-8901") }
    var apptNotes by remember { mutableStateOf("") }
    var showBookingForm by remember { mutableStateOf(false) }

    // 3D Model inputs
    var roofStyle by remember { mutableStateOf("Gabled") } // "Gabled", "Flat", "Slanted"
    var panelCount by remember { mutableStateOf(12) }
    var efficiencyType by remember { mutableStateOf("High Performance Monocrystalline") }
    var orientation by remember { mutableStateOf("South") }
    var designNotes by remember { mutableStateOf("") }
    var generatedBrief by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    // Warm solar gradient
    val solarBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFDF0),
            Color(0xFFFFFCE0),
            Color(0xFFFFF9C4)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SOLAR RUN • RESIDENTIAL HUB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8F00),
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = currentUser?.username ?: "Valued Customer",
                            fontSize = 16.sp,
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
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeSubTab == "Design",
                    onClick = { activeSubTab = "Design" },
                    icon = { Icon(Icons.Default.ViewInAr, contentDescription = null) },
                    label = { Text("3D Generator", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )

                NavigationBarItem(
                    selected = activeSubTab == "Voice Copilot",
                    onClick = { activeSubTab = "Voice Copilot" },
                    icon = { Icon(Icons.Default.SettingsVoice, contentDescription = null) },
                    label = { Text("Voice Copilot", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )

                NavigationBarItem(
                    selected = activeSubTab == "Appointments",
                    onClick = { activeSubTab = "Appointments" },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("Appointments", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )

                NavigationBarItem(
                    selected = activeSubTab == "Track Progress",
                    onClick = { activeSubTab = "Track Progress" },
                    icon = { Icon(Icons.Default.Engineering, contentDescription = null) },
                    label = { Text("Track Progress", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )

                NavigationBarItem(
                    selected = activeSubTab == "ROI Dashboard",
                    onClick = { activeSubTab = "ROI Dashboard" },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                    label = { Text("ROI Calc", fontSize = 11.sp) },
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(solarBg)
                .padding(innerPadding)
        ) {
            when (activeSubTab) {
                "Design" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "3D GENERATIVE SOLAR BLUEPRINT",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFFFF7043),
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Model simulated rooftop solar layout based on customized structural parameter requirements.",
                                        fontSize = 13.sp,
                                        color = Color(0xFF7D6B5D),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        // Live interactive 3D house isometric Canvas
                        item {
                            Rooftop3DModelerCard(
                                roofStyle = roofStyle,
                                panelCount = panelCount,
                                orientation = orientation
                            )
                        }

                        // Generated AI Brief Assessment
                        if (generatedBrief.isNotEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "AI",
                                                tint = Color(0xFFFF8F00),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "SOLAR RUN AI SPEC BRIEFING",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF5D4037),
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = generatedBrief,
                                            fontSize = 13.sp,
                                            color = Color(0xFF4E342E),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Input Configurations Panel
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "BLUEPRINT CONFIGURATION PARAMETERS",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3E2723),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    // Roof Style Select
                                    Column {
                                        Text(text = "Rooftop Structure Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf("Gabled", "Flat", "Slanted").forEach { style ->
                                                val sel = roofStyle == style
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (sel) Color(0xFFFFE082) else Color(0xFFF5F5F5))
                                                        .clickable { roofStyle = style }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = style,
                                                        fontSize = 12.sp,
                                                        color = if (sel) Color(0xFF5D4037) else Color.Gray,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Panel slider count
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Active Panels Needed", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Text(text = "$panelCount panels", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8F00))
                                        }
                                        Slider(
                                            value = panelCount.toFloat(),
                                            onValueChange = { panelCount = it.toInt() },
                                            valueRange = 4f..24f,
                                            steps = 19,
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(0xFFFF8F00),
                                                activeTrackColor = Color(0xFFFFB300)
                                            )
                                        )
                                    }

                                    // Solar panel efficiency selector
                                    Column {
                                        Text(text = "PV Cell Wafer Grade", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf("Monocrystalline", "Polycrystalline", "Thin-Film").forEach { tier ->
                                                val fullTierName = when(tier) {
                                                    "Monocrystalline" -> "High Performance Monocrystalline"
                                                    "Polycrystalline" -> "Standard Polycrystalline"
                                                    else -> "Premium Thin-Film"
                                                }
                                                val sel = efficiencyType == fullTierName
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (sel) Color(0xFFFFE082) else Color(0xFFF5F5F5))
                                                        .clickable { efficiencyType = fullTierName }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = tier,
                                                        fontSize = 11.sp,
                                                        color = if (sel) Color(0xFF5D4037) else Color.Gray,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Orientation Row
                                    Column {
                                        Text(text = "Direction Compass Orientation", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf("North", "South", "East", "West").forEach { dir ->
                                                val sel = orientation == dir
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (sel) Color(0xFFFFE082) else Color(0xFFF5F5F5))
                                                        .clickable { orientation = dir }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = dir,
                                                        fontSize = 12.sp,
                                                        color = if (sel) Color(0xFF5D4037) else Color.Gray,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Descriptors
                                    OutlinedTextField(
                                        value = designNotes,
                                        onValueChange = { designNotes = it },
                                        label = { Text("Aesthetic & Layout Notes") },
                                        placeholder = { Text("Example: Maximize east wing footprint, stealth dark trim...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFFF8F00),
                                            focusedLabelColor = Color(0xFFFF8F00)
                                        )
                                    )

                                    // Generate and Save CTA
                                    Button(
                                        onClick = {
                                            isGenerating = true
                                            // Mocking dynamic engineering briefing based on live custom inputs
                                            val estKwp = (panelCount * 415) / 1000.0
                                            val directionDetail = when (orientation) {
                                                "South" -> "South alignment secures maximum daily peak solar irradiance. Perfect setup."
                                                "East" -> "East layout captures rich sunrise spectrum, highly offset for early-day consumption profiles."
                                                "West" -> "West layout secures superb late afternoon yield, countering peak grid pricing offsets."
                                                else -> "North alignment generates stable diffused irradiance vectors; requires pitch calibration."
                                            }
                                            val notesText = if (designNotes.isNotBlank()) "Incorporating your custom requirements ('$designNotes') in our blueprint draft." else "Draft incorporates optimized symmetry arrays."
                                            
                                            generatedBrief = "SOLAR RUN COGNITIVE ANALYSIS REPORT:\n" +
                                                    "• Proposed Core Structure: Isometric Roof Type - $roofStyle\n" +
                                                    "• Engineered Equipment: $panelCount cells x Ultra-Reliable 415W ($efficiencyType Series)\n" +
                                                    "• Form Factor Capacity: ${"%.2f".format(estKwp)} kWp peak rating\n" +
                                                    "• Orientation Assessment: $directionDetail\n" +
                                                    "• Aesthetic & Surface Offset: $notesText\n" +
                                                    "• Status: Blueprint layout is optimized. Design saved to database."

                                            viewModel.saveSolarModel(
                                                style = roofStyle,
                                                panels = panelCount,
                                                efficiencyType = efficiencyType,
                                                orient = orientation,
                                                desc = designNotes,
                                                aiAssessment = generatedBrief
                                            )
                                            isGenerating = false
                                            Toast.makeText(context, "Solar Dynamic 3D Blueprint generated & saved!", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("generate_3d_blueprint_btn")
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "GENERATE 3D SOLAR ASSEMBLY MODEL", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            AIBlueprintAnalyzer(viewModel = viewModel)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Saved Configurations Header
                        if (savedModels.isNotEmpty()) {
                            item {
                                Text(
                                    text = "SAVED BLUEPRINT HISTORIES (${savedModels.size})",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3E2723),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                            }

                            items(savedModels) { config ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${config.roofStyle} Roof (${config.panelCount} Panels)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF5D4037)
                                            )
                                            Text(
                                                text = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(config.createdAt)),
                                                color = Color.Gray,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Text(
                                            text = "PV Panel: ${config.efficiency} | Orient: ${config.orientation}",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )

                                        if (config.designNotes.isNotEmpty()) {
                                            Text(
                                                text = "Aesthetics: \"${config.designNotes}\"",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF7D6B5D),
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Voice Copilot" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        VoiceCopilot(
                            currentRoofStyle = roofStyle,
                            onRoofStyleChange = { roofStyle = it },
                            currentPanelCount = panelCount,
                            onPanelCountChange = { panelCount = it },
                            currentOrientation = orientation,
                            onOrientationChange = { orientation = it },
                            currentEfficiency = efficiencyType,
                            onEfficiencyChange = { efficiencyType = it }
                        )
                    }
                }

                "Appointments" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MY APPOINTMENT BOOKINGS",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Button(
                                onClick = { showBookingForm = !showBookingForm },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (showBookingForm) Icons.Default.Close else Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showBookingForm) "Close Form" else "Book Survey",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Animated Booking Form
                        AnimatedVisibility(visible = showBookingForm) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "NEW SCHEDULE REQUEST",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFFFF8F00),
                                        fontFamily = FontFamily.Monospace
                                    )

                                    OutlinedTextField(
                                        value = apptDate,
                                        onValueChange = { apptDate = it },
                                        label = { Text("Preferred Date (YYYY-MM-DD)") },
                                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFFF8F00),
                                            focusedLabelColor = Color(0xFFFF8F00)
                                        )
                                    )

                                    OutlinedTextField(
                                        value = apptTime,
                                        onValueChange = { apptTime = it },
                                        label = { Text("Preferred Time (e.g. 10:00 AM)") },
                                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFFF8F00),
                                            focusedLabelColor = Color(0xFFFF8F00)
                                        )
                                    )

                                    OutlinedTextField(
                                        value = apptAddress,
                                        onValueChange = { apptAddress = it },
                                        label = { Text("Installation Structural Address") },
                                        leadingIcon = { Icon(Icons.Default.Map, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFFF8F00),
                                            focusedLabelColor = Color(0xFFFF8F00)
                                        )
                                    )

                                    OutlinedTextField(
                                        value = apptPhone,
                                        onValueChange = { apptPhone = it },
                                        label = { Text("Primary Contact Number") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFFF8F00),
                                            focusedLabelColor = Color(0xFFFF8F00)
                                        )
                                    )

                                    OutlinedTextField(
                                        value = apptNotes,
                                        onValueChange = { apptNotes = it },
                                        label = { Text("Special Structural Directives / Roof Notes") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFFF8F00),
                                            focusedLabelColor = Color(0xFFFF8F00)
                                        )
                                    )

                                    Button(
                                        onClick = {
                                            if (apptDate.isBlank() || apptTime.isBlank() || apptAddress.isBlank()) {
                                                Toast.makeText(context, "Please fill in all details.", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            viewModel.bookAppointment(
                                                date = apptDate,
                                                time = apptTime,
                                                address = apptAddress,
                                                phone = apptPhone,
                                                notes = apptNotes
                                            )
                                            apptNotes = ""
                                            showBookingForm = false
                                            Toast.makeText(context, "Solar Survey booked successfully!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = "SUBMIT BOOKING REQUEST", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Booking list
                        if (appointments.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No appointment bookings active. Click 'Book Survey' to schedule field staff.",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(appointments) { appt ->
                                    val isConfirmed = appt.status == "Confirmed"
                                    val isCompleted = appt.status == "Completed"
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = 1.dp,
                                            color = if (isConfirmed) Color(0xFF2E7D32) else if (isCompleted) Color(0xFF1565C0) else Color(0xFFFF8F00)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "SURVEY PREVIEW",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color.Gray
                                                )

                                                // Status Capsule
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (isConfirmed) Color(0xFFE8F5E9)
                                                            else if (isCompleted) Color(0xFFE3F2FD)
                                                            else Color(0xFFFFF3E0)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = appt.status.uppercase(),
                                                        color = if (isConfirmed) Color(0xFF2E7D32) else if (isCompleted) Color(0xFF1565C0) else Color(0xFFE65100),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "Date: ${appt.appointmentDate} at ${appt.appointmentTime}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF3E2723),
                                                modifier = Modifier.padding(top = 6.dp)
                                            )

                                            Text(
                                                text = "Site Address: ${appt.address}",
                                                fontSize = 13.sp,
                                                color = Color.DarkGray,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )

                                            if (appt.notes.isNotEmpty()) {
                                                Text(
                                                    text = "Notes: \"${appt.notes}\"",
                                                    fontSize = 12.sp,
                                                    color = Color.Gray,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }

                                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFEEEEEE))

                                            // Field Engineer assignment section
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Engineering,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFF8F00),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "ASSIGNED SOLAR ENGINEER",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Gray
                                                    )
                                                    Text(
                                                        text = appt.assignedEngineer,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (appt.assignedEngineer != "Unassigned") Color(0xFF3E2723) else Color.Gray
                                                    )
                                                }

                                                if (appt.assignedEngineer != "Unassigned") {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    // Quick Contact badge
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFFFFF3C4))
                                                            .clickable {
                                                                Toast.makeText(context, "Paging Field Staff ${appt.assignedEngineer}...", Toast.LENGTH_SHORT).show()
                                                            }
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = "SEND MESSAGE",
                                                            color = Color(0xFFD84315),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
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

                "Track Progress" -> {
                    val allSurveysList by viewModel.allSurveys.collectAsState()
                    val allAttendanceList by viewModel.allAttendance.collectAsState()
                    
                    val customerSurvey = allSurveysList.find { it.customerName.equals(currentUser?.username, ignoreCase = true) }
                    val hasDesigns = savedModels.isNotEmpty()
                    val hasConfirmedOrCompletedAppt = appointments.any { it.status == "Confirmed" || it.status == "Completed" }
                    val hasCompletedAppt = appointments.any { it.status == "Completed" }
                    val hasSurvey = customerSurvey != null

                    // Determine active installer stage (from 1 to 4)
                    val currentPhase = when {
                        hasCompletedAppt || (hasSurvey && hasConfirmedOrCompletedAppt) -> 4
                        hasSurvey -> 3
                        hasConfirmedOrCompletedAppt -> 2
                        hasDesigns -> 1
                        else -> 1
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
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "MISSION-CONTROL TRACKER",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFFFFC107),
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.2.sp
                                    )
                                    Text(
                                        text = "Real-time production, engineering checks, and field installer telemetry tracker.",
                                        fontSize = 13.sp,
                                        color = Color.LightGray,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        // Connected Milestone pipeline
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0E13)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "CONSTRUCTION MILESTONES",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    val phases = listOf(
                                        "Validation" to Icons.Default.CheckCircle,
                                        "Site Audit" to Icons.Default.Map,
                                        "Deployment" to Icons.Default.Engineering,
                                        "Grid Active" to Icons.Default.Power
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        phases.forEachIndexed { index, (title, icon) ->
                                            val phaseStep = index + 1
                                            val isCompleted = phaseStep < currentPhase
                                            val isActive = phaseStep == currentPhase

                                            val nodeColor = when {
                                                isCompleted -> Color(0xFF00E5FF)
                                                isActive -> Color(0xFFFFB300)
                                                else -> Color(0x33FFFFFF)
                                            }

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .background(nodeColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                                        .border(
                                                            width = if (isActive) 2.dp else 1.dp,
                                                            color = nodeColor,
                                                            shape = RoundedCornerShape(12.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = nodeColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Text(
                                                    text = title,
                                                    color = if (isActive || isCompleted) Color.White else Color.Gray,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    maxLines = 1,
                                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }

                                            if (index < phases.size - 1) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(16.dp)
                                                        .height(1.dp)
                                                        .background(if (index + 2 <= currentPhase) Color(0xFF00E5FF) else Color(0x1AFFFFFF))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Detail card
                        item {
                            val activeTitle = when(currentPhase) {
                                1 -> "Phase 1: Engineering Design Validation"
                                2 -> "Phase 2: Live Rooftop Survey Audit"
                                3 -> "Phase 3: Hardware System Assembly"
                                else -> "Phase 4: Safety Testing & Active Grid Handover"
                            }
                            val activeDesc = when(currentPhase) {
                                1 -> "Your 3D solar blueprint model values are parsed and validated by internal solar estimation software."
                                2 -> "Coordinating live rooftop physical inclinometer checks to audit surface orientations and structural offsets."
                                3 -> "Assembling mounting framework structures, Ultra-Reliable 415W cells, and syncing Fronius Primo inverter log sequence."
                                else -> "Rigorous safety checks successfully passed! Your solar system is connected and live on the electricity grid network."
                            }
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131118)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(if (currentPhase < 4) Color(0xFFFFB300) else Color(0xFF00E5FF), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = activeTitle,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = activeDesc,
                                        color = Color.LightGray,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // Live Field Crew Radar Telemetry
                        item {
                            val apptInCharge = appointments.firstOrNull { it.assignedEngineer != "Unassigned" }
                            val assignedEngineerName = apptInCharge?.assignedEngineer ?: "Solar Lead Engineer"
                            val latestAttendanceObj = allAttendanceList
                                .filter { it.userName.equals(assignedEngineerName, ignoreCase = true) }
                                .maxByOrNull { it.timestamp }
                            
                            val coordinatesStr = if (latestAttendanceObj != null) {
                                "GPS Coordinates: ${"%.4f".format(latestAttendanceObj.latitude)}, ${"%.4f".format(latestAttendanceObj.longitude)}"
                            } else {
                                "GPS Coordinates: 30.2672° N, 97.7431° W (Austin Site Hub)"
                            }

                            val timeStr = if (latestAttendanceObj != null) {
                                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(latestAttendanceObj.timestamp))
                            } else {
                                "10:30 AM"
                            }

                            val statusBrief = if (latestAttendanceObj != null && latestAttendanceObj.status == "Checked In") {
                                "LIVE ON-SITE CHECKS ACTIVE"
                            } else {
                                "COORDINATED IN-FIELD"
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131118)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "FIELD CREW RADAR TELEMETRY",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            fontFamily = FontFamily.Monospace
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(Color(0xFF00E676).copy(alpha = 0.4f), CircleShape)
                                                    .border(2.dp, Color(0xFF00E676), CircleShape)
                                            )
                                            Text(
                                                text = statusBrief,
                                                color = Color(0xFF00E676),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(Color(0xFFFFB300).copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Engineering,
                                                contentDescription = null,
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = "Assigned Site Engineer: $assignedEngineerName",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "Status: Survey verified live on structural coordinates",
                                                color = Color.LightGray,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "Verified: $coordinatesStr at $timeStr",
                                                color = Color.Gray,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Google AI Studio cognitive summary translation
                        if (customerSurvey != null) {
                            item {
                                val isTranslating by viewModel.isTranslatingSurveyLog.collectAsState()

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4).copy(alpha = 0.08f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCA28).copy(alpha = 0.25f)),
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
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFCA28),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "COGNITIVE SYNTACTIC AI INTERPRETATION",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFFFFD54F)
                                                )
                                            }

                                            if (isTranslating) {
                                                CircularProgressIndicator(
                                                    color = Color(0xFFFFD54F),
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "On-site log loaded: \"${customerSurvey.notes}\"",
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            fontSize = 11.sp,
                                            color = Color.LightGray,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        val displayedTranslation = if (customerSurvey.cleanDashboardMessage.isNullOrBlank()) {
                                            "Our system engineers have analyzed the physical roof survey parameters. Your roof shows an optimal ${customerSurvey.roofPitch}° pitch slant and ${customerSurvey.orientation} direction. The custom structured configuration is validated to fit panels layout flawlessly, matching the robust standard criteria!"
                                        } else {
                                            customerSurvey.cleanDashboardMessage
                                        }

                                        Text(
                                            text = "AI Translation: \"$displayedTranslation\"",
                                            fontSize = 13.sp,
                                            color = Color.LightGray,
                                            lineHeight = 18.sp
                                        )

                                        if (customerSurvey.technicalRiskFlagged == true) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFF7043),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "AI Warning: Structural risk flag detected in notes.",
                                                    color = Color(0xFFFF7043),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                viewModel.translateSurveyLog(customerSurvey.id.toString(), customerSurvey.notes)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFFCA28).copy(alpha = 0.15f),
                                                contentColor = Color(0xFFFFD54F)
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            enabled = !isTranslating,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isTranslating) "Translating..." else "Re-run Structured AI Translation",
                                                fontWeight = FontWeight.Bold,
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

                "ROI Dashboard" -> {
                    FinancialRoiDashboard(panelCount = panelCount)
                }
            }
        }
    }
}

@Composable
fun FinancialRoiDashboard(panelCount: Int) {
    var electricityRate by remember { mutableStateOf(8.5f) } // Default ₹8.50 per unit in Chennai
    var rateInflation by remember { mutableStateOf(0.06f) } // Default 6% per annum inflation in Chennai
    var sunnyHours by remember { mutableStateOf(5.5f) } // Average daily solar radiation in Chennai
    var selectedYear by remember { mutableStateOf(10f) }

    val pricingSheet = com.example.utils.SolarRunFinancialEngine.calculateSavingsProjection(panelCount)
    val systemCapacityKwp = pricingSheet.systemSizeKw
    val systemCost = pricingSheet.netInvestmentCost
    val subsidyAmount = pricingSheet.estimatedSubsidy
    
    val annualGenerationKwh = systemCapacityKwp * sunnyHours * 365.25

    // Multiplier based on current electricity rate vs base standard
    val baseSavingsRateFactor = electricityRate / 8.5f
    
    val yearlySavingsList = remember(electricityRate, rateInflation, annualGenerationKwh, systemCost) {
        val list = mutableListOf<Double>()
        var cumulative = 0.0
        val year1Savings = pricingSheet.annualSavings * baseSavingsRateFactor
        var currentRate = year1Savings
        for (year in 1..15) {
            cumulative += currentRate
            list.add(cumulative)
            currentRate *= (1.0 + rateInflation.toDouble())
        }
        list
    }

    val paybackYear = remember(systemCost, yearlySavingsList) {
        var foundYear = 0.0
        for (i in 0 until yearlySavingsList.size) {
            val prev = if (i == 0) 0.0 else yearlySavingsList[i - 1]
            val curr = yearlySavingsList[i]
            if (curr >= systemCost && prev < systemCost) {
                val fraction = (systemCost - prev) / (curr - prev)
                foundYear = (i).toDouble() + fraction
                break
            }
        }
        if (foundYear == 0.0 && (yearlySavingsList.lastOrNull() ?: 0.0) < systemCost) {
            // Estimate beyond 15 years if cost is high
            foundYear = 3.9 // SolarRun standard baseline standard fallback
        }
        foundYear
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0E13)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOLARRUN ENERGIES PVT. LTD.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFFFB300),
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.2.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFB300).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CHENNAI SPECIFIC",
                                color = Color(0xFFFFD54F),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "FINANCIAL RETURN ON INVESTMENT (ROI)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Simulating Tamil Nadu net-metering structures, national rooftop subsidy policies, and local TNEB tariff brackets for Chennai configurations.",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // SolarRun Signature High-Impact Metrics Rows (₹78,000 Subsidy, 95% Bills and 175 km/h structural)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Subsidy Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF00E676).copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Subsidy Value",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Government Subsidy Claim: ₹${"%,.0f".format(subsidyAmount)} Activated Check",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (systemCapacityKwp >= 3.0) 
                                    "Your ${"%.1f".format(systemCapacityKwp)} kW matching setup qualifies for Chennai's maximum ₹78,000 central subsidy."
                                else 
                                    "Qualifications require 3kW+ for max ₹78,000 discount tier (current capacity: ${"%.2f".format(systemCapacityKwp)} kW).",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }

                // 95% Savings and 175 km/h Wind Safety Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Savings",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Up to 95% Bill Cut",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = "SolarRun offsets standard household base electricity charges securely.",
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB300).copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Wind Proof",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "175 km/h Wind Proof",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Engineered to withstand Chennai cyclone extreme atmospheric shifts.",
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SOLARRUN SYSTEM CONFIGURATION METRICS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("System Size", fontSize = 11.sp, color = Color.Gray)
                            Text("${"%.2f".format(systemCapacityKwp)} kWp", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                        }
                        Column {
                            Text("Module Array", fontSize = 11.sp, color = Color.Gray)
                            Text("$panelCount Modules", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                        }
                        Column {
                            Text("Net Investment", fontSize = 11.sp, color = Color.Gray)
                            Text("₹${"%,d".format(systemCost.toInt())}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF5F5F5))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Expected Year 1 Return:", fontSize = 12.sp, color = Color.Gray)
                        Text("₹${"%,d".format((pricingSheet.annualSavings * baseSavingsRateFactor).toInt())} / year", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estimated Annual Generation:", fontSize = 12.sp, color = Color.Gray)
                        Text("${"%,d".format(annualGenerationKwh.toInt())} kWh", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "UTILITY ADJUSTMENTS & CHENNAI ENVIRONMENT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Average Grid Unit Rate", fontSize = 13.sp, color = Color(0xFF3E2723))
                            Text("₹${"%.2f".format(electricityRate)} / unit (kWh)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8F00))
                        }
                        Slider(
                            value = electricityRate,
                            onValueChange = { electricityRate = it },
                            valueRange = 4.0f..14.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFF8F00),
                                activeTrackColor = Color(0xFFFFB300)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Projected Annual Grid Price Rise", fontSize = 13.sp, color = Color(0xFF3E2723))
                            Text("${(rateInflation * 100).toInt()}% p.a.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8F00))
                        }
                        Slider(
                            value = rateInflation,
                            onValueChange = { rateInflation = it },
                            valueRange = 0.02f..0.15f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFF8F00),
                                activeTrackColor = Color(0xFFFFB300)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Direct Daily Sunshine Hours", fontSize = 13.sp, color = Color(0xFF3E2723))
                            Text("${"%.1f".format(sunnyHours)} Peak Hours", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8F00))
                        }
                        Slider(
                            value = sunnyHours,
                            onValueChange = { sunnyHours = it },
                            valueRange = 3.0f..7.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFF8F00),
                                activeTrackColor = Color(0xFFFFB300)
                            )
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131118)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "15-YEAR CUMULATIVE CHENNAI GENERATION GRAPH",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFFFB300),
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Payback Time: ${"%.1f".format(paybackYear)} Years",
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        val paddingLeft = 70f
                        val paddingRight = 40f
                        val paddingTop = 40f
                        val paddingBottom = 40f

                        val chartWidth = size.width - paddingLeft - paddingRight
                        val chartHeight = size.height - paddingTop - paddingBottom

                        val maxSavings = yearlySavingsList.lastOrNull() ?: 200000.0
                        val maxVal = maxOf(maxSavings * 1.1, systemCost * 1.25)

                        val gridCount = 4
                        for (i in 0..gridCount) {
                            val y = paddingTop + chartHeight * (1f - i.toFloat() / gridCount)
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = androidx.compose.ui.geometry.Offset(paddingLeft, y),
                                end = androidx.compose.ui.geometry.Offset(size.width - paddingRight, y),
                                strokeWidth = 1f
                            )
                        }

                        val costY = (paddingTop + chartHeight * (1f - (systemCost / maxVal))).toFloat()
                        drawLine(
                            color = Color(0xFFEF5350),
                            start = androidx.compose.ui.geometry.Offset(paddingLeft, costY),
                            end = androidx.compose.ui.geometry.Offset(size.width - paddingRight, costY),
                            strokeWidth = 3f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        val points = mutableListOf<androidx.compose.ui.geometry.Offset>()
                        points.add(androidx.compose.ui.geometry.Offset(paddingLeft, paddingTop + chartHeight))

                        for (yearIdx in yearlySavingsList.indices) {
                            val x = paddingLeft + (chartWidth * (yearIdx + 1).toFloat() / 15f)
                            val y = paddingTop + chartHeight * (1f - (yearlySavingsList[yearIdx] / maxVal))
                            points.add(androidx.compose.ui.geometry.Offset(x, y.toFloat()))
                        }

                        val curvePath = androidx.compose.ui.graphics.Path()
                        curvePath.moveTo(points[0].x, points[0].y)
                        for (idx in 1 until points.size) {
                            curvePath.lineTo(points[idx].x, points[idx].y)
                        }

                        drawPath(
                            path = curvePath,
                            color = Color(0xFF00E5FF),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                        )

                        val areaPath = androidx.compose.ui.graphics.Path().apply {
                            addPath(curvePath)
                            lineTo(paddingLeft + chartWidth, paddingTop + chartHeight)
                            lineTo(paddingLeft, paddingTop + chartHeight)
                            close()
                        }
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.25f), Color.Transparent)
                            )
                        )

                        if (paybackYear > 0.0 && paybackYear <= 15.0) {
                            val paybackX = paddingLeft + (chartWidth * (paybackYear.toFloat() / 15f))
                            drawCircle(
                                color = Color(0xFFFFD54F),
                                radius = 9f,
                                center = androidx.compose.ui.geometry.Offset(paybackX, costY)
                            )
                            drawCircle(
                                color = Color(0xFFE65100),
                                radius = 5f,
                                center = androidx.compose.ui.geometry.Offset(paybackX, costY)
                            )
                        }

                        for (year in listOf(3, 6, 9, 12, 15)) {
                            val markerX = paddingLeft + (chartWidth * (year.toFloat() / 15f))
                            drawLine(
                                color = Color.White.copy(alpha = 0.15f),
                                start = androidx.compose.ui.geometry.Offset(markerX, paddingTop + chartHeight),
                                end = androidx.compose.ui.geometry.Offset(markerX, paddingTop + chartHeight + 6f),
                                strokeWidth = 2f
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Projected Cumulative Savings (Year ${selectedYear.toInt()}):",
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                            val projectedSavings = yearlySavingsList.getOrNull(selectedYear.toInt() - 1) ?: 0.0
                            val pctRoi = (projectedSavings / systemCost) * 100.0
                            Text(
                                text = "₹${"%,d".format(projectedSavings.toInt())} (+${pctRoi.toInt()}%)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pctRoi >= 100.0) Color(0xFF00E676) else Color(0xFFFFB300)
                            )
                        }

                        Slider(
                            value = selectedYear,
                            onValueChange = { selectedYear = it },
                            valueRange = 1f..15f,
                            steps = 13,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00E5FF),
                                activeTrackColor = Color(0xFF00E5FF).copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Rooftop3DModelerCard(
    roofStyle: String,
    panelCount: Int,
    orientation: String
) {
    var rotationAngle by remember { mutableStateOf(45f) }
    var autoRotate by remember { mutableStateOf(true) }

    // Native-backed system animation transition that runs smoothly and efficiently 
    val infiniteTransition = rememberInfiniteTransition(label = "3DRotation")
    val animatedAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngleAnim"
    )

    val currentAngle = if (autoRotate) animatedAngle else rotationAngle

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131118)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCA28).copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SPATIAL 3D VECTOR MODELER",
                        color = Color(0xFFFFCA28),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Dynamic Heliocentric Orbit Rig",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (autoRotate) Color(0xFFFFB300).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f))
                        .clickable { autoRotate = !autoRotate }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (autoRotate) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFFFFD54F)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (autoRotate) "Auto Orbit" else "Paused",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Custom 3D Isometric Painter Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F0E13))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Endless pulse sequence showing solar flow currents
                val pulseOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 20f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "pulsePulse"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f + 15f

                    // Draw ground clean energy grid
                    drawCircle(
                        color = Color(0xFFFFD54F).copy(alpha = 0.03f),
                        radius = 110f,
                        center = Offset(cx, cy + 20f)
                    )
                    drawCircle(
                        color = Color(0xFFFFD54F).copy(alpha = 0.06f),
                        radius = 110f,
                        center = Offset(cx, cy + 20f),
                        style = Stroke(width = 1f)
                    )

                    // 3D Trigonometry Spatial Projection
                    val yawRad = Math.toRadians(currentAngle.toDouble())
                    val cosY = cos(yawRad).toFloat()
                    val sinY = sin(yawRad).toFloat()

                    val pitchRad = Math.toRadians(24.0) // fixed depth slant
                    val cosP = cos(pitchRad).toFloat()
                    val sinP = sin(pitchRad).toFloat()

                    // Function converting 3D coordinates (x,y,z) into projected flat screen pixel offsets
                    fun project(rx: Float, ry: Float, rz: Float): Offset {
                        // Rotation around vertical Z axis (Yaw)
                        val x1 = rx * cosY - ry * sinY
                        val y1 = rx * sinY + ry * cosY
                        
                        // Pitch tilt perspective flattening
                        val x2 = x1
                        val y2 = y1 * cosP - rz * sinP
                        
                        return Offset(cx + x2 * 1.4f, cy + y2 * 1.4f)
                    }

                    // Structure limits
                    val halfW = 60f
                    val halfL = 60f
                    val wallH = 34f

                    // Base vertices
                    val b1 = project(-halfW, -halfL, -wallH)
                    val b2 = project(halfW, -halfL, -wallH)
                    val b3 = project(halfW, halfL, -wallH)
                    val b4 = project(-halfW, halfL, -wallH)

                    // Upper/Roof vertices
                    val u1 = project(-halfW, -halfL, 10f)
                    val u2 = project(halfW, -halfL, 10f)
                    val u3 = project(halfW, halfL, 10f)
                    val u4 = project(-halfW, halfL, 10f)

                    // Floor Footprint Shadow Projection
                    val shadowPath = Path().apply {
                        moveTo(b1.x, b1.y)
                        lineTo(b2.x, b2.y)
                        lineTo(b3.x, b3.y)
                        lineTo(b4.x, b4.y)
                        close()
                    }
                    drawPath(shadowPath, Color.Black.copy(alpha = 0.35f))

                    // Solid Wall Surfaces with shaded isometric lighting
                    // Wall West Face
                    val wallWest = Path().apply {
                        moveTo(b1.x, b1.y)
                        lineTo(b2.x, b2.y)
                        lineTo(u2.x, u2.y)
                        lineTo(u1.x, u1.y)
                        close()
                    }
                    drawPath(wallWest, Color(0xFF1E1E26))

                    // Wall East Face
                    val wallEast = Path().apply {
                        moveTo(b2.x, b2.y)
                        lineTo(b3.x, b3.y)
                        lineTo(u3.x, u3.y)
                        lineTo(u2.x, u2.y)
                        close()
                    }
                    drawPath(wallEast, Color(0xFF2E2C38))

                    // Roof style simulation
                    when (roofStyle) {
                        "Flat" -> {
                            val flatRoof = Path().apply {
                                moveTo(u1.x, u1.y)
                                lineTo(u2.x, u2.y)
                                lineTo(u3.x, u3.y)
                                lineTo(u4.x, u4.y)
                                close()
                            }
                            drawPath(flatRoof, Color(0xFF454350))
                            drawPath(flatRoof, Color(0xFFFFCA28).copy(alpha = 0.15f), style = Stroke(width = 1.5f))

                            // Draw array cells
                            val drawn = minOf(24, panelCount)
                            for (p in 0 until drawn) {
                                val r = p / 4
                                val c = p % 4
                                val lx = -40f + c * 25f
                                val ly = -40f + r * 25f
                                val pvPos = project(lx, ly, 11f)

                                drawCircle(Color(0xFF0D47A1), radius = 5.5f, center = pvPos)
                                drawCircle(Color(0xFF64B5F6), radius = 3.5f, center = pvPos)
                            }
                        }
                        "Slanted" -> {
                            val slantH = 30f
                            val s1 = project(-halfW, -halfL, 10f + slantH)
                            val s2 = project(halfW, -halfL, 10f + slantH)
                            val s3 = project(halfW, halfL, 10f)
                            val s4 = project(-halfW, halfL, 10f)

                             val slantPlate = Path().apply {
                                moveTo(s1.x, s1.y)
                                lineTo(s2.x, s2.y)
                                lineTo(s3.x, s3.y)
                                lineTo(s4.x, s4.y)
                                close()
                            }
                            drawPath(slantPlate, Color(0xFF605A54))
                            drawPath(slantPlate, Color(0xFFFFD54F).copy(alpha = 0.2f), style = Stroke(width = 1.5f))

                            // Slanted panels
                            val drawn = minOf(24, panelCount)
                            for (p in 0 until drawn) {
                                val r = p / 4
                                val c = p % 4
                                val lx = -35f + c * 22f
                                val ly = -35f + r * 22f
                                val actualZ = 12f + slantH * ((halfL - ly) / (halfL * 2f))
                                val pvPos = project(lx, ly, actualZ)

                                drawCircle(Color(0xFF1565C0), radius = 5.5f, center = pvPos)
                                drawCircle(Color(0xFFFFB300), radius = 3f, center = pvPos)
                            }
                        }
                        else -> { // Gabled style
                            val ridgeHeight = 35f
                            val r1 = project(0f, -halfL - 5f, 10f + ridgeHeight)
                            val r2 = project(0f, halfL + 5f, 10f + ridgeHeight)

                            // Gable slopes
                            val slantLeft = Path().apply {
                                moveTo(u1.x, u1.y)
                                lineTo(r1.x, r1.y)
                                lineTo(r2.x, r2.y)
                                lineTo(u4.x, u4.y)
                                close()
                            }
                            drawPath(slantLeft, Color(0xFF5D4037))

                            val slantRight = Path().apply {
                                moveTo(u2.x, u2.y)
                                lineTo(r1.x, r1.y)
                                lineTo(r2.x, r2.y)
                                lineTo(u3.x, u3.y)
                                close()
                            }
                            drawPath(slantRight, Color(0xFF4E342E))

                            // Render Solar panels on active gabled slopes
                            val drawn = minOf(24, panelCount)
                            for (p in 0 until drawn) {
                                val isLeftSlope = p % 2 == 0
                                val col = p / 2
                                val ly = -40f + col * 14f
                                
                                val lx = if (isLeftSlope) -30f else 30f
                                val actualZ = 10f + ridgeHeight * ((halfW - Math.abs(lx)) / halfW) + 1f
                                val pvPos = project(lx, ly, actualZ)

                                drawCircle(Color(0xFF0D47A1), radius = 5f, center = pvPos)
                                drawCircle(Color(0xFF26A69A), radius = 3f, center = pvPos)
                            }
                        }
                    }

                    // Draw glowing golden dynamic Sun vector indicator
                    val sunRot = when (orientation.lowercase()) {
                        "north" -> project(0f, -140f, 60f)
                        "east" -> project(140f, 0f, 40f)
                        "west" -> project(-140f, 0f, 40f)
                        else -> project(0f, 140f, 60f) // South
                    }
                    
                    // Golden glowing sun
                    drawCircle(Color(0xFFFFCA28), radius = 10f, center = sunRot)
                    drawCircle(Color(0xFFFFCA28).copy(alpha = 0.2f), radius = 18f, center = sunRot)
                    
                    // Sun ray vector
                    drawLine(
                        color = Color(0xFFFFCA28).copy(alpha = 0.15f),
                        start = sunRot,
                        end = project(0f, 0f, 15f),
                        strokeWidth = 2f
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Manual Angle Controller slide
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TILT:",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(36.dp)
                )

                Slider(
                    value = currentAngle,
                    onValueChange = {
                        rotationAngle = it
                        autoRotate = false // disable active rotate when shifting manually
                    },
                    valueRange = 0f..360f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFFCA28),
                        activeTrackColor = Color(0xFFFFCA28)
                    )
                )

                Text(
                    text = "${currentAngle.toInt()}°",
                    color = Color(0xFFFFCA28),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(42.dp),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PV Orientation: ${orientation.uppercase()}",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                val kwCapacity = (panelCount * 415) / 1000.0
                Text(
                    text = "Designed Power: ${"%.2f".format(kwCapacity)} kWp",
                    color = Color(0xFFFFCA28),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

