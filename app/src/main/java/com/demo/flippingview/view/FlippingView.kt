package com.demo.flippingview.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

/**
 * @Author kuyan
 * @Date 3/16/21-12:53 PM
 * @explain
 */
class FlippingView : View {
    val TAG = "FlippingView"
    private var mWidth = 0 //view宽度
    private var mHeight = 0 //view高度

    //滑动中第一个view，若当前position为1 则向左滑动时为postion为0的视图，向右滑动时为position为1的视图
    private var firstView: View? = null

    //滑动中第二个view，若当前position为1 则向左滑动时为postion为1的视图，向右滑动时为position为2的视图
    private var secendView: View? = null

    //第一个视图对应转化的bitmap
    private var firstBitmap: Bitmap? = null

    //第二个视图对应转化的bitmap
    private var secendBitmap: Bitmap? = null

    //选中position的bitmap
    private var currentBitmap: Bitmap? = null
    private var mAdapter: BaseAdapter? = null

    //滑动中 左边bitmap
    var leftBitmap: Bitmap? = null

    //滑动中 右边bitmap
    var rightBitmap: Bitmap? = null

    //做翻转动画时前半部bitma
    var bitmap1: Bitmap? = null

    //做翻转动画时后半部bitmap
    var bitmap2: Bitmap? = null
    private var itemCount = 0

    //当前position 小于0意味着第一次
    private var currentPosition = -1
    private val camera = Camera()
    var currentChangeListener: CurrentChangeListener? = null

    //手指按下时坐标
    var downX = 0f
    var downY = 0f

    /**
     * 是否正在运动
     */
    private var isAni = false

    //手指按下时时间
    var downTime = 0L

    //动画运动时间
    var durTime: Long = 1500

    //动画开始时间
    var startTime: Long = 0

    //是否是右翻页
    var isNext = false

