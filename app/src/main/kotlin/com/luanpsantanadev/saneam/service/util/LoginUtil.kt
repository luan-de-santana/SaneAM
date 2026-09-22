package com.luanpsantanadev.saneam.service.util

import android.util.Patterns

fun emailValido(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

fun senhaForte(password: String): Boolean {
    if (password.length < 6) return false
    var hasLetter = false
    var hasDigit = false

    for (c in password.toCharArray()) {
        if (Character.isSpaceChar(c)) return false
        if (Character.isLetter(c)) hasLetter = true
        if (Character.isDigit(c)) hasDigit = true
        if (hasLetter && hasDigit) return true
    }
    return false
}