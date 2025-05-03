package com.example.knackhabit.ui_components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt
import kotlin.math.min

class DaysOfWeekView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val days = listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс")
    private val selected = BooleanArray(days.size) { false }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            14f,
            resources.displayMetrics
        )
    }

    private val defaultColor = "#E0E0E0".toColorInt()
    private val selectedColor = "#FFA726".toColorInt()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cellW = width.toFloat() / days.size
        val radius = min(cellW, height.toFloat()) * 0.4f
        val cy = height / 2f

        days.forEachIndexed { i, label ->
            val cx = cellW * i + cellW / 2
            circlePaint.color = if (selected[i]) selectedColor else defaultColor
            canvas.drawCircle(cx, cy, radius, circlePaint)

            val txtY = cy - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(label, cx, txtY, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val idx = (event.x / (width.toFloat() / days.size))
                .toInt()
                .coerceIn(0, days.lastIndex)
            selected[idx] = !selected[idx]
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }

    /** Возвращает список выбранных меток дней, напр. ["Пн","Ср"] */
    fun getSelectedDays(): List<String> =
        days.filterIndexed { i, _ -> selected[i] }

    /** Устанавливает выбор по списку меток, напр. ["Вт","Сб"] */
    fun setSelectedDays(labels: List<String>) {
        days.forEachIndexed { i, day ->
            selected[i] = day in labels
        }
        invalidate()
    }
}