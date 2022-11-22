package com.example.whatsappstatusdownloader

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import com.bumptech.glide.Glide
import java.io.IOException
import java.io.OutputStream

class VideoActivity : AppCompatActivity() {

    private lateinit var statusVideo : VideoView
    private lateinit var btShare : Button
    private lateinit var btDownload : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        window.statusBarColor = this.resources.getColor(R.color.whatsapp)

        statusVideo = findViewById(R.id.status_video)
        btShare = findViewById(R.id.bt_share_video)
        btDownload = findViewById(R.id.bt_download_video)

        val intent = intent
        val fileUri = intent.getStringExtra("FileUri")

        val mediaController = MediaController(this)
        mediaController.setAnchorView(statusVideo)
        statusVideo.setMediaController(mediaController)
        statusVideo.setVideoURI(Uri.parse(fileUri))
        statusVideo.requestFocus()
        statusVideo.start()

        btShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).setType("video/*")
            intent.putExtra(Intent.EXTRA_STREAM, fileUri)
            startActivity(Intent.createChooser(intent, "Share Video"))
        }

        btDownload.setOnClickListener {
            val inputStream = contentResolver.openInputStream(Uri.parse(fileUri))
            val fileName = "${System.currentTimeMillis()}.mp4"
            try {
                val values = ContentValues()
                values.put(MediaStore.MediaColumns.DISPLAY_NAME,fileName)
                values.put(MediaStore.MediaColumns.MIME_TYPE,"video/mp4")
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS+"/Videos/")
                val uri = contentResolver.insert(
                    MediaStore.Files.getContentUri("external"),values
                )
                val outputStream: OutputStream = uri?.let { contentResolver.openOutputStream(it) }!!
                if(inputStream != null) {
                    outputStream.write(inputStream.readBytes())
                }
                outputStream.close()
                Toast.makeText(applicationContext,"Video Saved", Toast.LENGTH_SHORT).show()
            }
            catch (e: IOException) {
                Toast.makeText(applicationContext,"Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}