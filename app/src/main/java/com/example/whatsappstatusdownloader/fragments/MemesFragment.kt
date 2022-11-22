package com.example.whatsappstatusdownloader.fragments

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.whatsappstatusdownloader.R
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MemesFragment : Fragment() {

    private lateinit var btShare: Button
    private lateinit var btDownload : Button
    private lateinit var btNext: Button
    private lateinit var ivMeme: ImageView
    private lateinit var progressBar : ProgressBar
    private lateinit var currentImageUrl:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view:View =  inflater.inflate(R.layout.fragment_memes, container, false)

        btShare = view.findViewById(R.id.bt_share_meme)
        btDownload = view.findViewById(R.id.bt_download_meme)
        btNext = view.findViewById(R.id.bt_next_meme)
        ivMeme = view.findViewById(R.id.iv_meme)
        progressBar = view.findViewById(R.id.progBar)

        loadMeme()

        btNext.setOnClickListener {
            loadMeme()
        }

        btShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT,"Hey checkout this cool meme $currentImageUrl")
            val chooser = Intent.createChooser(intent,"Share this meme using...")
            startActivity(chooser)
        }

        btDownload.setOnClickListener {
//            val bitmap = MediaStore.Images.Media.getBitmap(this.activity?.contentResolver, Uri.parse(currentImageUrl.toUri().toString()))

            val bitmap = ivMeme.drawable.toBitmap()
            val fileName = "${System.currentTimeMillis()}.jpg"
            var fos: OutputStream?=null
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
                this.activity?.contentResolver.also { resolver->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME,fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE,"images/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val imageUri: Uri? = resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)
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
                Toast.makeText(this.activity?.applicationContext,"Image Saved", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun loadMeme() {
        progressBar.visibility = View.VISIBLE

        val url = "https://meme-api.herokuapp.com/gimme"

        // request a string response from the provided URL
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                currentImageUrl = response.getString("url")
                activity?.let {
                    Glide.with(it.applicationContext).load(currentImageUrl).listener(object: RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.visibility=View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.visibility=View.GONE
                            return false
                        }
                    }).into(ivMeme)
                }
            },
            {
                Toast.makeText(this.context,"Error occurred", Toast.LENGTH_SHORT).show()
            })

        // add the request to the RequestQueue
        activity?.let { MySingletonForMeme.getInstance(it).addToRequestQueue(jsonObjectRequest) }
    }
}