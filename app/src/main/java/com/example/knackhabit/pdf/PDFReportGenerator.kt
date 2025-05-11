package com.example.knackhabit.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.example.knackhabit.R
import java.io.File

object PDFReportGenerator {
    fun createHabitReportPdf(
        context: Context,
        fileName: String,
        periods: List<PeriodReport>
    ): Boolean {
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f
        val bottomLimit = pageHeight - margin

        val pdf = PdfDocument()
        var pageNumber = 1
        lateinit var page: PdfDocument.Page
        lateinit var canvas: Canvas
        var yPos = 0f
        var pageStarted = false
        var isFirstPage = true

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK; textSize = 16f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; textSize = 14f }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.LTGRAY; strokeWidth = 1f }

        fun startNewPage() {
            if (pageStarted) pdf.finishPage(page)
            val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdf.startPage(info)
            canvas = page.canvas
            yPos = margin
            if (isFirstPage) {
                textPaint.apply { textSize = 28f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
                canvas.drawText("Ваш отчёт по привычкам", margin, yPos, textPaint)
                yPos += 30f
                canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
                yPos += 30f
                isFirstPage = false
            }
            pageNumber++
            pageStarted = true
        }

        fun drawHabit(item: HabitReportItem) {
            val estH = 140f
            if (yPos + estH > bottomLimit) startNewPage()
            val availW = pageWidth - margin * 2
            val title = TextUtils.ellipsize(item.title, titlePaint, availW, TextUtils.TruncateAt.END).toString()
            canvas.drawText(title, margin, yPos, titlePaint)
            yPos += 25f

            textPaint.apply { textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
            var x = margin
            val days = listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс")
            for (d in days) {
                canvas.drawText(d, x, yPos, textPaint)
                x += 60f
            }
            yPos += 20f

            x = margin; val bs = 20f
            for (i in days.indices) {
                if (!item.scheduled[i]) {
                    val backPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(50, 255, 0, 0); style = Paint.Style.FILL }
                    canvas.drawRoundRect(x, yPos, x + bs, yPos + bs, 6f, 6f, backPaint)
                }
                val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
                canvas.drawRoundRect(x, yPos, x + bs, yPos + bs, 6f, 6f, boxPaint)
                if (item.checks[i]) {
                    val dr = ContextCompat.getDrawable(context, R.drawable.baseline_check_24)!!
                    dr.setTint(Color.BLACK)
                    dr.setBounds((x + 4).toInt(), (yPos + 4).toInt(), (x + bs - 4).toInt(), (yPos + bs - 4).toInt())
                    dr.draw(canvas)
                }
                x += 60f
            }
            yPos += bs + 10f

            // progress bar
            val bh = 20f; val bw = 400f
            val backP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.LTGRAY; style = Paint.Style.FILL }
            val fgP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = item.barColor; style = Paint.Style.FILL }
            canvas.drawRoundRect(margin, yPos, margin + bw, yPos + bh, 10f, 10f, backP)
            val fillW = bw * item.percent / 100f
            canvas.drawRoundRect(margin, yPos, margin + fillW, yPos + bh, 10f, 10f, fgP)
            textPaint.apply { textSize = 14f; color = Color.BLACK }
            canvas.drawText("${item.percent}%", margin + bw + 10f, yPos + bh - 4f, textPaint)
            yPos += bh + 30f
        }

        fun drawPeriod(p: PeriodReport) {
            startNewPage()
            textPaint.apply { textSize = 22f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            canvas.drawText("Период: ${p.label}", margin, yPos, textPaint)
            yPos += 30f
            p.items.forEach { drawHabit(it) }
        }

        periods.forEach { drawPeriod(it) }
        if (pageStarted) pdf.finishPage(page)

        val saved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cv = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { out -> pdf.writeTo(out) }
                true
            } ?: false
        } else {
            val dl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!dl.exists()) dl.mkdirs()
            File(dl, "$fileName.pdf").outputStream().use { pdf.writeTo(it) }
            true
        }
        pdf.close()
        return saved
    }
}