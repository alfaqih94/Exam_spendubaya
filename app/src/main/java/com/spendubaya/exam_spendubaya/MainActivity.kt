package com.spendubaya.exam_spendubaya

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.*
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.view.Gravity
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*
import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


class MainActivity : Activity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName
    private lateinit var webView: WebView
    private lateinit var batteryStatus: TextView
    private lateinit var timeStatus: TextView
    private lateinit var buttonContainer: LinearLayout
    private lateinit var maximizeButton: ImageButton
    private lateinit var mediaPlayer: MediaPlayer
    private val hideHandler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hideSystemUI() }


    // Di MainActivity
    private fun disableKeyguard() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Untuk Android 8.0+ (API 26)
            keyguardManager.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() {
                    // Keyguard berhasil dinonaktifkan
                }

                override fun onDismissCancelled() {
                    // Handle jika user membatalkan
                }
            })
        } else {
            // Untuk Android 5.0+ (API 21)
            @Suppress("DEPRECATION")
            keyguardManager.newKeyguardLock("MyApp").disableKeyguard()
        }
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

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Mencegah recent apps dan screenshot

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Sembunyikan tombol navigasi dan status bar
        hideSystemUI()

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, DeviceAdminReceiver::class.java)
        OverlayDetection.detectOverlayApps(this)

        if (!devicePolicyManager.isAdminActive(componentName)) {
            blockAssistantApps()
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Aktifkan untuk Kiosk Mode")
            }
            startActivity(intent)
        } else {
            startLockTask()
        }

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
    override fun onResume() {
        super.onResume()
        hideHandler.postDelayed(hideRunnable, 100)
        if (devicePolicyManager.isAdminActive(componentName)) {
            startLockTask() // Pastikan mode terkunci tetap berjalan
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_APP_SWITCH -> true
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
    override fun onKeyMultiple(keyCode: Int, repeatCount: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_MULTIPLE) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK,
                KeyEvent.KEYCODE_APP_SWITCH -> return true
            }
        }
        return super.onKeyMultiple(keyCode, repeatCount, event)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    @SuppressLint("WrongConstant")
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Untuk Android 11 (API 30) ke atas
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // Untuk Android 10 (API 29) ke bawah
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                            View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
            startLockTask()
        } else {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
    private fun blockAssistantApps() {
        val blockedPackages = arrayOf(
            "com.samsung.android.bixby.agent", // Samsung Bixby
            "com.google.android.apps.googleassistant", // Google Assistant
            "com.miui.voiceassist", // Xiaomi Assistant
            "com.huawei.vassistant", // Huawei Celia
            "com.coloros.voiceassistant", // Oppo Breeno
            "com.vivo.assistant" // Vivo Jovi
        )

        for (packageName in blockedPackages) {
            devicePolicyManager.setApplicationHidden(componentName, packageName, true)
        }
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

    @SuppressLint("SetTextI18n")
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