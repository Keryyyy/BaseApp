package com.hy.baseapp.common.view


import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import com.hy.baseapp.R
import com.hy.baseapp.common.extension.getResColor


/**
 * <pre>
 *
 *     author: Hy
 *     time  : 2020/10/28
 *     desc  : 仿IOS开关按钮
 *
 * </pre>
 */
class IOSToggleButton @JvmOverloads constructor(context: Context,
                                                attrs: AttributeSet? = null,
                                                defStyleAttr: Int = androidx.appcompat.R.attr.checkboxStyle)
    : androidx.appcompat.widget.AppCompatCheckBox(context,attrs,defStyleAttr) {


    /**
     * 控件默认宽度
     */
    private val DEFAULT_WIDTH = 200

    /**
     * 控件默认高度
     */
    private val DEFAULT_HEIGHT = DEFAULT_WIDTH / 8 * 5

    /**
     * 画笔
     */
    private lateinit var mPaint: Paint

    /**
     * 控件背景的矩形范围
     */
    private lateinit var mRectF: RectF

    /**
     * 开关指示器按钮圆心 X 坐标的偏移量
     */
    private var mButtonCenterXOffset = 0f

    /**
     * 颜色渐变系数
     */
    private var mColorGradientFactor = 1f

    /**
     * 状态切换时的动画时长
     */
    private var mAnimateDuration = 200L

    /**
     * 开关未选中状态,即关闭状态时的背景颜色
     */
    private var mBackgroundColorUnchecked =
        getResColor(R.color.gray_e1)

    /**
     * 开关选中状态,即打开状态时的背景颜色
     */
    private var mBackgroundColorChecked =
        getResColor(R.color.main_color)

    /**
     * 开关指示器按钮的颜色
     */
    private var mButtonColor = -0x1


    init {
        // 不显示 CheckBox 默认的 Button
        buttonDrawable = null
        // 不显示 CheckBox 默认的背景
        setBackgroundResource(0)
        // 默认 CheckBox 为关闭状态
        isChecked = false
        mPaint = Paint()
        mPaint.setAntiAlias(true)
        mRectF = RectF()
        // 点击时开始动画
        setOnClickListener { startAnimate() }
    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width: Int
        val height: Int
        width = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else {
            paddingLeft + DEFAULT_WIDTH + paddingRight
        }
        height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            paddingTop + DEFAULT_HEIGHT + paddingBottom
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 设置画笔宽度为控件宽度的 1/40,准备绘制控件背景
        mPaint.strokeWidth = measuredWidth.toFloat() / 40
        // 根据是否选中的状态设置画笔颜色
        if (isChecked) {
            // 选中状态时,背景颜色由未选中状态的背景颜色逐渐过渡到选中状态的背景颜色
            mPaint.setColor(
                getCurrentColor(
                    mColorGradientFactor,
                    mBackgroundColorUnchecked,
                    mBackgroundColorChecked
                )
            )
        } else {
            // 未选中状态时,背景颜色由选中状态的背景颜色逐渐过渡到未选中状态的背景颜色
            mPaint.setColor(
                getCurrentColor(
                    mColorGradientFactor,
                    mBackgroundColorChecked,
                    mBackgroundColorUnchecked
                )
            )
        }
        // 设置背景的矩形范围
        mRectF[mPaint.strokeWidth, mPaint.getStrokeWidth(), measuredWidth - mPaint.getStrokeWidth()] =
            measuredHeight - mPaint.getStrokeWidth()
        // 绘制圆角矩形作为背景
        canvas.drawRoundRect(
            mRectF,
            measuredHeight.toFloat(),
            measuredHeight.toFloat(),
            mPaint
        )

        // 设置画笔颜色,准备绘制开关按钮指示器
        mPaint.color = mButtonColor
        /*
         * 获取开关按钮指示器的半径
         * 为了好看一点,开关按钮指示器在背景矩形中显示一点内边距,所以多减去两个画笔宽度
         */
        val radius: Float = (measuredHeight - mPaint.getStrokeWidth() * 4) / 2
        val x: Float
        // 根据是否选中的状态来决定开关按钮指示器圆心的 X 坐标
        x = if (isChecked) {
            //            // 选中状态时开关按钮指示器在右边
            //            x = getMeasuredWidth() - radius - mPaint.getStrokeWidth() - mPaint.getStrokeWidth();
            // 选中状态时开关按钮指示器圆心的 X 坐标从左边逐渐移到右边
            measuredWidth - radius - mPaint.getStrokeWidth() - mPaint.getStrokeWidth() - mButtonCenterXOffset
        } else {
            //            // 未选中状态时开关按钮指示器在左边
            //            x = radius + mPaint.getStrokeWidth() + mPaint.getStrokeWidth();
            // 未选中状态时开关按钮指示器圆心的 X 坐标从右边逐渐移到左边
            radius + mPaint.getStrokeWidth() + mPaint.getStrokeWidth() + mButtonCenterXOffset
        }
        // Y 坐标就是控件高度的一半不变
        val y: Float = measuredHeight.toFloat() / 2
        canvas.drawCircle(x, y, radius, mPaint)
    }

    /**
     * Author: QinHao
     * Email:qinhao@jeejio.com
     * Date:2019/6/3 9:45
     * Description:开始开关按钮切换状态和背景颜色过渡的动画
     */
    private fun startAnimate() {
        // 计算开关指示器的半径
        val radius: Float = (measuredHeight - mPaint.getStrokeWidth() * 4) / 2
        // 计算开关指示器的 X 坐标的总偏移量
        val centerXOffset: Float =
            (measuredWidth - mPaint.getStrokeWidth() - mPaint.getStrokeWidth() - radius
                    - (mPaint.getStrokeWidth() + mPaint.getStrokeWidth() + radius))
        val animatorSet = AnimatorSet()
        // 偏移量逐渐变化到 0
        val objectAnimator =
            ObjectAnimator.ofFloat(this, "buttonCenterXOffset", centerXOffset, 0f)
        objectAnimator.duration = mAnimateDuration
        objectAnimator.addUpdateListener { invalidate() }

        // 背景颜色过渡系数逐渐变化到 1
        val objectAnimator2 =
            ObjectAnimator.ofFloat(this, "colorGradientFactor", 0f, 1f)
        objectAnimator2.duration = mAnimateDuration

        // 同时开始修改开关指示器 X 坐标偏移量的动画和修改背景颜色过渡系数的动画
        animatorSet.play(objectAnimator).with(objectAnimator2)
        animatorSet.start()
    }

    /**
     * Author: QinHao
     * Email:qinhao@jeejio.com
     * Date:2019/6/3 9:04
     * Description:获取一个过渡期中当前颜色,fraction 为过渡系数,取值范围 0f-1f,值越接近 1,颜色就越接近 endColor
     *
     * @param fraction   当前渐变系数
     * @param startColor 过渡开始颜色
     * @param endColor   过渡结束颜色
     * @return 当前颜色
     */
    private fun getCurrentColor(fraction: Float, startColor: Int, endColor: Int): Int {
        val redStart: Int = Color.red(startColor)
        val blueStart: Int = Color.blue(startColor)
        val greenStart: Int = Color.green(startColor)
        val alphaStart: Int = Color.alpha(startColor)
        val redEnd: Int = Color.red(endColor)
        val blueEnd: Int = Color.blue(endColor)
        val greenEnd: Int = Color.green(endColor)
        val alphaEnd: Int = Color.alpha(endColor)
        val redDifference = redEnd - redStart
        val blueDifference = blueEnd - blueStart
        val greenDifference = greenEnd - greenStart
        val alphaDifference = alphaEnd - alphaStart
        val redCurrent = (redStart + fraction * redDifference).toInt()
        val blueCurrent = (blueStart + fraction * blueDifference).toInt()
        val greenCurrent = (greenStart + fraction * greenDifference).toInt()
        val alphaCurrent = (alphaStart + fraction * alphaDifference).toInt()
        return Color.argb(alphaCurrent, redCurrent, greenCurrent, blueCurrent)
    }

    fun setButtonCenterXOffset(buttonCenterXOffset: Float) {
        mButtonCenterXOffset = buttonCenterXOffset
    }

    fun setColorGradientFactor(colorGradientFactor: Float) {
        mColorGradientFactor = colorGradientFactor
    }

    fun setAnimateDuration(animateDuration: Long) {
        mAnimateDuration = animateDuration
    }

    fun setBackgroundColorUnchecked(backgroundColorUnchecked: Int) {
        mBackgroundColorUnchecked = backgroundColorUnchecked
    }

    fun setBackgroundColorChecked(backgroundColorChecked: Int) {
        mBackgroundColorChecked = backgroundColorChecked
    }

    fun setButtonColor(buttonColor: Int) {
        mButtonColor = buttonColor
    }

}