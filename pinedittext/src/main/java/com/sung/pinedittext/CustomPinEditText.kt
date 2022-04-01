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
    private var cursorLineThickness = Util.dpToPx(1.0f)
    private val cursorPadding = Util.dpToPx(10f)
    private var cursorPaint = Paint()
    private var cursorPaintColor = ContextCompat.getColor(context, R.color.cursorColor)
        set(value) {
            field = value
            cursorPaint.color = field
            invalidate()
        }
    private var cursorThickness = cursorLineThickness
        get() {
            return cursorLineThickness + cursorLineThickness * 0.7f
        }
    private val defaultWidth = Util.dpToPx(55f).toInt()
    private var fieldColor = ContextCompat.getColor(context, R.color.inactivePinFieldColor)
        set(value) {
            field = value
            fieldPaint.color = field
            invalidate()
        }
    private var fieldPaint = Paint().apply {
        color = fieldColor
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = cursorLineThickness
    }
    private var isCursorVisibleNow = true
    private var lastCursorChangeState: Long = -1
    private var singleFieldWidth = 0
    private var textPaint = Paint()

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr) {
        initialize(attr)
    }

    constructor(context: Context, attr: AttributeSet, defStyle: Int) : super(context,
        attr,
        defStyle) {
        initialize(attr)
    }

    private fun limitCharsToNoOfFields() {
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = InputFilter.LengthFilter(NUMBER_OF_FIELDS)
        filters = filterArray
    }

    init {
        cursorPaint = Paint(fieldPaint).apply {
            color = cursorPaintColor
            strokeWidth = cursorThickness
        }
        isSingleLine = true
        limitCharsToNoOfFields()
        maxLines = 1
        setWillNotDraw(false)
        textPaint.color = currentTextColor
        textPaint.isAntiAlias = true
        textPaint.textSize = textSize
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.style = Paint.Style.FILL
    }

    override fun onSelectionChanged(start: Int, end: Int) {
        super.onSelectionChanged(start, end)
        this.text?.length?.let { this.setSelection(it) }
    }

    private fun initialize(attr: AttributeSet) {
        val attributes = context.theme.obtainStyledAttributes(attr, R.styleable.PinField, 0, 0)
        try {
            fieldColor = attributes.getColor(R.styleable.PinField_fieldColor, fieldColor)
            cursorPaintColor =
                attributes.getColor(R.styleable.PinField_highlightColor, cursorPaintColor)
            textPaint.typeface = typeface
        } finally {
            attributes.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getViewWidth(defaultWidth * NUMBER_OF_FIELDS, widthMeasureSpec)
        singleFieldWidth = width / NUMBER_OF_FIELDS
        val height = getViewHeight(singleFieldWidth, heightMeasureSpec) * MULTIPLIER
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
            val padding = Util.dpToPx(6f)
            val left = x1 + padding
            var right = (x1 + singleFieldWidth) - padding
            val squareHeight = ((right) - left) * MULTIPLIER
            val top = (height / 2) - (squareHeight / 2)
            val bottom = (height / 2) + (squareHeight / 2)
            val textX = ((right - left) / 2) + left
            val textY = ((bottom - top) / 2 + top) + cursorLineThickness + (textPaint.textSize / 4)
            val character: Char? = getCharAt(i)
            canvas?.drawRect(left, top, right, bottom, fieldPaint)
            if (character != null) {
                canvas?.drawText(character.toString(), textX, textY, textPaint)
            }

            if (hasFocus() && i == text?.length ?: 0) {
                val cursorPadding = (cursorPadding + cursorThickness)
                val cursorY1 = (top + cursorPadding) * 1.3f
                val cursorY2 = (bottom - cursorPadding) * 0.8f
                drawCursor(canvas, textX, cursorY1, cursorY2, cursorPaint)
            }
        }
    }

    private fun drawCursor(canvas: Canvas?, x: Float, y1: Float, y2: Float, paint: Paint) {
        if (System.currentTimeMillis() - lastCursorChangeState > 500) {
            isCursorVisibleNow = !isCursorVisibleNow
            lastCursorChangeState = System.currentTimeMillis()
        }
        if (isCursorVisibleNow) {
            canvas?.drawLine(x, y1, x, y2, paint)
        }
        postInvalidateDelayed(cursorTimeout)
    }

    companion object {
        private const val NUMBER_OF_FIELDS = 4
        private const val cursorTimeout = 500L
        private const val MULTIPLIER = 1.5f
    }
}
