package com.foxymacscan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foxymacscan.app.ui.theme.FoxyMacScanTheme
import com.foxymacscan.app.scanner.MacScanner

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoxyMacScanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MacScanScreen()
                }
            }
        }
    }
}

@Composable
fun MacScanScreen() {
    val macScanner = remember { MacScanner() }
    var panelUrl by remember { mutableStateOf("") }
    var macPrefix by remember { mutableStateOf("00:1A:79:") }
    var botCount by remember { mutableStateOf("2") }
    var isScanning by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<String>>(emptyList()) }
    var progress by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Header
        Text(
            text = "🅕🅞🅧🅨 MAC SCAN Pro V3.9",
            fontSize = 24.sp,
            color = Color(0xFFFFD700),
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Panel URL Input
        OutlinedTextField(
            value = panelUrl,
            onValueChange = { panelUrl = it },
            label = { Text("Panel URL", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                unfocusedBorderColor = Color.Gray
            ),
            enabled = !isScanning
        )

        // MAC Prefix Input
        OutlinedTextField(
            value = macPrefix,
            onValueChange = { macPrefix = it },
            label = { Text("MAC Prefix", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                unfocusedBorderColor = Color.Gray
            ),
            enabled = !isScanning
        )

        // Bot Count Input
        OutlinedTextField(
            value = botCount,
            onValueChange = { botCount = it },
            label = { Text("Bot Count (1-15)", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                unfocusedBorderColor = Color.Gray
            ),
            enabled = !isScanning
        )

        // Start/Stop Button
        Button(
            onClick = {
                if (!isScanning) {
                    isScanning = true
                    results = emptyList()
                    // Start scanning in background
                    macScanner.startScan(
                        panelUrl = panelUrl,
                        macPrefix = macPrefix,
                        botCount = botCount.toIntOrNull() ?: 2,
                        onProgress = { progress = it },
                        onResult = { result ->
                            results = results + result
                        },
                        onComplete = { isScanning = false }
                    )
                } else {
                    isScanning = false
                    macScanner.stopScan()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isScanning) Color.Red else Color(0xFF00CC00)
            )
        ) {
            Text(if (isScanning) "STOP SCANNING" else "START SCAN", color = Color.Black)
        }

        // Progress Bar
        if (isScanning) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = Color(0xFF00CC00),
                trackColor = Color.Gray
            )
        }

        // Results Display
        Text(
            text = "RESULTS (${results.size})",
            fontSize = 14.sp,
            color = Color(0xFFFFD700),
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0a0a0a))
                .padding(8.dp)
        ) {
            items(results) { result ->
                Text(
                    text = result,
                    fontSize = 10.sp,
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
    }
}
