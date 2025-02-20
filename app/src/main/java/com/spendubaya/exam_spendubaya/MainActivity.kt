package com.spendubaya.exam_spendubaya

import android.app.Activity
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.EditText
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName
    private val exitCode = "Alfaqih94" // Kode keluar aplikasi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup Device Policy Manager
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, DeviceAdminReceiver::class.java)

        // Cek jika aplikasi memiliki Device Admin
        if (!devicePolicyManager.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Aktifkan untuk Kiosk Mode")
            }
            startActivity(intent)
        } else {
            startLockTask() // Aktifkan Screen Pinning
        }

        // Konfigurasi WebView
        val webView = findViewById<WebView>(R.id.webView)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://sites.google.com/guru.smp.belajar.id/abd-faqih/home")

        // Tambahkan tombol Exit dengan ikon "X"
        addExitButton()
    }

    // Fungsi untuk menambahkan tombol Exit dengan ikon X
    private fun addExitButton() {
        val exitButton = ImageButton(this)
        exitButton.setImageResource(R.drawable.exit_icon) // Ikon X
        exitButton.setBackgroundColor(Color.argb(80, 0, 0, 0)) // Transparan hitam samar
        exitButton.layoutParams = FrameLayout.LayoutParams(100, 100) // Ukuran lebih besar

        // Posisi tombol di pojok kanan atas
        val params = FrameLayout.LayoutParams(
            100, // Lebar tombol
            100, // Tinggi tombol
            Gravity.TOP or Gravity.END // Pojok kanan atas
        )
        params.setMargins(20, 20, 20, 20) // Margin dari tepi layar

        // Menambahkan tombol ke root layout
        val rootView = findViewById<FrameLayout>(R.id.rootLayout)
        rootView.addView(exitButton, params)

        // Event klik tombol Exit
        exitButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                showExitDialog()
            }
            true
        }
    }

    // Menampilkan Dialog untuk memasukkan kode keluar
    private fun showExitDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Apakah yakin ingin keluar?")

        val input = EditText(this)
        input.hint = "Masukkan kode Alfaqih94"
        builder.setView(input)

        builder.setPositiveButton("KELUAR") { _, _ ->
            val enteredCode = input.text.toString()
            if (enteredCode == exitCode) {
                stopLockTask() // Keluar dari Screen Pinning
                finishAffinity() // Tutup aplikasi sepenuhnya
            } else {
                Toast.makeText(this, "Kode yang dimasukkan salah!", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("BATAL") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
}