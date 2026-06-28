package com.example.openplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.openplayer.ui.navigation.OpenPlayerNavHost
import com.example.openplayer.ui.rememberLibraryViewModel
import com.example.openplayer.ui.theme.OpenPlayerTheme
import com.example.openplayer.util.Permissions

class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val granted = results.values.all { it }
            pendingPermissionCallback?.invoke(granted)
            pendingPermissionCallback = null
        }

    private var pendingPermissionCallback: ((Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenPlayerTheme {
                val libraryViewModel = rememberLibraryViewModel()
                var permissionsRequested by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    if (!permissionsRequested) {
                        permissionsRequested = true
                        requestAudioPermissions { granted ->
                            libraryViewModel.onPermissionResult(granted)
                        }
                    }
                }

                OpenPlayerNavHost()
            }
        }
    }

    private fun requestAudioPermissions(onResult: (Boolean) -> Unit) {
        if (Permissions.hasAudioPermission(this)) {
            onResult(true)
            return
        }

        val audioPermissions = Permissions.requiredAudioPermissions()
        if (audioPermissions.isEmpty()) {
            onResult(true)
            return
        }

        val notificationPermission = Permissions.notificationPermission()
        val permissionsToRequest = buildList {
            addAll(audioPermissions)
            if (notificationPermission != null && !Permissions.hasNotificationPermission(this@MainActivity)) {
                add(notificationPermission)
            }
        }

        pendingPermissionCallback = onResult
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }
}
