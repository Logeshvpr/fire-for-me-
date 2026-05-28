package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun VoiceCopilot(
    currentRoofStyle: String,
    onRoofStyleChange: (String) -> Unit,
    currentPanelCount: Int,
    onPanelCountChange: (Int) -> Unit,
    currentOrientation: String,
    onOrientationChange: (String) -> Unit,
    currentEfficiency: String,
    onEfficiencyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var spokenText by remember { mutableStateOf("") }
    var assistantResponse by remember { mutableStateOf("Hello! I am your Solar Command Voice Copilot. Tap the mic or choose a suggestion below to direct our design grid!") }
    
    // Suggestion pills for voice triggers & quick touch commands
    val suggestions = listOf(
        "Change roof to Slanted slope",
        "Change roof to Gabled slope",
        "Set flat roof design",
        "Increase panel count to 16",
        "Set orientation to West",
        "What is our estimated power?",
        "Select premium monocrystalline grade"
    )

    // Setup TextToSpeech
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val initializedTts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configured successfully
            }
        }
        initializedTts.language = Locale.US
        tts = initializedTts
        onDispose {
            initializedTts.stop()
            initializedTts.shutdown()
        }
    }

    // Function to speak assistant replies aloud
    val speakAloud: (String) -> Unit = { text ->
        tts?.let {
            isSpeaking = true
            it.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SolarEngineTTS")
        }
    }

    // Checking speaking state in loop
    LaunchedEffect(isSpeaking) {
        if (isSpeaking) {
            delay(4000) // Estimate speaking length window or monitor TTS
            isSpeaking = false
        }
    }

    // Cognitive Parser to coordinate direct spoken triggers into design edits!
    val parseCommand: (String) -> Unit = { command ->
        val cleaned = command.lowercase(Locale.getDefault())
        var matched = false
        var reply = ""

        when {
            cleaned.contains("slanted") || cleaned.contains("slant") -> {
                onRoofStyleChange("Slanted")
                reply = "Command received. Adjusting blueprint to slanted structure profile."
                matched = true
            }
            cleaned.contains("flat") -> {
                onRoofStyleChange("Flat")
                reply = "Adjusting. Configuring streamlined flat roof layout."
                matched = true
            }
            cleaned.contains("gabled") || cleaned.contains("gable") -> {
                onRoofStyleChange("Gabled")
                reply = "Design set. Transitioning to symmetric gabled ridge roof layout."
                matched = true
            }
            cleaned.contains("increase") || cleaned.contains("more panels") || cleaned.contains("add panels") -> {
                val nextCount = minOf(24, currentPanelCount + 4)
                onPanelCountChange(nextCount)
                reply = "Sizing array upwards. Increased active count to $nextCount panels for higher peak power."
                matched = true
            }
            cleaned.contains("decrease") || cleaned.contains("fewer panels") -> {
                val nextCount = maxOf(4, currentPanelCount - 4)
                onPanelCountChange(nextCount)
                reply = "Scaling down density. Set solar cell mesh to $nextCount panels."
                matched = true
            }
            cleaned.contains("16 panels") || cleaned.contains("sixteen") -> {
                onPanelCountChange(16)
                reply = "Calibrated. Set active design layout exactly to 16 pv cells."
                matched = true
            }
            cleaned.contains("west") -> {
                onOrientationChange("West")
                reply = "Orientation turned West to capture rich sunset radiant vectors."
                matched = true
            }
            cleaned.contains("east") -> {
                onOrientationChange("East")
                reply = "Turning East. Setup optimizes early-day spectral coverage."
                matched = true
            }
            cleaned.contains("south") -> {
                onOrientationChange("South")
                reply = "South alignment locked. Securing maximum daily peak solar yield."
                matched = true
            }
            cleaned.contains("monocrystalline") || cleaned.contains("mono") -> {
                onEfficiencyChange("High Performance Monocrystalline")
                reply = "Wafer grade upgraded to high performance Monocrystalline cell arrays."
                matched = true
            }
            cleaned.contains("estimated") || cleaned.contains("power") || cleaned.contains("capacity") || cleaned.contains("how much") -> {
                val estKwp = (currentPanelCount * 415) / 1000.0
                reply = "Current capacity configuration yields an estimated ${"%.2f".format(estKwp)} kilowatts-peak power, securing exceptional green efficiency!"
                matched = true
            }
            cleaned.contains("hello") || cleaned.contains("hi") || cleaned.contains("copilot") -> {
                reply = "Hello there! I am online and listening. Ask me to change the roof style, add panels, or report estimated capacity!"
                matched = true
            }
        }

        if (!matched) {
            reply = "I recognized: '$command'. To update the blueprint, say commands like: 'set flat roof', 'increase panels', or 'orientation west'!"
        }

        assistantResponse = reply
        speakAloud(reply)
    }

    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                spokenText = "Listening color spectrums..."
            }
            override fun onBeginningOfSpeech() {
                spokenText = "Processing audio vibrations..."
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }
            override fun onError(error: Int) {
                isListening = false
                spokenText = ""
                assistantResponse = "Voice input timed out or requires permission. Select a custom command trigger below instead, and I will narrate the response!"
                speakAloud("I am ready. Choose a suggested command path below.")
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val speechResult = matches[0]
                    spokenText = speechResult
                    parseCommand(speechResult)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    // Native Speech To Text listener setup
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    DisposableEffect(context) {
        val isAvailable = try { SpeechRecognizer.isRecognitionAvailable(context) } catch (e: Throwable) { false }
        val recognizer = if (isAvailable) {
            try {
                SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(recognitionListener)
                }
            } catch (e: Throwable) {
                null
            }
        } else {
            null
        }
        speechRecognizer = recognizer
        onDispose {
            try {
                recognizer?.destroy()
            } catch (e: Throwable) {
                // Ignore failure on cleanup
            }
        }
    }

    val recognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F0E13)) // Deep Space Obsidian Background
            .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Futuristic Card Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Hearing,
                    contentDescription = null,
                    tint = Color(0xFFFFCA28),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "COGNITIVE VOICE COPILOT",
                    letterSpacing = 1.sp,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFCA28)
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isListening || isSpeaking) Color(0xFFE53935).copy(alpha = 0.2f) else Color(0xFFFFD54F).copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isListening) "SPEECH IN_SYNC" else if (isSpeaking) "ASSISTANT VOCAL" else "READY_IDLE",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isListening || isSpeaking) Color(0xFFEF5350) else Color(0xFFFFD54F),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large sound-wave visualization sphere
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (isListening) listOf(Color(0xFFE53935).copy(alpha = 0.3f), Color.Transparent)
                        else if (isSpeaking) listOf(Color(0xFF00E676).copy(alpha = 0.3f), Color.Transparent)
                        else listOf(Color(0xFFFFD54F).copy(alpha = 0.12f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Infinite wave pulses
            val infiniteTransition = rememberInfiniteTransition(label = "Waves")
            val scaleFactor by infiniteTransition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(if (isListening || isSpeaking) 700 else 1800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Vibration"
            )

            IconButton(
                onClick = {
                    val recognizer = speechRecognizer
                    if (recognizer == null) {
                        // Safe Sandbox bypass triggers directly if speech packages are absent
                        isListening = true
                        spokenText = "[Virtual Voice Sandbox Active]"
                        isListening = false
                        parseCommand("hello")
                    } else {
                        if (isListening) {
                            try { recognizer.stopListening() } catch (e: Throwable) {}
                            isListening = false
                        } else {
                            try {
                                recognizer.startListening(recognizerIntent)
                            } catch (e: Throwable) {
                                // Fallback triggers if speech packages are absent
                                isListening = true
                                spokenText = "[Virtual Voice Sandbox Active]"
                                isListening = false
                                parseCommand("hello")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .size(68.dp)
                    .scale(if (isListening || isSpeaking) scaleFactor else 1.0f)
                    .background(
                        color = if (isListening) Color(0xFFE53935) else if (isSpeaking) Color(0xFF00C853) else Color(0xFFFFCA28),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice Control",
                    tint = if (isListening || isSpeaking) Color.White else Color(0xFF3E2723),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sound-bars equalizer graphic
        Row(
            modifier = Modifier.height(24.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val count = 9
            val infiniteTransition = rememberInfiniteTransition(label = "Equalizer")
            val sweepFactor by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "sweepFactorAnim"
            )

            for (i in 0 until count) {
                // Generate a pseudo-animated height for each bar using phase offsets
                val wave = kotlin.math.sin((sweepFactor * 2.0 * kotlin.math.PI) + (i * 0.6)).toFloat()
                val heightPercent = if (isListening || isSpeaking) {
                    (0.55f + 0.45f * wave).coerceIn(0.15f, 1.0f)
                } else {
                    (0.25f + 0.1f * kotlin.math.sin(i.toDouble()).toFloat()).coerceIn(0.15f, 0.40f)
                }

                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .fillMaxHeight(heightPercent)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (isListening) Color(0xFFEF5350)
                            else if (isSpeaking) Color(0xFF69F0AE)
                            else Color(0xFFFFD54F).copy(alpha = 0.4f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Spoken subtitle caption banner
        AnimatedVisibility(
            visible = spokenText.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "YOU SPOKE:",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "\"$spokenText\"",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Assistant Caption Response
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "SYSTEM RESPONSE:",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Crossfade(targetState = assistantResponse, label = "ReplyCrossfade") { text ->
                    Text(
                        text = text,
                        fontSize = 13.sp,
                        color = Color.White,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Command Suggestions Header
        Text(
            text = "OR TAP HIGHSPEED COMMAND PRESET:",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Suggested Voice Triggers Grid (Horizontal scroll / Simple compact column list for quick touch emulation)
        LazyColumn(
            modifier = Modifier.height(120.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(suggestions) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .clickable {
                            spokenText = "[Tapped Option: $item]"
                            parseCommand(item)
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SettingsVoice,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F).copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item,
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
