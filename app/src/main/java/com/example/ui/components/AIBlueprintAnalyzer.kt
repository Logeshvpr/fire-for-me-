package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.AutomatedSiteBlueprint
import com.example.viewmodel.AppViewModel

@Composable
fun AIBlueprintAnalyzer(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val analyzedBlueprint by viewModel.analyzedBlueprint.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingBlueprint.collectAsState()

    var selectedPresetIndex by remember { mutableStateOf(0) }
    val presets = listOf(
        BlueprintPreset(
            title = "Austin Residential Gabled Frame",
            description = "Standard gabled frame roof with clear Southern solar exposure vector path.",
            colorHex = 0xFFFFCA28
        ),
        BlueprintPreset(
            title = "Commercial Slanted Solar Array",
            description = "Industrial flat rooftop section with a 15-degree structural tilt framing.",
            colorHex = 0xFF00E5FF
        ),
        BlueprintPreset(
            title = "Suburban Flat Canopy Layout",
            description = "Modern pergola structure with light canopy overhangs and high load capacity.",
            colorHex = 0xFF76FF03
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_blueprint_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0E13)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Blueprint Camera Scan",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MULTIMODAL BLUEPRINT VISION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFFB300),
                        letterSpacing = 1.2.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE53935).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "COGNITIVE ACTIVE",
                        color = Color(0xFFFF7043),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Spatial Computer Vision Analyzer",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Capture a blueprint sketch or select an aerial satellite crop to generate optimal 3D solar parameters directly into Room store.",
                color = Color.LightGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dynamic Preset Blueprint Selector
            Text(
                text = "SELECT BLUEPRINT PATTERN MODEL TO AUDIT:",
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            presets.forEachIndexed { index, preset ->
                val isSelected = selectedPresetIndex == index
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(preset.colorHex) else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedPresetIndex = index }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(preset.colorHex).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Home
                                    1 -> Icons.Default.Business
                                    else -> Icons.Default.Layers
                                },
                                contentDescription = null,
                                tint = Color(preset.colorHex),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = preset.description,
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active Selection",
                                tint = Color(preset.colorHex),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Vision Action Panel
            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.03f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFFFB300),
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                        Column {
                            Text(
                                text = "DECODING VECTOR SPATIAL blueprints...",
                                fontWeight = FontWeight.Bold,
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Running Gemini 1.5 Flash structured audit",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = {
                        // Generate a dummy bitmap to pass to the repository vision orchestrator
                        val conf = Bitmap.Config.ARGB_8888
                        val bmp = Bitmap.createBitmap(200, 200, conf)
                        val canvas = Canvas(bmp)
                        val paint = Paint()
                        paint.color = AndroidColor.BLUE
                        canvas.drawCircle(50f, 50f, 30f, paint)

                        viewModel.analyzeBlueprint(bmp, currentUser?.username ?: "Mark Client")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("analyze_blueprint_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB300),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Trigger AI Vision Check"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RUN COMPUTER VISION LAYOUT EXTRACTION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Results Animation
            val blueprint = analyzedBlueprint
            if (blueprint != null && !isAnalyzing) {
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "AI VISION ANALYSIS RESULT:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color(0xFFFFC107),
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Grid layout details
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131118)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rooftop Profile Style",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFB300).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = blueprint.suggestedRoofStyle,
                                    color = Color(0xFFFFD54F),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Max Layout Density",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${blueprint.absolutePanelCapacity} Panels Max",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Structural Risk Grade",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (blueprint.structuralRiskAssessment) {
                                            "LOW" -> Color(0xFF00E676).copy(alpha = 0.15f)
                                            "MEDIUM" -> Color(0xFFFF9100).copy(alpha = 0.15f)
                                            else -> Color(0xFFFF1744).copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = blueprint.structuralRiskAssessment,
                                    color = when (blueprint.structuralRiskAssessment) {
                                        "LOW" -> Color(0xFF00E676)
                                        "MEDIUM" -> Color(0xFFFF9100)
                                        else -> Color(0xFFFF1744)
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // High fidelity 7-point efficiency trend curve drawing canvas
                        Text(
                            text = "ESTIMATED SOLAR CONVERSION EFFICIENCY CURVE (DIURNAL):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height
                                val vals = blueprint.estimatedGenerationCurve
                                val pointCount = vals.size

                                if (pointCount > 1) {
                                    val stepX = width / (pointCount - 1)
                                    val path = Path()

                                    vals.forEachIndexed { idx, value ->
                                        // Map 0.0f..1.0f on flipped Y coordinate
                                        val x = idx * stepX
                                        val y = height - (value * (height - 12f)) - 6f

                                        if (idx == 0) {
                                            path.moveTo(x, y)
                                        } else {
                                            path.lineTo(x, y)
                                        }

                                        // Draw coordinate dots
                                        drawCircle(
                                            color = Color(0xFFFFCA28),
                                            radius = 4f,
                                            center = androidx.compose.ui.geometry.Offset(x, y)
                                        )
                                    }

                                    drawPath(
                                        path = path,
                                        color = Color(0xFFFFCA28),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Success notification
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Model synced to local Room Database flawlessly.",
                                color = Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

data class BlueprintPreset(
    val title: String,
    val description: String,
    val colorHex: Long
)
