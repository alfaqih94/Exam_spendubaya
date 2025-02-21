package com.spendubaya.exam_spendubaya

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.*
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.*



class MainActivity : Activity() {


    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName
    private lateinit var webView: WebView
    private lateinit var batteryStatus: TextView
    private lateinit var timeStatus: TextView
    private lateinit var buttonContainer: LinearLayout
    private lateinit var maximizeButton: ImageButton
    private lateinit var mediaPlayer: MediaPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var overlayBlocker = OverlayBlocker(this)
        overlayBlocker.startOverlay()

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, DeviceAdminReceiver::class.java)

        if (!devicePolicyManager.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Aktifkan untuk Kiosk Mode")
            }
            startActivity(intent)
        } else {
            startLockTask()
        }
        startService(Intent(this, AppBlockerService::class.java))
        blockFloatingApp("com.lwi.android.flapps")
        killApp("com.lwi.android.flapps")


        webView = findViewById(R.id.webView)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://sites.google.com/view/examspendubaya")

        buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(10, 10, 10, 10)
            visibility = LinearLayout.VISIBLE
        }
        val rootView = findViewById<FrameLayout>(android.R.id.content)
        rootView.addView(buttonContainer)

        addNavigationButtons()
        addBatteryAndTimeDisplay()
        addMinimizeButton()
        addExitButton()
        addMaximizeButton()
    }

    private fun blockFloatingApp(packageName: String) {
        if (devicePolicyManager.isAdminActive(componentName)) {
            try {
                devicePolicyManager.setApplicationHidden(componentName, packageName, true)
                Toast.makeText(this, "Berhasil memblokir $packageName", Toast.LENGTH_SHORT).show()
                val overlayView = OverlayView(this, packageName)
                overlayView.show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error blocking app: ${e.message}")
                Toast.makeText(this, "Gagal memblokir $packageName: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Device admin is not active", Toast.LENGTH_SHORT).show()
        }
    }
    private fun killApp(packageName: String) {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.killBackgroundProcesses(packageName)
    }



    private fun getExitCode(): String {
        val dateFormat = SimpleDateFormat("ddMMyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun showExitDialog() {
        val input = EditText(this).apply {
            hint = "Masukkan Kode"
            setTextColor(Color.RED)
            textSize = 40f
            setHintTextColor(Color.DKGRAY)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER // Hanya angka
            setTypeface(typeface, android.graphics.Typeface.BOLD) // Teks tebal
            setPadding(50, 40, 50, 40)
            setBackgroundResource(android.R.drawable.editbox_background)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 80, 40)
            setBackgroundColor(Color.WHITE)
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Keluar")
            .setMessage("Masukkan kode unik untuk keluar")
            .setView(layout)
            .setPositiveButton("KELUAR") { _, _ ->
                if (input.text.toString() == getExitCode()) {
                    stopLockTask()
                    finishAffinity()
                } else {
                    Toast.makeText(this, "Kode salah!", Toast.LENGTH_SHORT).show()
                }
            }
            .setIcon(R.drawable.exit_icon) // Tambahkan ikon untuk tampilan menarik
            .show()
    }


    private fun addNavigationButtons() {
        val buttons = listOf(Pair(R.drawable.back_icon) { webView.goBack() }, Pair(R.drawable.next_icon) { webView.goForward() }, Pair(R.drawable.refresh_icon) { webView.reload() })

        for ((icon, action) in buttons) {
            val button = ImageButton(this).apply {
                setImageResource(icon)
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener { action() }
            }
            buttonContainer.addView(button)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        playExitSound()
    }

    private fun playExitSound() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)

        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound)
        mediaPlayer.setOnCompletionListener {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
        }
        mediaPlayer.start()
    }
    private fun addBatteryAndTimeDisplay() {
        val statusLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(20,20,20,20)
        }

        val batteryLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val batteryIcon = ImageView(this).apply {
            setImageResource(R.drawable.battery_icon)
        }
        batteryStatus = TextView(this).apply {
            setTextColor(Color.DKGRAY)
            textSize = 14f
        }

        val timeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val timeIcon = ImageView(this).apply {
            setImageResource(R.drawable.clock_icon)
        }
        timeStatus = TextView(this).apply {
            setTextColor(Color.DKGRAY)
            textSize = 14f
        }

        batteryLayout.addView(batteryIcon)
        batteryLayout.addView(batteryStatus)
        timeLayout.addView(timeIcon)
        timeLayout.addView(timeStatus)

        statusLayout.addView(batteryLayout)
        statusLayout.addView(timeLayout)
        buttonContainer.addView(statusLayout)
        updateBatteryAndTime()
    }

    private fun updateBatteryAndTime() {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        batteryStatus.text = "$level%"

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeStatus.text = timeFormat.format(Date())

        Handler(Looper.getMainLooper()).postDelayed({ updateBatteryAndTime() }, 60000)
    }

    private fun addMinimizeButton() {
        val minimizeButton = ImageButton(this).apply {
            setImageResource(R.drawable.minimize_icon)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                buttonContainer.visibility = LinearLayout.GONE
                maximizeButton.visibility = ImageButton.VISIBLE
            }
        }
        buttonContainer.addView(minimizeButton)
    }

    private fun addExitButton() {
        val exitButton = ImageButton(this).apply {
            setImageResource(R.drawable.exit_icon)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { showExitDialog() }
        }
        buttonContainer.addView(exitButton)
    }

    private fun addMaximizeButton() {
        maximizeButton = ImageButton(this).apply {
            setImageResource(R.drawable.maximize_icon)
            setBackgroundColor(Color.TRANSPARENT)
            visibility = ImageButton.GONE
            setOnClickListener {
                buttonContainer.visibility = LinearLayout.VISIBLE
                buttonContainer.setBackgroundColor(Color.TRANSPARENT) // Ubah agar transparan, bukan buram
                this.visibility = ImageButton.GONE
            }

        }
        val layoutParams = FrameLayout.LayoutParams(120, 120).apply {
            gravity = Gravity.TOP or Gravity.END
            marginEnd = 40
            topMargin = 40
        }
        val rootView = findViewById<FrameLayout>(android.R.id.content)
        rootView.addView(maximizeButton, layoutParams)
    }
}