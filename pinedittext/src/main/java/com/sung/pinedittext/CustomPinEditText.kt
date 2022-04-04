package com.sung.pinedittext

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.InputFilter
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import kotlin.math.min

/**
 *   A class that contains each confirmation code field
 *
 *   @author John Sung
 */
class CustomPinEditText : AppCompatEditText {
    private var cursorPaint = Paint()
    private var cursorPaintColor = ContextCompat.getColor(context, R.color.cursorColor)
        set(value) {
            field = value
            cursorPaint.color = field
            invalidate()
        }
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
        strokeWidth = CURSOR_LINE_THICKNESS
    }
    private var isCursorVisibleNow = true
    private var lastCursorChangeState: Long = -1
    private var numberOfFields: Int = 4
        set(value) {
            field = value
            limitCharacter()
            invalidate()
        }
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

    private fun drawCursor(
        canvas: Canvas?,
        startX: Float,
        startY: Float,
        stopY: Float,
        paint: Paint,
    ) {
        setUpCursorBlinkingInterval()
        if (isCursorVisibleNow) {
            canvas?.drawLine(startX, startY, startX, stopY, paint)
        }
        postInvalidateDelayed(CURSOR_TIMEOUT)
    }

    private fun getCharacterAt(position: Int) =
        transformationMethod.getTransformation(text, this)?.getOrNull(position)
            ?: text?.getOrNull(position)

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
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize)
            MeasureSpec.UNSPECIFIED -> desiredWidth
            else -> desiredWidth
        }
    }

    private fun limitCharacter() {
        filters = arrayOf(InputFilter.LengthFilter(numberOfFields))
    }

    init {
        cursorPaint = Paint(fieldPaint).apply {
            color = cursorPaintColor
            strokeWidth = CURSOR_THICKNESS
        }
        isSingleLine = true
        limitCharacter()
        maxLines = 1
        setWillNotDraw(false)
        textPaint.color = currentTextColor
        textPaint.isAntiAlias = true
        textPaint.textSize = textSize
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.style = Paint.Style.FILL
        textPaint.typeface = typeface
    }

    private fun initialize(attr: AttributeSet) {
        val attributes = context.theme.obtainStyledAttributes(attr, R.styleable.PinField, 0, 0)
        try {
            cursorPaintColor =
                attributes.getColor(R.styleable.PinField_highlightColor, cursorPaintColor)
            fieldColor = attributes.getColor(R.styleable.PinField_fieldColor, fieldColor)
            numberOfFields = attributes.getInt(R.styleable.PinField_noOfFields, numberOfFields)
        } finally {
            attributes.recycle()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        for (i in 0 until numberOfFields) {
            val x1 = (i * singleFieldWidth)
            val padding = Util.dpToPx(6f)
            val left = x1 + padding
            val right = (x1 + singleFieldWidth) - padding
            val squareHeight = ((right) - left) * MULTIPLIER
            val top = (height / 2) - (squareHeight / 2)
            val bottom = (height / 2) + (squareHeight / 2)
            val textX = ((right - left) / 2) + left
            val textY =
                ((bottom - top) / 2 + top) + CURSOR_LINE_THICKNESS + (textPaint.textSize / 4)
            canvas?.drawRect(left, top, right, bottom, fieldPaint)
            val character: Char? = getCharacterAt(i)
            character?.let {
                canvas?.drawText(it.toString(), textX, textY, textPaint)
            }
            if (hasFocus() && i == text?.length ?: 0) {
                val cursorPadding = CURSOR_PADDING + CURSOR_THICKNESS
                val cursorY1 = (top + cursorPadding) * 1.3f
                val cursorY2 = (bottom - cursorPadding) * 0.8f
                drawCursor(canvas, textX, cursorY1, cursorY2, cursorPaint)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getViewWidth(DEFAULT_WIDTH * numberOfFields, widthMeasureSpec)
        singleFieldWidth = width / numberOfFields
        val height = getViewHeight(singleFieldWidth, heightMeasureSpec) * MULTIPLIER
        setMeasuredDimension(width, height.toInt())
    }

    override fun onSelectionChanged(start: Int, end: Int) {
        super.onSelectionChanged(start, end)
        this.text?.length?.let { this.setSelection(it) }
    }

    private fun setUpCursorBlinkingInterval() {
        if (System.currentTimeMillis() - lastCursorChangeState > CURSOR_TIMEOUT) {
            isCursorVisibleNow = !isCursorVisibleNow
            lastCursorChangeState = System.currentTimeMillis()
        }
    }

    private companion object {
        private var CURSOR_LINE_THICKNESS = Util.dpToPx(1.0f)
        private val CURSOR_PADDING = Util.dpToPx(10f)
        private var CURSOR_THICKNESS = CURSOR_LINE_THICKNESS + CURSOR_LINE_THICKNESS * 0.7f
        private val DEFAULT_WIDTH = Util.dpToPx(55f).toInt()
        private const val CURSOR_TIMEOUT = 500L
        private const val MULTIPLIER = 1.5f
    }
}
