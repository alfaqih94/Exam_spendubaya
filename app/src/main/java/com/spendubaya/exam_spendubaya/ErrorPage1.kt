package com.spendubaya.exam_spendubaya

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ErrorPage1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error_page1)

        // Panggil findViewById setelah setContentView
        val keluarButton = findViewById<Button>(R.id.KeluarButton)

        // Aksi ketika tombol keluar diklik
        keluarButton.setOnClickListener {
            finishAffinity() // Menutup semua aktivitas dan keluar dari aplikasi
        }
    }
}