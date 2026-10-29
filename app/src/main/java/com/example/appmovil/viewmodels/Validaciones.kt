package com.example.appmovil.viewmodels

import android.util.Patterns // Patrón estándar de Android para emails

/**
 * Reglas de validación compartidas por los ViewModels.
 * Cada función devuelve el mensaje de error o null si el valor es válido.
 */
object Validaciones {

    fun email(valor: String): String? {
        if (valor.isBlank()) return "El email es obligatorio"
        val ok = Patterns.EMAIL_ADDRESS.matcher(valor).matches()
        return if (!ok) "Formato de email inválido" else null
    }

    fun nombre(valor: String): String? {
        if (valor.isBlank()) return "El nombre es obligatorio"
        val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
        return if (!regex.matches(valor)) "Solo letras y espacios" else null
    }

    fun telefono(valor: String): String? {
        if (valor.isBlank()) return null // Teléfono opcional para el técnico
        if (!valor.all { it.isDigit() }) return "Solo números"
        if (valor.length !in 8..15) return "Debe tener entre 8 y 15 dígitos"
        return null
    }

    fun password(valor: String): String? {
        if (valor.isBlank()) return "La contraseña es obligatoria"
        if (valor.length < 8) return "Mínimo 8 caracteres"
        if (!valor.any { it.isUpperCase() }) return "Debe incluir una mayúscula"
        if (!valor.any { it.isLowerCase() }) return "Debe incluir una minúscula"
        if (!valor.any { it.isDigit() }) return "Debe incluir un número"
        if (!valor.any { !it.isLetterOrDigit() }) return "Debe incluir un símbolo"
        if (valor.contains(' ')) return "No debe contener espacios"
        return null
    }

    fun confirmar(pass: String, confirm: String): String? {
        if (confirm.isBlank()) return "Confirma tu contraseña"
        return if (pass != confirm) "Las contraseñas no coinciden" else null
    }

    // Valores de medición del protocolo en terreno
    fun medicion(valor: String): String? {
        if (valor.isBlank()) return "Debe registrar los valores de medición para guardar el protocolo"
        if (valor.trim().length < 3) return "Medición muy corta, ej: 2.8 GΩ"
        return null
    }

    fun empresaNombre(valor: String): String? {
        if (valor.isBlank()) return "El nombre de la empresa es obligatorio"
        if (valor.trim().length < 2) return "Nombre muy corto"
        return null
    }

    fun zona(valor: String): String? {
        if (valor.isBlank()) return "La zona es obligatoria"
        if (valor.trim().length < 2) return "Zona muy corta, ej: Planta Maipú"
        return null
    }

    // RUT de la empresa cliente (formato chileno 76.123.456-7)
    fun rut(valor: String): String? {
        if (valor.isBlank()) return "El RUT es obligatorio"
        val limpio = valor.trim().replace(".", "").replace("-", "")
        if (limpio.length != 9) return "El RUT debe tener 9 dígitos, ej: 76.123.456-7"
        if (!limpio.all { it.isDigit() }) return "El RUT solo contiene números"
        return null
    }

    fun sucursalNombre(valor: String): String? {
        if (valor.isBlank()) return "El nombre de la sucursal es obligatorio"
        if (valor.trim().length < 2) return "Nombre muy corto"
        return null
    }

    fun sucursalDireccion(valor: String): String? {
        if (valor.isBlank()) return "La dirección es obligatoria"
        if (valor.trim().length < 5) return "Dirección muy corta, ej: Av. José Pedro Alt 800"
        return null
    }

    fun codigoMaquina(valor: String): String? {
        if (valor.isBlank()) return "El código es obligatorio"
        if (valor.trim().length < 3) return "Código muy corto, ej: REL-123"
        val regex = Regex("^[A-Za-z0-9\\-]+$")
        return if (!regex.matches(valor.trim())) "Solo letras, números y guion" else null
    }

    // Valor del trabajo ejecutado (opcional en el protocolo)
    fun precio(valor: String): String? {
        if (valor.isBlank()) return null
        val normalizado = valor.trim().replace(",", ".")
        if (normalizado.toDoubleOrNull() == null) return "Ingresa un valor numérico"
        if (normalizado.toDouble() < 0) return "El valor no puede ser negativo"
        return null
    }
}
