package com.example.appmovil.domain.validation

import android.util.Patterns // Patrón estándar de Android para emails (Ej.8 Validators.kt)

// Valida que el email no esté vacío y cumpla patrón de email
fun validateEmail(email: String): String? {
    if (email.isBlank()) return "El email es obligatorio"
    val ok = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    return if (!ok) "Formato de email inválido" else null
}

// Valida que el nombre contenga solo letras y espacios (sin números)
fun validateNameLettersOnly(name: String): String? {
    if (name.isBlank()) return "El nombre es obligatorio"
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
    return if (!regex.matches(name)) "Solo letras y espacios" else null
}

// Valida que el teléfono tenga solo dígitos y longitud razonable (opcional en Induztek)
fun validatePhoneDigitsOnly(phone: String): String? {
    if (phone.isBlank()) return null // Teléfono opcional para el técnico
    if (!phone.all { it.isDigit() }) return "Solo números"
    if (phone.length !in 8..15) return "Debe tener entre 8 y 15 dígitos"
    return null
}

// Valida seguridad de la contraseña (mín. 8, mayús, minús, número y símbolo; sin espacios)
fun validateStrongPassword(pass: String): String? {
    if (pass.isBlank()) return "La contraseña es obligatoria"
    if (pass.length < 8) return "Mínimo 8 caracteres"
    if (!pass.any { it.isUpperCase() }) return "Debe incluir una mayúscula"
    if (!pass.any { it.isLowerCase() }) return "Debe incluir una minúscula"
    if (!pass.any { it.isDigit() }) return "Debe incluir un número"
    if (!pass.any { !it.isLetterOrDigit() }) return "Debe incluir un símbolo"
    if (pass.contains(' ')) return "No debe contener espacios"
    return null
}

// Valida que la confirmación coincida con la contraseña
fun validateConfirm(pass: String, confirm: String): String? {
    if (confirm.isBlank()) return "Confirma tu contraseña"
    return if (pass != confirm) "Las contraseñas no coinciden" else null
}

// Valida valores de medición del protocolo en terreno (caso Induztek 3.2)
fun validateMedicion(valor: String): String? {
    if (valor.isBlank()) return "Debe registrar los valores de medición para guardar el protocolo"
    if (valor.trim().length < 3) return "Medición muy corta, ej: 2.8 GΩ"
    return null
}
