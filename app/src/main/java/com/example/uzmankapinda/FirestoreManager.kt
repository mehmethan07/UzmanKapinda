package com.example.uzmankapinda

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

object FirestoreManager {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // --- KULLANICI İŞLEMLERİ ---

    fun createUserProfile(name: String, email: String, onResult: (Boolean, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val user = UserModel(id = uid, name = name, email = email, role = "user")

        firestore.collection("users").document(uid).set(user)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun getCurrentUserProfile(onResult: (UserModel?, String?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onResult(null, "Oturum yok")
            return
        }

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onResult(doc.toObject(UserModel::class.java), null)
                } else {
                    onResult(null, "Profil yok")
                }
            }
            .addOnFailureListener { onResult(null, it.localizedMessage) }
    }

    fun updateUserName(newName: String, onResult: (Boolean, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).update("name", newName)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun adminUpdateUserName(userId: String, newName: String, onResult: (Boolean, String?) -> Unit) {
        firestore.collection("users").document(userId).update("name", newName)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun updateAccountFrozenStatus(isFrozen: Boolean, onResult: (Boolean, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).update("isFrozen", isFrozen)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun getAllUsers(onResult: (List<UserModel>?, String?) -> Unit) {
        firestore.collection("users").get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject(UserModel::class.java) }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(null, it.localizedMessage) }
    }

    fun updateUserFrozenStatus(userId: String, isFrozen: Boolean, onResult: (Boolean, String?) -> Unit) {
        firestore.collection("users").document(userId).update("isFrozen", isFrozen)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun deleteUser(userId: String, onResult: (Boolean, String?) -> Unit) {
        firestore.collection("users").document(userId).delete()
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    // --- HİZMETLER ---

    fun getAllServices(onResult: (List<ServiceModel>?, String?) -> Unit) {
        firestore.collection("services").get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ServiceModel::class.java)?.copy(id = doc.id)
                }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(null, it.localizedMessage) }
    }

