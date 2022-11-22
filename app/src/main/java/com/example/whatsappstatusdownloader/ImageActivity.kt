package com.example.whatsappstatusdownloader

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ImageActivity : AppCompatActivity() {

    private lateinit var statusImage : ImageView
    private lateinit var btShare : Button
    private lateinit var btDownload : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        window.statusBarColor = this.resources.getColor(R.color.whatsapp)

        statusImage = findViewById(R.id.status_image)
        btShare = findViewById(R.id.bt_share_image)
        btDownload = findViewById(R.id.bt_download_image)

        val intent = intent
        val fileUri = intent.getStringExtra("FileUri")
        Glide.with(applicationContext).load(fileUri).into(statusImage)

        btShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).setType("image/*")
            val bitmap = statusImage.drawable.toBitmap()
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(this.contentResolver, bitmap, "tempimage", null)
            val uri = Uri.parse(path)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(intent)
        }

        btDownload.setOnClickListener {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(fileUri))
            val fileName = "${System.currentTimeMillis()}.jpg"
            var fos: OutputStream?=null
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
                contentResolver.also { resolver->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME,fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE,"images/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val imageUri:Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)
                    fos=imageUri?.let{ resolver.openOutputStream(it) }
                }
            }
            else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, fileName)
                fos = FileOutputStream(image)
            }
            fos?.use{
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,it)
                Toast.makeText(applicationContext,"Image Saved", Toast.LENGTH_SHORT).show()
            }
        }
    }
}