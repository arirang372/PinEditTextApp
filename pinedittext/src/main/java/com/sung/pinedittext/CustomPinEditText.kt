package com.sung.pinedittext

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.InputFilter
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import kotlin.math.min

class CustomPinEditText : AppCompatEditText {
    var onTextCompleteListener: PinField.OnTextCompleteListener? = null
    private val cursorPadding = Util.dpToPx(5f)
    private val defaultWidth = Util.dpToPx(60f).toInt()
    var fieldBgColor : Int = ContextCompat.getColor(context, R.color.white)
        set(value) {
            field = value
            fieldBgPaint.color = fieldBgColor
            invalidate()
        }

    private var highlightPaint = Paint()
    private var fieldPaint = Paint()
    private var textPaint = Paint()
    private var fieldBgPaint = Paint()
    private var singleFieldWidth = 0
    private var lineThickness = Util.dpToPx(1.0f)
    private var highlightSingleFieldType = HighlightType.ALL_FIELDS

    var highlightPaintColor = ContextCompat.getColor(context, R.color.pinFieldLibraryAccent)
        set(value) {
            field = value
            highlightPaint.color = field
            invalidate()
        }

    var highLightThickness = lineThickness
        get() {
            return lineThickness + lineThickness * 0.7f
        }
    var fieldColor = ContextCompat.getColor(context, R.color.inactivePinFieldColor)
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

        highlightPaint = Paint(fieldPaint)
        highlightPaint.color = highlightPaintColor
        highlightPaint.strokeWidth = highLightThickness

        fieldBgColor = Color.TRANSPARENT
        fieldBgPaint.style = Paint.Style.FILL
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        this.setSelection(this.text!!.length)
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int,
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (text != null && text.length == NUMBER_OF_FIELDS) {
            val shouldCloseKeyboard = onTextCompleteListener?.onTextComplete(text.toString())
                ?: false
            if (shouldCloseKeyboard) {
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(windowToken, 0)
            }
        }
    }

    private fun initParams(attr: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attr, R.styleable.PinField, 0, 0)
        try {
            fieldColor = a.getColor(R.styleable.PinField_fieldColor, fieldColor)
            highlightPaintColor = a.getColor(R.styleable.PinField_highlightColor, highlightPaintColor)
            fieldBgColor = a.getColor(R.styleable.PinField_fieldBgColor, fieldBgColor)
            highlightSingleFieldType = if (a.getBoolean(R.styleable.PinField_highlightEnabled,
                    true)
            ) HighlightType.ALL_FIELDS else HighlightType.NO_FIELDS
            highlightSingleFieldType =
                if (a.getBoolean(R.styleable.PinField_highlightSingleFieldMode,
                        false)
                ) HighlightType.CURRENT_FIELD else HighlightType.ALL_FIELDS
            highlightSingleFieldType =
                HighlightType.getEnum(a.getInt(R.styleable.PinField_highlightType,
                    highlightSingleFieldType.code))
            textPaint.typeface = typeface
        } finally {
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getViewWidth(defaultWidth * NUMBER_OF_FIELDS, widthMeasureSpec)
        singleFieldWidth = width / NUMBER_OF_FIELDS
        setMeasuredDimension(width, getViewHeight(singleFieldWidth, heightMeasureSpec))
    }


    private fun getViewHeight(desiredHeight: Int, heightMeasureSpec: Int): Int {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        //Measure Height
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

        //Measure Width
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

    private fun getDefaultDistanceInBetween(): Float {
        return (singleFieldWidth / (NUMBER_OF_FIELDS - 1)).toFloat()
    }

    override fun onDraw(canvas: Canvas?) {

        for (i in 0 until NUMBER_OF_FIELDS) {

            val x1 = (i * singleFieldWidth)
            val padding = getDefaultDistanceInBetween() / 2
            val paddedX1 = (x1 + padding)
            val paddedX2 = ((x1 + singleFieldWidth) - padding)
            val squareHeight = paddedX2 - paddedX1
            val paddedY1 = (height / 2) - (squareHeight / 2)
            val paddedY2 = (height / 2) + (squareHeight / 2)
            val textX = ((paddedX2 - paddedX1) / 2) + paddedX1
            val textY =
                ((paddedY2 - paddedY1) / 2 + paddedY1) + lineThickness + (textPaint.textSize / 4)
            val character: Char? = getCharAt(i)

            drawRect(canvas, paddedX1, paddedY1, paddedX2, paddedY2, fieldBgPaint)
            drawRect(canvas, paddedX1, paddedY1, paddedX2, paddedY2, fieldPaint)

            if (character != null) {
                canvas?.drawText(character.toString(), textX, textY, textPaint)
            }

//            if (shouldDrawHint()) {
//                val hintChar = hint.getOrNull(i)
//                if (hintChar != null) {
//                    canvas?.drawText(hintChar.toString(), textX, textY, hintPaint)
//                }
//            }

            if (hasFocus() && i == text?.length ?: 0) {
                val cursorPadding = cursorPadding + highLightThickness
                val cursorY1 = paddedY1 + cursorPadding
                val cursorY2 = paddedY2 - cursorPadding
                drawCursor(canvas, textX, cursorY1, cursorY2, highlightPaint)
            }
            highlightLogic(i, text?.length) {
                drawRect(canvas, paddedX1, paddedY1, paddedX2, paddedY2, highlightPaint)
            }
        }
    }

    private fun highlightLogic(currentPosition: Int, textLength: Int?, onHighlight: () -> Unit) {
        if (hasFocus() && !highlightNoFields()) {
            when {
                highlightNextField() && currentPosition == textLength ?: 0 -> {
                    onHighlight.invoke()
                }
                highlightCompletedFields() && currentPosition < textLength ?: 0 -> {
                    onHighlight.invoke()
                }
            }
        }
    }

    private fun highlightNextField(): Boolean {
        return highlightSingleFieldType == HighlightType.CURRENT_FIELD
    }

    private fun highlightCompletedFields(): Boolean {
        return highlightSingleFieldType == HighlightType.COMPLETED_FIELDS
    }

    private fun highlightAllFields(): Boolean {
        return highlightSingleFieldType == HighlightType.ALL_FIELDS
    }

    private fun highlightNoFields(): Boolean {
        return highlightSingleFieldType == HighlightType.NO_FIELDS
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
