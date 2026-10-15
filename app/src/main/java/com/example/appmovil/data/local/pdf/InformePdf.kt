package com.example.appmovil.data.local.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File

// Genera el protocolo formal en PDF con herramientas nativas de Android
// (CasoA pantalla 6) y lo comparte por WhatsApp/correo con ACTION_SEND.
object InformePdf {

    fun generar(
        context: Context,
        empresaNombre: String,
        empresaZona: String,
        categoria: String,
        codigo: String,
        actualValores: String,
        actualObs: String,
        actualFecha: String,
        previaValores: String?,
        previaFecha: String?
    ): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val titulo = Paint().apply { textSize = 20f; isFakeBoldText = true }
        val subtitulo = Paint().apply { textSize = 13f; isFakeBoldText = true }
        val cuerpo = Paint().apply { textSize = 12f }
        val gris = Paint().apply { textSize = 11f }

        var y = 60f
        fun linea(p: Paint, s: String, salto: Float = 22f) {
            // Corte simple por ancho para no salir de la página.
            var resto = s
            while (resto.length > 70) {
                canvas.drawText(resto.take(70), 40f, y, p)
                y += salto
                resto = resto.drop(70)
            }
            canvas.drawText(resto, 40f, y, p)
            y += salto
        }

        linea(titulo, "Induztek - Protocolo de pruebas electricas", 30f)
        linea(gris, "Generado en terreno (offline) - MVP DSY1105")
        y += 8f
        linea(subtitulo, "Empresa / instalacion")
        linea(cuerpo, "Nombre: $empresaNombre")
        linea(cuerpo, "Zona: $empresaZona")
        y += 8f
        linea(subtitulo, "Equipo")
        linea(cuerpo, "Categoria: $categoria")
        linea(cuerpo, "Codigo: $codigo")
        y += 8f
        linea(subtitulo, "Medicion actual ($actualFecha)")
        linea(cuerpo, "Valores: $actualValores")
        if (actualObs.isNotBlank()) linea(cuerpo, "Observaciones: $actualObs")
        y += 8f
        linea(subtitulo, "Tabla comparativa con lectura historica")
        if (previaValores != null && previaFecha != null) {
            linea(cuerpo, "Anterior ($previaFecha): $previaValores")
            linea(cuerpo, "Actual  ($actualFecha): $actualValores")
        } else {
            linea(cuerpo, "Sin lectura anterior: primera medicion del equipo.")
            linea(cuerpo, "Actual  ($actualFecha): $actualValores")
        }

        doc.finishPage(page)
        val dir = File(context.cacheDir, "informes").apply { if (!exists()) mkdirs() }
        val nombre = "protocolo_${codigo.replace("-", "_")}_${System.currentTimeMillis()}.pdf"
        val file = File(dir, nombre)
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun compartir(context: Context, file: File, codigo: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Protocolo $codigo - Induztek")
            putExtra(Intent.EXTRA_TEXT, "Adjunto protocolo de pruebas eléctricas del equipo $codigo.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir protocolo PDF"))
    }
}
