package com.example.uzmankapinda

data class ServiceModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val city: String = "",
    val ownerId: String = "",
    val imageUrl: String = "",
    val stock: Int = 1
)