package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.AudioRecorder
import com.example.ui.Dashboard
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val audioRecorder by lazy { AudioRecorder(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var onPermissionGrantedCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        onPermissionGrantedCallback?.invoke()
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Dashboard(
                        audioRecorder = audioRecorder,
                        onRequestRecordPermission = { onGranted ->
                            onPermissionGrantedCallback = onGranted
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
