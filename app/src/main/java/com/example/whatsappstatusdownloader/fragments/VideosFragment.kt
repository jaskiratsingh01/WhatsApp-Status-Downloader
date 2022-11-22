package com.example.whatsappstatusdownloader.fragments

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.storage.StorageManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.whatsappstatusdownloader.ImageActivity
import com.example.whatsappstatusdownloader.ModelClass
import com.example.whatsappstatusdownloader.R
import com.example.whatsappstatusdownloader.VideoActivity
import com.example.whatsappstatusdownloader.adapters.StatusImagesAdapter
import com.example.whatsappstatusdownloader.adapters.StatusVideosAdapter

class VideosFragment : Fragment() {

    private lateinit var rvStatusVidoes: RecyclerView
    private lateinit var statusList: ArrayList<ModelClass>
    private lateinit var statusVideosAdapter: StatusVideosAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view:View = inflater.inflate(R.layout.fragment_videos, container, false)

        rvStatusVidoes = view.findViewById(R.id.rv_status_videos)
        statusList = ArrayList()
        swipeRefreshLayout = view.findViewById(R.id.swipe_video_fragment)

        setupLayout()

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            setupLayout()
            Handler().postDelayed(Runnable{
                swipeRefreshLayout.isRefreshing = false
            }, 1000)
        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setupLayout() {
        val result = readDataFromPrefs()
        if (result) {
            val sh = activity?.getSharedPreferences("DATA_PATH",MODE_PRIVATE)
            val uriPath = sh?.getString("PATH","")

            activity?.contentResolver?.takePersistableUriPermission(Uri.parse(uriPath), Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if(uriPath != null) {
                val fileDoc = activity?.applicationContext?.let { DocumentFile.fromTreeUri(it, Uri.parse(uriPath)) }
                statusList.clear()
                for(file: DocumentFile in fileDoc!!.listFiles()) {
                    if(!file.name!!.endsWith(".nomedia") && file.name!!.endsWith(".mp4")) {
                        val modelClass = ModelClass(file.name!!, file.uri.toString())
                        statusList.add(modelClass)
                    }
                }
                setUpRecyclerView(statusList)
            }
        }
        else {
            getFolderPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getFolderPermission() {
        val storageManager = activity?.applicationContext?.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
        val targetDirectory = "Android%2Fmedia%2Fcom.whatsapp%2FMedia%2F.Statuses"
        var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI") as Uri
        var scheme = uri.toString()
        scheme = scheme.replace("/root/","/document/")
        scheme+= "%3A$targetDirectory"
        uri = Uri.parse(scheme)
        intent.putExtra("android.content.extra.SHOW_ADVANCED",true)
        intent.putExtra("android.provider.extra.INITIAL_URI",uri)
        startActivityForResult(intent,1234)
    }

    private fun setUpRecyclerView(statusList: ArrayList<ModelClass>) {
        statusVideosAdapter = activity?.applicationContext?.let {
            StatusVideosAdapter(
                it,statusList
            ){ selectedStatusItem:ModelClass->listItemClicked(selectedStatusItem)
            }
        }!!
        rvStatusVidoes.apply {
            setHasFixedSize(true)
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            adapter = statusVideosAdapter
        }
    }

    private fun listItemClicked(status: ModelClass) {
        val intent = Intent(activity?.applicationContext, VideoActivity::class.java)
        intent.putExtra("FileUri",status.fileUri)
        startActivity(intent)
    }

    private fun readDataFromPrefs(): Boolean {
        val sh = activity?.getSharedPreferences("DATA_PATH", MODE_PRIVATE)
        val uriPath = sh?.getString("PATH","")
        if (uriPath != null) {
            return uriPath.isNotEmpty()
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == AppCompatActivity.RESULT_OK) {
            val treeUri = data?.data

            val sharedPreferences = activity?.getSharedPreferences("DATA_PATH", MODE_PRIVATE)
            val myEdit = sharedPreferences?.edit()
            myEdit?.putString("PATH",treeUri.toString())
            myEdit?.apply()

            if(treeUri != null) {
                activity?.contentResolver?.takePersistableUriPermission(treeUri,Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val fileDoc = DocumentFile.fromTreeUri(requireActivity().applicationContext,treeUri)
                for(file:DocumentFile in fileDoc!!.listFiles()) {
                    if(!file.name!!.endsWith(".nomedia") && file.name!!.endsWith(".mp4")) {
                        val modelClass = ModelClass(file.name!!, file.uri.toString())
                        statusList.add(modelClass)
                    }
                }
                setUpRecyclerView(statusList)
            }
        }
    }

}