package com.spendubaya.exam_spendubaya

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class LoginPage : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName
    private val OVERLAY_PERMISSION_REQUEST_CODE = 1001
    private fun requestDeviceAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Aplikasi ini memerlukan izin Device Admin untuk keamanan.")
        startActivity(intent)
    }
    object OverlayDetection {
        fun detectOverlayApps(context: Context) {
            if (Settings.canDrawOverlays(context)) {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val runningTasks = activityManager.runningAppProcesses

                for (task in runningTasks) {
                    if (task.processName.contains("flapps")) { // Deteksi Floating Apps
                        Toast.makeText(context, "Aplikasi overlay terdeteksi! Harap matikan.", Toast.LENGTH_LONG).show()

                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        break
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            checkPermissionsAndConditions()
        }
    }

    private fun checkPermissionsAndConditions() {
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, DeviceAdminReceiver::class.java)

        if (!devicePolicyManager.isAdminActive(componentName)) {
            Toast.makeText(this, "Aktifkan Device Admin terlebih dahulu!", Toast.LENGTH_LONG).show()
            requestDeviceAdmin()
            return
        }
        // 2.1 Cek izin overlay
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return
        }

        // 2.2 Cek Bluetooth aktif
        val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter?.isEnabled == true) {
            startActivity(Intent(this, ErrorPage1::class.java))
            return
        }

        val blockedPackages = listOf(
            "com.lwi.android.flapps",  // Floating Apps
            "com.mercandalli.android.apps.bubble",  // Tambahkan package lain di sini
            "com.applay.overlay",
            "com.fossor.panels",
            "floatbrowser.floating.browser.float.web.window" // Tambahkan lebih banyak jika perlu
        )
        // 2.4 Cek aplikasi dengan package com.lwi.android.flapps berjalan
        for (packageName in blockedPackages) {
            if (isPackageRunning(packageName)) {
                startActivity(Intent(this, ErrorPage2::class.java))
                return
            }
        }

        // Jika tidak ada error, lanjut ke MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
    }


    private fun isPackageRunning(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true  // Aplikasi terinstall
        } catch (e: PackageManager.NameNotFoundException) {
            false  // Aplikasi tidak terinstall
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            checkPermissionsAndConditions()
        }
    }
}