    constructor(context: Context) : super(context) {}
    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
    }

    fun setAdapter(adapter: BaseAdapter?) {
        camera.setLocation(0f, 0f, -100f)
        mAdapter = adapter
        itemCount = mAdapter!!.count
        if (mWidth > 0) {
            setCurrentPosition(0)
        }
    }

    fun setCurrentPosition(position: Int) {
        if (currentPosition == position) {
            return
        }
        if (isAni) {
            return
        }
        if (currentPosition < 0) {
            val view = mAdapter!!.getView(position, this, null)
            currentBitmap = takeScreenshot(view)
            invalidate()
        } else {
            val isNext: Boolean
            if (currentPosition < position) {
                firstBitmap = currentBitmap
                secendView = mAdapter!!.getView(position, this, null)
                secendBitmap = takeScreenshot(secendView)
                isNext = true
            } else {
                firstView = mAdapter!!.getView(position, this, null)
                firstBitmap = takeScreenshot(firstView)
                secendBitmap = currentBitmap
                isNext = false
            }
            currentPosition = position
            currentBitmap = if (isNext) secendBitmap else firstBitmap
            startAni(isNext)
        }
        currentPosition = position
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        if (mAdapter != null && currentPosition < 0) {
            setCurrentPosition(0)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)

    }


    private fun startAni(isNext: Boolean) {
        if (firstBitmap == null || secendBitmap == null) {
            return
        }
        this.isNext = isNext
        leftBitmap = Bitmap.createBitmap(firstBitmap!!, 0, 0, mWidth / 2, mHeight)
        rightBitmap = Bitmap.createBitmap(secendBitmap!!, mWidth / 2, 0, mWidth / 2, mHeight)
        if (isNext) {
            bitmap1 = Bitmap.createBitmap(firstBitmap!!, mWidth / 2, 0, mWidth / 2, mHeight)
            bitmap2 = Bitmap.createBitmap(secendBitmap!!, 0, 0, mWidth / 2, mHeight)
            //镜像翻转
            bitmap2 = convert(bitmap2, bitmap2!!.getWidth(), bitmap2!!.getHeight())
        } else {
            bitmap1 = Bitmap.createBitmap(secendBitmap!!, 0, 0, mWidth / 2, mHeight)
            //镜像翻转
            bitmap1 = convert(bitmap1, bitmap1!!.getWidth(), bitmap1!!.getHeight())
            bitmap2 = Bitmap.createBitmap(firstBitmap!!, mWidth / 2, 0, mWidth / 2, mHeight)
        }
        isAni = true
        startTime = System.currentTimeMillis()
        invalidate()
    }

    fun convert(a: Bitmap?, width: Int, height: Int): Bitmap {
        val w = a!!.width
        val h = a.height
        val newb =
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) // 创建一个新的和SRC长度宽度一样的位图
        val cv = Canvas(newb)
        val m = Matrix()
        //        m.postScale(1, -1);   //镜像垂直翻转
        m.postScale(-1f, 1f) //镜像水平翻转
        //        m.postRotate(-90);  //旋转-90度
        val new2 = Bitmap.createBitmap(a, 0, 0, w, h, m, true)
        cv.drawBitmap(
            new2, Rect(0, 0, new2.width, new2.height),
            Rect(0, 0, width, height), null
        )
        return newb
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        if (isAni) {
            //计算翻转角度
            val progress = (System.currentTimeMillis() - startTime) * 1f / durTime * 180
            if (progress >= 180) {
                isAni = false
                if (currentBitmap != null) {
                    //解决动画完成闪一下才绘制当前页面
                    canvas.drawBitmap(currentBitmap!!, 0f, 0f, null)
                }
                if (currentChangeListener != null) {
                    currentChangeListener!!.onCurrentChange(currentPosition)
                }
                invalidate()
            } else {
                canvas.translate(mWidth / 2.toFloat(), mHeight / 2.toFloat())
                camera.save()
                canvas.drawBitmap(leftBitmap!!, -mWidth / 2.toFloat(), -mHeight / 2f, null)
                canvas.drawBitmap(rightBitmap!!, 0f, -mHeight / 2f, null)
                if (isNext) {
                    //右翻页
                    camera.rotateY(-progress)
                    camera.applyToCanvas(canvas)
                    canvas.drawBitmap(
                        if (progress < 90) bitmap1!! else bitmap2!!,
                        0f,
                        -mHeight / 2f,
                        null
                    )
                } else {
                    //左翻页
                    camera.rotateY(progress - 180)
                    camera.applyToCanvas(canvas)
                    canvas.drawBitmap(
                        if (progress < 90) bitmap1!! else bitmap2!!,
                        0f,
                        -mHeight / 2f,
                        null
                    )
                }
                camera.restore()
                invalidate()
            }
        } else {
            if (currentBitmap != null) {
                canvas.drawBitmap(currentBitmap!!, 0f, 0f, null)
            }
        }
        canvas.restore()


    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                downTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_UP ->
                if (Math.abs(event.x - downX) > mWidth / 10 && !isAni) {
                    if (event.x > downX) {
                        changePage(false)
                    } else {
                        changePage(true)
                    }
                } else if (System.currentTimeMillis() - downTime < 300) {
                    //点击事件
                }
        }
        return true
    }

    /**
     * view 转化为图片
     */
    fun takeScreenshot(view: View?): Bitmap {
        val config = Bitmap.Config.ARGB_8888
        Log.d(TAG, "mWidth:${mWidth}")
        val bitmap = Bitmap.createBitmap(mWidth, mHeight, config)
        val canvas = Canvas(bitmap)
        view!!.layout(0, 0, mWidth, mHeight)
        view.draw(canvas)
        return bitmap
    }


    public interface CurrentChangeListener {
        fun onCurrentChange(position: Int)
        fun leftEnable(): Boolean
        fun rightEnable(): Boolean
    }

    /**
     * 更改页码
     *
     * @param b 是否是下一页
     */
    fun changePage(b: Boolean) {
        if (isAni) {
            return
        }
        if (!b) {
            if (currentChangeListener == null || !currentChangeListener!!.leftEnable() || currentPosition < 1) {
                return
            }
            setCurrentPosition(currentPosition - 1)
        } else {
            if (currentChangeListener == null || !currentChangeListener!!.rightEnable() || currentPosition >= itemCount - 1) {
                return
            }
            setCurrentPosition(currentPosition + 1)
        }
    }
}