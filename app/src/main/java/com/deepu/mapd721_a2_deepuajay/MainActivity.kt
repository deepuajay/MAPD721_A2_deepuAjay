package com.deepu.mapd721_a2_deepuajay

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class MainActivity : ComponentActivity() {
    private lateinit var healthClient: HealthConnectClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        healthClient = HealthConnectClient.getOrCreate(this)

        val permissionRequestLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val permissionsGranted = permissions.values.all { it }
            if (permissionsGranted) {
                Log.d("HealthConnect", "Permissions granted.")
            } else {
                Log.e("HealthConnect", "Permissions denied.")
            }
        }

        setContent {
            MaterialTheme {
                var heartRateRecords by remember { mutableStateOf(listOf<Pair<String, String>>()) }

                LaunchedEffect(Unit) {
                    requestPermissions(permissionRequestLauncher)
                }

                HealthDataScreen(
                    onSaveData = { heartRate, dateTime ->
                        lifecycleScope.launch {
                            storeHeartRate(heartRate.toLong(), dateTime)
                        }
                    },
                    onLoadData = {
                        lifecycleScope.launch {
                            heartRateRecords = fetchHeartRates()
                        }
                    },
                    heartRateRecords = heartRateRecords
                )
            }
        }
    }

    private fun requestPermissions(permissionRequestLauncher: ActivityResultLauncher<Array<String>>) {
        val requiredPermissions = arrayOf(
            HealthPermission.getReadPermission(HeartRateRecord::class).toString(),
            HealthPermission.getWritePermission(HeartRateRecord::class).toString()
        )
        permissionRequestLauncher.launch(requiredPermissions)
    }

    private suspend fun storeHeartRate(heartRate: Long, dateTime: String) {
        try {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val localDateTime = java.time.LocalDateTime.parse(dateTime, formatter)

            val zonedTime = localDateTime.atZone(java.time.ZoneId.systemDefault())

            val heartRateEntry = HeartRateRecord(
                startTime = zonedTime.toInstant(),
                startZoneOffset = zonedTime.offset,
                endTime = zonedTime.toInstant(),
                endZoneOffset = zonedTime.offset,
                samples = listOf(HeartRateRecord.Sample(zonedTime.toInstant(), heartRate))
            )

            healthClient.insertRecords(listOf(heartRateEntry))
            Log.d("HealthConnect", "Heart rate recorded successfully.")
        } catch (e: Exception) {
            Log.e("HealthConnect", "Failed to record heart rate: ${e.message}")
        }
    }

    private suspend fun fetchHeartRates(): List<Pair<String, String>> {
        return try {
            val result = healthClient.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.before(Instant.now().plusSeconds(1))
                )
            )
            result.records.map { entry ->
                val formattedTime =
                    ZonedDateTime.ofInstant(entry.startTime, ZoneId.systemDefault())
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                Pair(formattedTime, "${entry.samples.first().beatsPerMinute} bpm")
            }
        } catch (e: Exception) {
            Log.e("HealthConnect", "Failed to load heart rate records: ${e.message}")
            emptyList()
        }
    }
}
