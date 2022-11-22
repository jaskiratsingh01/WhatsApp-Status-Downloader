package com.example.whatsappstatusdownloader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.example.whatsappstatusdownloader.adapters.ViewPagerAdapter
import com.example.whatsappstatusdownloader.fragments.ImagesFragment
import com.example.whatsappstatusdownloader.fragments.MemesFragment
import com.example.whatsappstatusdownloader.fragments.VideosFragment
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var tabLayout:TabLayout
    private lateinit var viewPager: ViewPager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = this.resources.getColor(R.color.whatsapp)

        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)

        setUpTabs()
    }

    private fun setUpTabs() {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(ImagesFragment(), "Images")
        adapter.addFragment(VideosFragment(), "Videos")
        adapter.addFragment(MemesFragment(), "Memes")
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }
}