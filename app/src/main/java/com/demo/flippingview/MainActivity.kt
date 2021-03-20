package com.demo.flippingview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.BaseAdapter
import com.demo.flippingview.adapter.FlippingAdapter
import com.demo.flippingview.view.FlippingView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        this.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        flippingView.currentChangeListener = object : FlippingView.CurrentChangeListener {
            override fun onCurrentChange(position: Int) {
                //当前选中position
            }

            override fun leftEnable(): Boolean {
                //是否可以左滑
                return true
            }

            override fun rightEnable(): Boolean {
                //是否可以右划
                return true
            }
        }
        flippingView.setAdapter(FlippingAdapter(this))

    }
}