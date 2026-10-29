package com.example.appmovil.view.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.Locale

// Genera el protocolo formal en PDF con herramientas nativas de Android
// (CasoA pantalla 6) y lo comparte por WhatsApp/correo con ACTION_SEND.
// El protocolo incluye los datos del cliente, la sucursal visitada, el técnico
// que ejecuta la prueba, el equipo, la medición, el valor del trabajo y la foto
// del equipo tomada en terreno como evidencia.
object InformePdf {

    // Datos ya resueltos por la pantalla de informe, para no consultar la base
    // desde aquí: el PDF solo dibuja lo que recibe.
    data class DatosInforme(
        val empresaNombre: String,
        val empresaRut: String,
        val sucursalNombre: String,
        val sucursalDireccion: String,
        val sucursalComuna: String,
        val sucursalTelefono: String,
        val tecnicoNombre: String,
        val tecnicoEmail: String,
        val categoria: String,
        val codigo: String,
        val actualValores: String,
        val actualObs: String,
        val actualFecha: String,
        val actualPrecio: Double?,
        val previaValores: String?,
        val previaFecha: String?,
        // Foto de evidencia del equipo: la app no la guarda, solo viaja al PDF.
        val fotoEvidencia: String? = null
    )

    fun generar(context: Context, datos: DatosInforme): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val titulo = Paint().apply { textSize = 20f; isFakeBoldText = true }
        val subtitulo = Paint().apply { textSize = 13f; isFakeBoldText = true }
        val cuerpo = Paint().apply { textSize = 12f }
        val gris = Paint().apply { textSize = 11f }
        val pie = Paint().apply { textSize = 10f; color = android.graphics.Color.DKGRAY }
        val marco = Paint().apply {
            color = android.graphics.Color.GRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

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

        linea(subtitulo, "Cliente")
        linea(cuerpo, "Empresa: ${datos.empresaNombre}")
        linea(cuerpo, "RUT: ${datos.empresaRut}")
        y += 8f

        linea(subtitulo, "Ubicacion de la prueba")
        linea(cuerpo, "Sucursal: ${datos.sucursalNombre}")
        linea(cuerpo, "Direccion: ${datos.sucursalDireccion}")
        linea(cuerpo, "Comuna: ${datos.sucursalComuna}")
        if (datos.sucursalTelefono.isNotBlank()) {
            linea(cuerpo, "Telefono: ${datos.sucursalTelefono}")
        }
        y += 8f

        linea(subtitulo, "Tecnico responsable")
        linea(cuerpo, "Nombre: ${datos.tecnicoNombre}")
        linea(cuerpo, "Correo: ${datos.tecnicoEmail}")
        y += 8f

        linea(subtitulo, "Equipo")
        linea(cuerpo, "Categoria: ${datos.categoria}")
        linea(cuerpo, "Codigo: ${datos.codigo}")
        y += 8f

        linea(subtitulo, "Medicion actual (${datos.actualFecha})")
        linea(cuerpo, "Valores: ${datos.actualValores}")
        if (datos.actualObs.isNotBlank()) linea(cuerpo, "Observaciones: ${datos.actualObs}")
        if (datos.actualPrecio != null) {
            linea(cuerpo, "Valor del trabajo: $${String.format(Locale.getDefault(), "%,.0f", datos.actualPrecio)}")
        }
        y += 8f

        linea(subtitulo, "Tabla comparativa con lectura historica")
        if (datos.previaValores != null && datos.previaFecha != null) {
            linea(cuerpo, "Anterior (${datos.previaFecha}): ${datos.previaValores}")
            linea(cuerpo, "Actual  (${datos.actualFecha}): ${datos.actualValores}")
        } else {
            linea(cuerpo, "Sin lectura anterior: primera medicion del equipo.")
            linea(cuerpo, "Actual  (${datos.actualFecha}): ${datos.actualValores}")
        }
        y += 14f

        // Evidencia fotografica: como estaba el equipo al momento de la prueba.
        val foto = datos.fotoEvidencia?.let { leerBitmap(context, it) }
        if (foto != null) {
            linea(subtitulo, "Evidencia fotografica del equipo")
            val ancho = 380f
            val alto = foto.height * (ancho / foto.width)
            val destino = RectF(40f, y, 40f + ancho, y + alto)
            canvas.drawBitmap(foto, null, destino, null)
            canvas.drawRect(destino, marco)
            canvas.drawText(
                "Estado del equipo al momento de la medicion (${datos.actualFecha})",
                40f,
                destino.bottom + 14f,
                pie
            )
            y = destino.bottom + 26f
        } else {
            linea(gris, "Sin evidencia fotografica en este protocolo.")
            y += 10f
        }

        doc.finishPage(page)

        // El protocolo queda en filesDir para que el administrador lo pueda
        // volver a compartir desde su historial.
        val dir = File(context.filesDir, "informes").apply { if (!exists()) mkdirs() }
        val nombre = "protocolo_${datos.codigo.replace("-", "_")}_${System.currentTimeMillis()}.pdf"
        val file = File(dir, nombre)
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // Lee la imagen de evidencia desde su Uri (cámara o galería).
    private fun leerBitmap(context: Context, uri: String): Bitmap? = try {
        context.contentResolver.openInputStream(Uri.parse(uri))?.use { BitmapFactory.decodeStream(it) }
    } catch (e: Exception) {
        null
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
