package com.spendubaya.exam_spendubaya

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

object OverlayDetection {
    private val knownOverlayApps = listOf(
        "com.facebook.orca",   // Messenger
        "com.android.systemui", // System UI overlay
        "com.whatsapp", // WhatsApp overlay
        "com.lwi.android.flapps",
        "com.android.chrome",
        "com.miui.freeform"
    )

    fun detectOverlayApps(context: Context) {
        val packageManager = context.packageManager

        for (packageName in knownOverlayApps) {
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)

                if (Settings.canDrawOverlays(context)) {
                    Log.w("Overlay Detected", "Aplikasi overlay terdeteksi: $packageName")
                    Toast.makeText(context, "Aplikasi overlay aktif: $packageName", Toast.LENGTH_LONG).show()

                    // Opsional: Buka pengaturan izin overlay
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    context.startActivity(intent)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("Overlay Check", "Aplikasi tidak ditemukan: $packageName")
            }
        }
    }
}
