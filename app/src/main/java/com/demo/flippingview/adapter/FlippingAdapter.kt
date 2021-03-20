package com.demo.flippingview.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.demo.flippingview.R

/**
 * @Author kuyan
 * @Date 3/20/21-11:01 AM
 * @explain
 */
class FlippingAdapter(val context: Context) : BaseAdapter() {
    override fun getView(position: Int, p1: View?, p2: ViewGroup?): View {
        var imageId = 0
        when (position % 3) {
            0 -> {
                imageId = R.mipmap.img_1
            }
            1 -> {
                imageId = R.mipmap.img_2

            }
            2 -> {
                imageId = R.mipmap.img_3

            }
        }
        val imageView = ImageView(context)
        imageView.setImageDrawable(context.resources.getDrawable(imageId))
        return imageView

    }

    override fun getItem(p0: Int): Any {
        return p0
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return 10
    }
}