    fun getServiceById(serviceId: String, onResult: (ServiceModel?, String?) -> Unit) {
        firestore.collection("services").document(serviceId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val service = doc.toObject(ServiceModel::class.java)
                    onResult(service?.copy(id = doc.id), null)
                } else {
                    onResult(null, "Servis bulunamadı")
                }
            }
            .addOnFailureListener { onResult(null, it.localizedMessage) }
    }

    fun addService(
        title: String, description: String, category: String, price: Double, city: String,
        stock: Int, imageUrl: String, onResult: (Boolean, String?) -> Unit
    ) {
        val ownerId = auth.currentUser?.uid ?: ""
        val service = ServiceModel(
            title = title, description = description, category = category,
            price = price, city = city, stock = stock, imageUrl = imageUrl, ownerId = ownerId
        )
        firestore.collection("services").add(service)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun updateService(
        serviceId: String, title: String, description: String, category: String, price: Double,
        city: String, stock: Int, imageUrl: String, onResult: (Boolean, String?) -> Unit
    ) {
        val updates = mapOf(
            "title" to title, "description" to description, "category" to category,
            "price" to price, "city" to city, "stock" to stock, "imageUrl" to imageUrl
        )
        firestore.collection("services").document(serviceId).update(updates)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun deleteService(serviceId: String, onResult: (Boolean, String?) -> Unit) {
        firestore.collection("services").document(serviceId).delete()
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    // --- SİPARİŞLER (STOK YÖNETİMLİ) ---

    fun createOrder(
        cartItems: List<CartItem>,
        address: String,
        note: String?,
        onResult: (Boolean, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        if (cartItems.isEmpty()) {
            onResult(false, "Sepet boş")
            return
        }

        val ordersRef = firestore.collection("orders").document()

        firestore.runTransaction { transaction ->

            // Stok Kontrolü ve Veri Hazırlama
            val snapshotList = cartItems.map { item ->
                val serviceRef = firestore.collection("services").document(item.service.id)
                val snapshot = transaction.get(serviceRef)

                if (!snapshot.exists()) {
                    throw FirebaseFirestoreException("Hizmet bulunamadı: ${item.service.title}", FirebaseFirestoreException.Code.ABORTED)
                }

                val currentStock = snapshot.getLong("stock")?.toInt() ?: 0
                if (currentStock < item.quantity) {
                    throw FirebaseFirestoreException("Yetersiz stok: ${item.service.title}", FirebaseFirestoreException.Code.ABORTED)
                }

                Triple(serviceRef, currentStock, item)
            }

            val totalPrice = cartItems.sumOf { it.service.price * it.quantity }

            val itemsData = cartItems.map { item ->
                mapOf(
                    "serviceId" to item.service.id,
                    "title" to item.service.title,
                    "price" to item.service.price,
                    "quantity" to item.quantity,
                    "city" to item.service.city
                )
            }

            val orderData = mapOf(
                "userId" to uid,
                "items" to itemsData,
                "totalPrice" to totalPrice,
                "address" to address,
                "note" to note,
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp()
            )

            // Yazma İşlemleri
            transaction.set(ordersRef, orderData)

            snapshotList.forEach { (ref, currentStock, item) ->
                val newStock = currentStock - item.quantity
                transaction.update(ref, "stock", newStock)
            }

        }.addOnSuccessListener {
            onResult(true, null)
        }.addOnFailureListener { e ->
            onResult(false, e.message ?: "Sipariş oluşturulamadı")
        }
    }

    fun getAllOrders(onResult: (List<OrderModel>?, String?) -> Unit) {
        firestore.collection("orders").get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject(OrderModel::class.java)?.copy(id = it.id) }
                    .sortedByDescending { it.createdAt?.toDate() }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(null, it.localizedMessage) }
    }

    fun getOrdersForCurrentUser(onResult: (List<OrderModel>?, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("orders").whereEqualTo("userId", uid).get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject(OrderModel::class.java)?.copy(id = it.id) }
                    .sortedByDescending { it.createdAt?.toDate() }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(null, it.localizedMessage) }
    }

    fun updateOrderStatus(orderId: String, newStatus: String, onResult: (Boolean, String?) -> Unit) {
        firestore.collection("orders").document(orderId).update("status", newStatus)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun cancelOrder(order: OrderModel, onResult: (Boolean, String?) -> Unit) {
        val batch = firestore.batch()
        val orderRef = firestore.collection("orders").document(order.id)

        // Durumu güncelle
        batch.update(orderRef, "status", "cancelled")

        // Stok iadesi
        order.items.forEach { item ->
            if (item.serviceId.isNotBlank()) {
                val serviceRef = firestore.collection("services").document(item.serviceId)
                batch.update(serviceRef, "stock", FieldValue.increment(item.quantity.toLong()))
            }
        }

        batch.commit()
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun completeOrderWithQr(orderId: String, onResult: (Boolean, String?) -> Unit) {
        val orderRef = firestore.collection("orders").document(orderId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(orderRef)
            if (!snapshot.exists()) {
                throw FirebaseFirestoreException("Sipariş bulunamadı", FirebaseFirestoreException.Code.NOT_FOUND)
            }

            val currentStatus = snapshot.getString("status") ?: ""
            if (currentStatus == "completed" || currentStatus == "cancelled") {
                throw FirebaseFirestoreException("Bu sipariş zaten işlem görmüş.", FirebaseFirestoreException.Code.ABORTED)
            }

            transaction.update(orderRef, "status", "completed")
        }.addOnSuccessListener {
            onResult(true, null)
        }.addOnFailureListener {
            onResult(false, it.message)
        }
    }

    // --- ONAY MEKANİZMASI ---

    fun submitOrderForApproval(orderId: String, onResult: (Boolean, String?) -> Unit) {
        firestore.collection("orders").document(orderId)
            .update(
                mapOf(
                    "status" to "pending_approval",
                    "submittedAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun approveOrder(orderId: String, onResult: (Boolean, String?) -> Unit) {
        firestore.collection("orders").document(orderId)
            .update("status", "completed")
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun rejectOrder(orderId: String, reason: String, onResult: (Boolean, String?) -> Unit) {
        firestore.collection("orders").document(orderId)
            .update(
                mapOf(
                    "status" to "pending",
                    "note" to "RED NEDENİ: $reason"
                )
            )
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }
}