package com.sung.pinedittext

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CustomRectangle @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {
    private val paint = Paint()

    public override fun onDraw(canvas: Canvas) {
//        paint.color = Color.parseColor("#CD5C5C")
//        canvas.drawRect(50f, 80f, 200f, 200f, paint)

        var left = 200 // initial start position of rectangles (50 pixels from left)
        var top = 50 // 50 pixels from the top

        val width = 150
        val height = 250
        //for (row in 0..1) { // draw 2 rows
            for (col in 0..3) { // draw 4 columns
                paint.color = Color.parseColor("#CD5C5C")
                canvas.drawRect(left.toFloat(), top.toFloat(), (left + width).toFloat(),
                    (top + height).toFloat(), paint)
                left = left + width + 10 // set new left co-ordinate + 10 pixel gap
                // Do other things here
                // i.e. change colour
            }
            top = top + height + 10 // move to new row by changing the top co-ordinate
      //  }
    }
}