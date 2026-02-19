package com.example.tablettaskoverlay.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object PermissionHelper {
    fun requiresNotificationPermission(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun notificationPermission(): String = Manifest.permission.POST_NOTIFICATIONS

    fun micPermission(): String = Manifest.permission.RECORD_AUDIO

    fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun overlayPermissionIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
    }
}
