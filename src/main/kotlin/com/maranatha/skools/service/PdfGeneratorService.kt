package com.maranatha.skools.service

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import java.io.ByteArrayOutputStream

object PdfGeneratorService {

    fun generatePdfFromHtml(htmlContent: String): ByteArray {
        val os = ByteArrayOutputStream()
        val builder = PdfRendererBuilder()
        builder.useFastMode()
        builder.withHtmlContent(htmlContent, "")
        builder.toStream(os)
        builder.run()
        return os.toByteArray()
    }
}