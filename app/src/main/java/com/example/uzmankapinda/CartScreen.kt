package com.example.uzmankapinda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzmankapinda.ui.components.AppHeader

data class Service(
    val title: String,
    val city: String,
    val stock: Int,
    val price: Double
)

data class CartItem(
    val service: ServiceModel,
    val quantity: Int
)

@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    onIncrease: (CartItem) -> Unit,
    onDecrease: (CartItem) -> Unit,
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    isUsd: Boolean,
    usdRate: Double?
) {
    val totalPrice = cartItems.sumOf { it.service.price * it.quantity }

    // Kur seçimine göre formatlama
    val displayTotal = if (isUsd && usdRate != null) {
        "${"%.2f".format(totalPrice * usdRate)} $"
    } else {
        "${"%.2f".format(totalPrice)} TL"
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AppHeader(title = "Sepetim", subtitle = "Seçtiğin hizmetler", onBack = onBack)

                Spacer(modifier = Modifier.height(12.dp))

                if (cartItems.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Sepetin boş.", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Anasayfadan hizmet ekleyebilirsin.", fontSize = 13.sp, color = Color.Gray)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(cartItems) { item ->
                                CartItemCard(
                                    item = item,
                                    onIncrease = { onIncrease(item) },
                                    onDecrease = { onDecrease(item) },
                                    isUsd = isUsd,
                                    usdRate = usdRate
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Alt Toplam Kartı
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Toplam", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = displayTotal,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Toplam hizmet: ${cartItems.sumOf { it.quantity }}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onCheckout,
                                    enabled = totalPrice > 0.0,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Siparişi Tamamla", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    isUsd: Boolean,
    usdRate: Double?
) {
    val isMaxStockReached = item.quantity >= item.service.stock
    val lineTotal = item.service.price * item.quantity

    // Birim Fiyat Gösterimi
    val displayPrice = if (isUsd && usdRate != null) {
        "${"%.2f".format(item.service.price * usdRate)} $"
    } else {
        "${"%.2f".format(item.service.price)} TL"
    }

    // Satır Toplamı Gösterimi
    val displayLineTotal = if (isUsd && usdRate != null) {
        "${"%.2f".format(lineTotal * usdRate)} $"
    } else {
        "${"%.2f".format(lineTotal)} TL"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Text(item.service.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Şehir: ${item.service.city}", fontSize = 12.sp, color = Color.Gray)

            if (isMaxStockReached) {
                Text("Maksimum stok limiti", fontSize = 10.sp, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Birim: $displayPrice", fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onDecrease,
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("-")
                    }

                    Text(
                        "${item.quantity}",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedButton(
                        onClick = onIncrease,
                        enabled = !isMaxStockReached,
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("+")
                    }
                }

                Text(
                    text = displayLineTotal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}