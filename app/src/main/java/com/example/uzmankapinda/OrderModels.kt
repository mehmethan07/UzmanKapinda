package com.example.uzmankapinda

import com.google.firebase.Timestamp

data class OrderItemModel(
    val serviceId: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val city: String = ""
)

data class OrderModel(
    val id: String = "",
    val userId: String = "",
    val items: List<OrderItemModel> = emptyList(),
    val totalPrice: Double = 0.0,
    val address: String = "",
    val note: String? = null,
    val status: String = "pending",
    val createdAt: Timestamp? = null
)
