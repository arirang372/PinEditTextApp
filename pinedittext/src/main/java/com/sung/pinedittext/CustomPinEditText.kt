package com.sung.pinedittext

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.InputFilter
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import kotlin.math.min

class CustomPinEditText : AppCompatEditText {
    private val cursorPadding = Util.dpToPx(10f)
    private val defaultWidth = Util.dpToPx(60f).toInt()
    private var cursorPaint = Paint()
    private var fieldPaint = Paint()
    private var textPaint = Paint()
    private var singleFieldWidth = 0
    private var lineThickness = Util.dpToPx(1.0f)

    private var cursorPaintColor = ContextCompat.getColor(context, R.color.cursorColor)
        set(value) {
            field = value
            cursorPaint.color = field
            invalidate()
        }

    private var highLightThickness = lineThickness
        get() {
            return lineThickness + lineThickness * 0.7f
        }

    private var fieldColor = ContextCompat.getColor(context, R.color.inactivePinFieldColor)
        set(value) {
            field = value
            fieldPaint.color = field
            invalidate()
        }

    private var lastCursorChangeState: Long = -1
    private var cursorCurrentVisible = true

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr) {
        initParams(attr)
    }

    constructor(context: Context, attr: AttributeSet, defStyle: Int) : super(context,
        attr,
        defStyle) {
        initParams(attr)
    }

    private fun limitCharsToNoOfFields() {
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = InputFilter.LengthFilter(NUMBER_OF_FIELDS)
        filters = filterArray
    }

    init {
        limitCharsToNoOfFields()
        setWillNotDraw(false)
        maxLines = 1
        isSingleLine = true

        fieldPaint.color = fieldColor
        fieldPaint.isAntiAlias = true
        fieldPaint.style = Paint.Style.STROKE
        fieldPaint.strokeWidth = lineThickness

        textPaint.color = currentTextColor
        textPaint.isAntiAlias = true
        textPaint.textSize = textSize
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.style = Paint.Style.FILL

        cursorPaint = Paint(fieldPaint)
        cursorPaint.color = cursorPaintColor
        cursorPaint.strokeWidth = highLightThickness
    }

    override fun onSelectionChanged(start: Int, end: Int) {
        super.onSelectionChanged(start, end)
        this.text?.length?.let { this.setSelection(it) }
    }

    private fun initParams(attr: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attr, R.styleable.PinField, 0, 0)
        try {
            fieldColor = a.getColor(R.styleable.PinField_fieldColor, fieldColor)
            cursorPaintColor = a.getColor(R.styleable.PinField_highlightColor, cursorPaintColor)
            textPaint.typeface = typeface
        } finally {
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getViewWidth(defaultWidth * NUMBER_OF_FIELDS, widthMeasureSpec)
        singleFieldWidth = width / NUMBER_OF_FIELDS
        val height = getViewHeight(singleFieldWidth, heightMeasureSpec) * 1.5
        setMeasuredDimension(width, height.toInt())
    }

    private fun getViewHeight(desiredHeight: Int, heightMeasureSpec: Int): Int {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        return when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            MeasureSpec.UNSPECIFIED -> desiredHeight
            else -> desiredHeight
        }
    }

    private fun getViewWidth(desiredWidth: Int, widthMeasureSpec: Int): Int {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        return when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> Math.min(desiredWidth, widthSize)
            MeasureSpec.UNSPECIFIED -> desiredWidth
            else -> desiredWidth
        }
    }

    private fun getCharAt(i: Int): Char? {
        return transformationMethod.getTransformation(text, this)?.getOrNull(i)
            ?: text?.getOrNull(i)
    }

    override fun onDraw(canvas: Canvas?) {

        for (i in 0 until NUMBER_OF_FIELDS) {
            val x1 = (i * singleFieldWidth)
            val padding = Util.dpToPx(7f)
            val left = x1 + padding
            var paddedX2 = (x1 + singleFieldWidth) - padding
            val squareHeight = ((paddedX2) - left) * 1.5f
            val top = (height / 2) - (squareHeight / 2)
            val paddedY2 = (height / 2) + (squareHeight / 2)
            val textX = ((paddedX2 - left) / 2) + left
            val textY = ((paddedY2 - top) / 2 + top) + lineThickness + (textPaint.textSize / 4)
            val character: Char? = getCharAt(i)
            drawRect(canvas, left, top, paddedX2, paddedY2, fieldPaint)

            if (character != null) {
                canvas?.drawText(character.toString(), textX, textY, textPaint)
            }

            if (hasFocus() && i == text?.length ?: 0) {
                val cursorPadding = (cursorPadding + highLightThickness)
                val cursorY1 = (top + cursorPadding)
                val cursorY2 = (paddedY2 - cursorPadding)
                drawCursor(canvas, textX, cursorY1, cursorY2, cursorPaint)
            }
        }
    }

    private fun drawCursor(canvas: Canvas?, x: Float, y1: Float, y2: Float, paint: Paint) {
        if (System.currentTimeMillis() - lastCursorChangeState > 500) {
            cursorCurrentVisible = !cursorCurrentVisible
            lastCursorChangeState = System.currentTimeMillis()
        }
        if (cursorCurrentVisible) {
            canvas?.drawLine(x, y1, x, y2, paint)
        }
        postInvalidateDelayed(cursorTimeout)
    }

    private fun drawRect(
        canvas: Canvas?,
        paddedX1: Float,
        paddedY1: Float,
        paddedX2: Float,
        paddedY2: Float,
        paint: Paint,
    ) {
        canvas?.drawRect(paddedX1, paddedY1, paddedX2, paddedY2, paint)
    }

    companion object {
        private const val NUMBER_OF_FIELDS = 4
        private const val cursorTimeout = 500L
    }
}
