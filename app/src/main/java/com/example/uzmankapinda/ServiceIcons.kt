package com.example.uzmankapinda

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object ServiceIcons {
    // Kategori Listesi
    val categories = listOf(
        "Temizlik",
        "Tamirat",
        "Tesisat",
        "Boya",
        "Elektrik",
        "Nakliye",
        "Bilgisayar",
        "Güvenlik",
        "Bahçe",
        "Diğer"
    )

    // Seçilebilir ikon listesi (ID, İkon)
    val list = listOf(
        "home" to Icons.Filled.Home,
        "cleaning" to Icons.Filled.CleaningServices,
        "repair" to Icons.Filled.Build,
        "plumbing" to Icons.Filled.Plumbing,
        "painting" to Icons.Filled.FormatPaint,
        "electric" to Icons.Filled.ElectricBolt,
        "moving" to Icons.Filled.LocalShipping,
        "computer" to Icons.Filled.Computer,
        "security" to Icons.Filled.Security,
        "garden" to Icons.Filled.Grass,
        "pest" to Icons.Filled.PestControl,
        "laundry" to Icons.Filled.LocalLaundryService,
        "child_care" to Icons.Filled.ChildCare,
        "pet" to Icons.Filled.Pets,
        "ac_unit" to Icons.Filled.AcUnit,
        "car_repair" to Icons.Filled.CarRepair
    )

    // ID verince İkonu döndüren fonksiyon
    fun getIcon(id: String): ImageVector {
        return list.find { it.first == id }?.second ?: Icons.Filled.Home
    }
}