package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CanvasSolarMap(
    roofLength: Double,
    roofWidth: Double,
    roofPitch: Int,
    orientation: String,
    modifier: Modifier = Modifier
) {
    // 1 standard solar panel is approx 1.7m x 1.0m
    val panelLength = 1.7
    val panelWidth = 1.0
    
    // Fit calculations (Portrait installation helper)
    val cols = if (roofWidth > 0) (roofWidth / panelWidth).toInt() else 0
    val rows = if (roofLength > 0) (roofLength / panelLength).toInt() else 0
    val totalPanels = cols * rows
    val systemCapacityKwp = (totalPanels * 410) / 1000.0 // 410W per panel

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface) // Professional White Card Background
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DXY ARRAY SCHEMATIC",
                    color = MaterialTheme.colorScheme.primary, // Solar Purple Primary
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Interactive Roof PV Layout",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${"%.2f".format(systemCapacityKwp)} kWp",
                    color = MaterialTheme.colorScheme.primary, // Purple Accent
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Est. Capacity ($totalPanels Panels)",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Visual Canvas Draw
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141218)), // High-contrast rich dark blueprint sheet
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw tech grid backdrop safely without while loops
                val gridSpacing = 24f
                if (gridSpacing > 0.001f && canvasWidth.isFinite() && canvasHeight.isFinite()) {
                    val numXLines = (canvasWidth / gridSpacing).toInt().coerceIn(0, 500)
                    for (i in 0..numXLines) {
                        val cx = i * gridSpacing
                        drawLine(
                            color = Color(0xFF1D2D3D),
                            start = Offset(cx, 0f),
                            end = Offset(cx, canvasHeight),
                            strokeWidth = 1f
                        )
                    }
                    val numYLines = (canvasHeight / gridSpacing).toInt().coerceIn(0, 500)
                    for (i in 0..numYLines) {
                        val cy = i * gridSpacing
                        drawLine(
                            color = Color(0xFF1D2D3D),
                            start = Offset(0f, cy),
                            end = Offset(canvasWidth, cy),
                            strokeWidth = 1f
                        )
                    }
                }

                // If dimensions are <= 0, draw placeholder roof bounds
                val drawLength = if (roofLength > 0) roofLength else 10.0
                val drawWidth = if (roofWidth > 0) roofWidth else 15.0

                // Aspect ratio fit
                val scale = minOf(
                    (canvasWidth * 0.7f) / drawWidth.toFloat(),
                    (canvasHeight * 0.7f) / drawLength.toFloat()
                )

                val roofW = drawWidth.toFloat() * scale
                val roofH = drawLength.toFloat() * scale
                val left = (canvasWidth - roofW) / 2f
                val top = (canvasHeight - roofH) / 2f

                // Draw the physical roof deck
                drawRect(
                    color = Color(0xFF2C3E50),
                    topLeft = Offset(left, top),
                    size = Size(roofW, roofH)
                )

                // Draw roof ridge outline
                drawRect(
                    color = Color(0xFF7F8C8D),
                    topLeft = Offset(left, top),
                    size = Size(roofW, roofH),
                    style = Stroke(width = 4f)
                )

                // Draw pitch tilt contouring line representations
                val pitchLinesMode = if (roofPitch > 10) (roofPitch / 10) else 1
                for (i in 1..pitchLinesMode) {
                    val lineY = top + (roofH / (pitchLinesMode + 1)) * i
                    drawLine(
                        color = Color(0xFF5D6D7E),
                        start = Offset(left, lineY),
                        end = Offset(left + roofW, lineY),
                        strokeWidth = 2f
                    )
                }

                // If valid dims, draw individual solar PV panel cells
                if (roofWidth > 0 && roofLength > 0 && cols > 0 && rows > 0) {
                    val isLargeArray = cols > 30 || rows > 30
                    val drawCols = if (isLargeArray) 30 else cols
                    val drawRows = if (isLargeArray) 30 else rows

                    val pW = (if (isLargeArray) (roofWidth / drawCols) else panelWidth).toFloat() * scale
                    val pH = (if (isLargeArray) (roofLength / drawRows) else panelLength).toFloat() * scale

                    // Centering panels within the roof frame
                    val totalGridW = drawCols * pW
                    val totalGridH = drawRows * pH
                    val startX = left + (roofW - totalGridW) / 2f
                    val startY = top + (roofH - totalGridH) / 2f

                    for (r in 0 until drawRows) {
                        for (c in 0 until drawCols) {
                            val px = startX + c * pW
                            val py = startY + r * pH

                            // Draw individual deep blue PV panel wafer
                            drawRect(
                                color = Color(0xFF1565C0),
                                topLeft = Offset(px + 1.5f, py + 1.5f),
                                size = Size(pW - 3f, pH - 3f)
                            )

                            if (pW > 12f && pH > 12f) {
                                // Inner reflective pattern lines (semiconductor grid)
                                drawLine(
                                    color = Color(0xFF90CAF9).copy(alpha = 0.5f),
                                    start = Offset(px + pW / 2f, py + 1.5f),
                                    end = Offset(px + pW / 2f, py + pH - 1.5f),
                                    strokeWidth = 1f
                                )
                                drawLine(
                                    color = Color(0xFF90CAF9).copy(alpha = 0.5f),
                                    start = Offset(px + 1.5f, py + pH / 2f),
                                    end = Offset(px + pW - 1.5f, py + pH / 2f),
                                    strokeWidth = 1f
                                )
                            }

                            // Frame border
                            if (pW > 6f && pH > 6f) {
                                drawRect(
                                    color = Color.White.copy(alpha = 0.6f),
                                    topLeft = Offset(px + 0.5f, py + 0.5f),
                                    size = Size(pW - 1f, pH - 1f),
                                    style = Stroke(width = 1f)
                                )
                            }
                        }
                    }
                } else {
                    // Draw guide text
                    // (Omitted standard canvas native textual drawing to avoid font/context crashes in diverse environments)
                }

                // Draw Compass Direction / Sun Indicator based on Orientation
                val sunColor = Color(0xFFFFD54F)
                val sunCenter = when (orientation.lowercase()) {
                    "north" -> Offset(canvasWidth / 2f, top - 25f)
                    "east" -> Offset(left + roofW + 25f, canvasHeight / 2f)
                    "west" -> Offset(left - 25f, canvasHeight / 2f)
                    else -> Offset(canvasWidth / 2f, top + roofH + 25f) // South Default
                }

                // Draw glowing Sun node
                drawCircle(
                    color = sunColor,
                    radius = 12f,
                    center = sunCenter
                )
                // Draw sun ray indicators
                for (heading in 0 until 8) {
                    val angle = heading * Math.PI / 4
                    val rx = sunCenter.x + (18f * cos(angle)).toFloat()
                    val ry = sunCenter.y + (18f * sin(angle)).toFloat()
                    drawLine(
                        color = sunColor.copy(alpha = 0.7f),
                        start = sunCenter,
                        end = Offset(rx, ry),
                        strokeWidth = 2f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dimensional and orientation legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Size: ${roofWidth}m x ${roofLength}m",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Tilt: $roofPitch° (${orientation.uppercase()})",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
