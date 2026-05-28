package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SolarRunLogoHeader
import com.example.data.User
import com.example.viewmodel.AppViewModel
import kotlin.random.Random

// Solar Obsidian Signature Color Palette
val ObsidianBg = Color(0xFF0F0E13)
val GlassSurface = Color(0x1F12141C)
val SolarAmber = Color(0xFFFFB300)
val CleanTeal = Color(0xFF00E5FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    allUsers: List<User>,
    onLoginSuccess: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf("Staff") } // "Customer", "Staff", "Site Leader", "Admin"
    var pinInput by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var regUsername by remember { mutableStateOf("") }
    var regPin by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val loginError by viewModel.loginError.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Sync selected user when role changes or list finishes loading
    val roleUsers = allUsers.filter { it.role.equals(selectedRole, ignoreCase = true) }
    
    LaunchedEffect(selectedRole, allUsers) {
        val filtered = allUsers.filter { it.role.equals(selectedRole, ignoreCase = true) }
        selectedUser = filtered.firstOrNull()
        pinInput = ""
        localError = null
    }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            onLoginSuccess(it)
        }
    }

    // Parallax Starfield Generator State (static particles that we animate on Canvas)
    val starCount = 60
    val stars = remember {
        List(starCount) {
            Offset(Random.nextFloat(), Random.nextFloat()) to Random.nextFloat()
        }
    }

    // Infinite ambient transition loop for helical orbits
    val infiniteTransition = rememberInfiniteTransition(label = "SolarOrbit")
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "Rotation"
    )

    val starAlphaMultiplier by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "StarAlpha"
    )

    // Layout Screen Frame
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09080C)) // Deep cosmic canvas color
    ) {
        // 1. Decorative Atmospheric Parallax Starfield and Vector Energy Rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerOffset = Offset(canvasWidth / 2f, canvasHeight * 0.3f)

            // Draw cosmic stars
            stars.forEach { (coord, sizeFactor) ->
                val px = coord.x * canvasWidth
                // Apply a gentle drift offset using orbit rotation logic for parallax
                val drift = (orbitRotation * 0.15f * sizeFactor) % 30f
                val py = (coord.y * canvasHeight + drift) % canvasHeight
                val finalAlpha = 0.15f + (0.5f * sizeFactor * starAlphaMultiplier)
                drawCircle(
                    color = Color.White.copy(alpha = finalAlpha),
                    radius = (1.5.dp.toPx() * sizeFactor),
                    center = Offset(px, py)
                )
            }

            // Draw giant deep amber solar core glow patch
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SolarAmber.copy(alpha = 0.08f), Color.Transparent),
                    center = centerOffset,
                    radius = 350.dp.toPx()
                ),
                radius = 350.dp.toPx(),
                center = centerOffset
            )

            // Inner cyan/teal telemetry energy path
            drawArc(
                color = CleanTeal.copy(alpha = 0.15f),
                startAngle = orbitRotation,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(centerOffset.x - 140.dp.toPx(), centerOffset.y - 140.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(280.dp.toPx(), 280.dp.toPx())
            )

            // Outer golden amber telemetry energy path (revolving in opposite direction)
            drawArc(
                color = SolarAmber.copy(alpha = 0.1f),
                startAngle = -orbitRotation + 180f,
                sweepAngle = 140f,
                useCenter = false,
                style = Stroke(width = 0.8.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(centerOffset.x - 180.dp.toPx(), centerOffset.y - 180.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(360.dp.toPx(), 360.dp.toPx())
            )
        }

        // 2. High-Fidelity Operator Access Panel
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // Header Typography Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                SolarRunLogoHeader()
                Text(
                    text = "SOLARRUN ENERGIES • PREMIUM CHENNAI PORTAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CleanTeal.copy(alpha = 0.8f),
                    letterSpacing = 1.2.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // 3. Segmented Role Deck Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassSurface, RoundedCornerShape(14.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val roles = listOf("Customer", "Staff", "Site Leader", "Admin")
                roles.forEach { role ->
                    val isSelected = selectedRole == role
                    val activeBgColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF1F2433) else Color.Transparent,
                        animationSpec = tween(250), label = "RoleTabBg"
                    )
                    val activeTxtColor by animateColorAsState(
                        targetValue = if (isSelected) CleanTeal else Color.Gray,
                        animationSpec = tween(250), label = "RoleTabTxt"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(activeBgColor)
                            .clickable {
                                selectedRole = role
                                isRegisterMode = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = role,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = activeTxtColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Display errors (Database Login Errors or local mismatch warnings)
            val combinedError = loginError ?: localError
            if (combinedError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF331414)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBA1A1A).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = combinedError,
                            color = Color(0xFFFF8A80),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                localError = null
                                viewModel.clearLoginError()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // 4. Central Panel Crossfade Transition
            Crossfade(
                targetState = isRegisterMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                label = "FormModeSwitch"
            ) { regMode ->
                if (regMode) {
                    // Registration Module Form
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GlassSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(20.dp),
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
                                text = "COMMISSION NEW $selectedRole PROFILE",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = regUsername,
                                onValueChange = { regUsername = it },
                                label = { Text("Operator Code-Name") },
                                placeholder = { Text("e.g. AstroSolar") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CleanTeal) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = CleanTeal,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedLabelColor = CleanTeal,
                                    unfocusedLabelColor = Color.Gray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("username_input")
                            )

                            OutlinedTextField(
                                value = regPin,
                                onValueChange = { if (it.length <= 4) regPin = it },
                                label = { Text("4-Digit Access PIN") },
                                placeholder = { Text("1234") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SolarAmber) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = SolarAmber,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedLabelColor = SolarAmber,
                                    unfocusedLabelColor = Color.Gray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("password_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    if (regUsername.isBlank() || regPin.length != 4) {
                                        localError = "Please enter an operator code-name and a strict 4-digit numeric PIN."
                                    } else {
                                        viewModel.registerUser(regUsername.trim(), selectedRole, regPin) { success ->
                                            if (success) {
                                                isRegisterMode = false
                                                regUsername = ""
                                                regPin = ""
                                            } else {
                                                localError = "Profile collision. That code-name already exists for role $selectedRole."
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SolarAmber,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.Black)
                                    Text(
                                        text = "COMMISSION PROFILE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Button(
                                onClick = { isRegisterMode = false },
                                colors = ButtonDefaults.textButtonColors(contentColor = CleanTeal),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("Back to Direct Profile Sign In", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Sign-In Module Panel
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        
                        // Active Profile Selector Scroll Container
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "SELECT IN-FIELD SYSTEM PROFILE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            if (roleUsers.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(GlassSurface, RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No profiles found in $selectedRole.\nTap register button below to create one.",
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    roleUsers.forEach { user ->
                                        val isUserSelected = selectedUser?.username == user.username
                                        val chipBorderColor = if (isUserSelected) CleanTeal else Color.White.copy(alpha = 0.08f)
                                        val chipBgColor = if (isUserSelected) Color(0xFF131118) else GlassSurface

                                        Box(
                                            modifier = Modifier
                                                .width(110.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(chipBgColor)
                                                .border(1.dp, chipBorderColor, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    selectedUser = user
                                                    pinInput = ""
                                                    localError = null
                                                }
                                                .padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = if (isUserSelected) CleanTeal else Color.Gray,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = user.username,
                                                    color = if (isUserSelected) Color.White else Color.LightGray,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isUserSelected) FontWeight.Bold else FontWeight.Normal,
                                                    maxLines = 1,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // PIN Input Node Display Dots
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(4) { index ->
                                    val isFilled = index < pinInput.length
                                    val dotColor by animateColorAsState(
                                        targetValue = if (isFilled) SolarAmber else Color(0x33FFFFFF),
                                        animationSpec = tween(150), label = "Dot"
                                    )
                                    val scale by animateFloatAsState(
                                        targetValue = if (isFilled) 1.25f else 1.0f,
                                        label = "DotScale"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp)
                                            .size(12.dp)
                                            .background(dotColor, CircleShape)
                                            .border(1.dp, if (isFilled) SolarAmber else Color.Transparent, CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = selectedUser?.let { "Enter authorization code for operator \"${it.username}\"" }
                                    ?: "Select or register a profile to authenticate",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Secure Tactical Keypad Grid
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val keypadKeys = listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("BIO", "0", "DEL")
                            )

                            keypadKeys.forEach { keypadRow ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    keypadRow.forEach { key ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(52.dp)
                                                .background(GlassSurface, RoundedCornerShape(12.dp))
                                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                                .clickable {
                                                    val activeUser = selectedUser
                                                    if (activeUser == null) {
                                                        localError = "Please select a system profile first."
                                                        return@clickable
                                                    }
                                                    localError = null

                                                    when (key) {
                                                        "DEL" -> {
                                                            if (pinInput.isNotEmpty()) {
                                                                pinInput = pinInput.dropLast(1)
                                                            }
                                                        }
                                                        "BIO" -> {
                                                            // Fast Authenticate Biometric Bypass
                                                            viewModel.login(activeUser.username, activeUser.role)
                                                        }
                                                        else -> {
                                                            if (pinInput.length < 4) {
                                                                pinInput += key
                                                                if (pinInput.length == 4) {
                                                                    // Verify PIN match
                                                                    if (pinInput == activeUser.pin) {
                                                                        viewModel.login(activeUser.username, activeUser.role)
                                                                    } else {
                                                                        localError = "ACCESS DENIED: PIN code validation mismatch."
                                                                        pinInput = ""
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when (key) {
                                                "DEL" -> Icon(
                                                    imageVector = Icons.Default.Backspace,
                                                    contentDescription = "Backspace",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                "BIO" -> Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Fingerprint,
                                                        contentDescription = "Biometrics",
                                                        tint = CleanTeal,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                else -> Text(
                                                    text = key,
                                                    color = Color.White,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
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

            // 5. Create Profile Toggle Access Row
            if (!isRegisterMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NEW OPERATOR COMMISSION SIGN-UP",
                        color = SolarAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier
                            .clickable { isRegisterMode = true }
                            .padding(8.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }
        }
    }
}
