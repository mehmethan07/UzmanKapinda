package com.example.uzmankapinda

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object AuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun register(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    onResult(false, "Kullanıcı ID hatası")
                    return@addOnSuccessListener
                }

                val userMap = mapOf(
                    "id" to uid,
                    "name" to name,
                    "email" to email,
                    "role" to "user",
                    "createdAt" to System.currentTimeMillis(),
                    "isFrozen" to false
                )

                firestore.collection("users")
                    .document(uid)
                    .set(userMap)
                    .addOnSuccessListener {
                        onResult(true, null)
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.localizedMessage)
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage)
            }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    onResult(false, false, "ID alınamadı")
                    return@addOnSuccessListener
                }

                firestore.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        if (!doc.exists()) {
                            auth.signOut()
                            onResult(false, false, "Kullanıcı kaydı bulunamadı")
                            return@addOnSuccessListener
                        }

                        val isFrozen = doc.getBoolean("isFrozen") == true
                        if (isFrozen) {
                            auth.signOut()
                            onResult(false, false, "Hesabınız erişime kapatılmıştır.")
                            return@addOnSuccessListener
                        }

                        val role = doc.getString("role") ?: "user"
                        val isAdmin = role == "admin"

                        onResult(true, isAdmin, null)
                    }
                    .addOnFailureListener { e ->
                        auth.signOut()
                        onResult(false, false, e.localizedMessage)
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, false, e.localizedMessage)
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser

    fun updatePassword(newPassword: String, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "Oturum kapalı")
            return
        }

        user.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Hata oluştu")
                }
            }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            onResult(false, "Mail adresi boş olamaz")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage)
            }
    }

    fun updateEmailWithPassword(
        currentPassword: String,
        newEmail: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null || user.email == null) {
            onResult(false, "Oturum yok")
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.verifyBeforeUpdateEmail(newEmail)
                    .addOnSuccessListener {
                        onResult(true, null)
                    }
                    .addOnFailureListener { e ->
                        onResult(false, "Mail gönderilemedi: ${e.localizedMessage}")
                    }
            }
            .addOnFailureListener {
                onResult(false, "Şifre yanlış veya oturum zaman aşımına uğradı.")
            }
    }

    fun updatePasswordSecurely(
        currentPassword: String,
        newPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null || user.email == null) {
            onResult(false, "Kullanıcı bulunamadı")
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        onResult(true, null)
                    }
                    .addOnFailureListener { e ->
                        onResult(false, "Güncelleme hatası: ${e.localizedMessage}")
                    }
            }
            .addOnFailureListener {
                onResult(false, "Mevcut şifre hatalı.")
            }
    }
}