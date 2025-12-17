package com.example.uzmankapinda

/**
 * Firestore'da tutulacak kullanıcı profili.
 */
data class UserModel(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    val createdAt: Long = System.currentTimeMillis(),
    val isFrozen: Boolean = false // Hesap dondurulmuş mu?
)

