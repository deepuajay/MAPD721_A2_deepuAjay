package com.deepu.mapd721_a2_deepuajay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HealthDataScreen(
    onSaveData: (String, String) -> Unit,
    onLoadData: () -> Unit,
    heartRateRecords: List<Pair<String, String>>
) {
    var heartRateInput by remember { mutableStateOf("") }
    var dateTimeInput by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp)
        ) {
            OutlinedTextField(
                value = heartRateInput,
                onValueChange = {
                    heartRateInput = it
                    validationMessage = if (it.toIntOrNull() !in 1..300) "Heart rate must be between 1 and 300 bpm" else ""
                },
                label = { Text("Heart Rate (1-300 bpm)") },
                modifier = Modifier.fillMaxWidth(),
                isError = validationMessage.isNotEmpty()
            )
            if (validationMessage.isNotEmpty()) {
                Text(validationMessage, color = Color.Red, style = TextStyle(fontSize = 14.sp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = dateTimeInput,
                onValueChange = { dateTimeInput = it },
                label = { Text("Date/Time (yyyy-MM-dd HH:mm)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onLoadData,
                    modifier = Modifier.padding(8.dp).height(50.dp)
                ) {
                    Text("Load", fontSize = 18.sp)
                }
                Button(
                    onClick = {
                        if (validationMessage.isEmpty()) {
                            onSaveData(heartRateInput, dateTimeInput)
                        }
                    },
                    modifier = Modifier.padding(8.dp).height(50.dp)
                ) {
                    Text("Save", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Heart Rate History", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))

            Box(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(heartRateRecords) { record ->
                        Text(
                            "${record.second} - ${record.first}",
                            style = TextStyle(fontSize = 16.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Student Info",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Name: Deepu Ajay", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text("ID: 301494114", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}
