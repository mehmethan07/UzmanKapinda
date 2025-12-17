package com.example.uzmankapinda

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.uzmankapinda.ui.components.AppHeader
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminOrdersScreen(
    onBack: () -> Unit
) {
    var orders by remember { mutableStateOf<List<OrderModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Diyalog state'leri
    var orderToReject by remember { mutableStateOf<OrderModel?>(null) }
    var rejectReason by remember { mutableStateOf("") }
    var orderToShowQr by remember { mutableStateOf<OrderModel?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    fun loadData() {
        FirestoreManager.getAllOrders { list, _ ->
            orders = list ?: emptyList()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadData() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF5F6F8)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AppHeader(
                    title = "İş Yönetimi",
                    subtitle = "Onay bekleyen ve aktif görevler",
                    onBack = onBack
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (orders.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Kayıt bulunamadı.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(orders) { order ->
                            AdminOrderCard(
                                order = order,
                                onCopy = { clipboardManager.setText(AnnotatedString(it)) },
                                onApprove = {
                                    FirestoreManager.approveOrder(order.id) { success, _ ->
                                        if (success) loadData()
                                    }
                                },
                                onRejectClick = {
                                    orderToReject = order
                                    rejectReason = ""
                                },
                                onShowQrClick = {
                                    orderToShowQr = order
                                }
                            )
                        }
                    }
                }
            }

            // Reddetme Diyaloğu
            if (orderToReject != null) {
                AlertDialog(
                    onDismissRequest = { orderToReject = null },
                    title = { Text("Görevi Reddet") },
                    text = {
                        Column {
                            Text("Bu işi neden reddediyorsunuz? (İşçiye geri gönderilecek)")
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = rejectReason,
                                onValueChange = { rejectReason = it },
                                label = { Text("Red Nedeni") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (rejectReason.isNotBlank()) {
                                    FirestoreManager.rejectOrder(
                                        orderToReject!!.id, rejectReason
                                    ) { success, _ ->
                                        if (success) {
                                            orderToReject = null
                                            loadData()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("Reddet ve Geri Gönder") }
                    },
                    dismissButton = {
                        TextButton(onClick = { orderToReject = null }) { Text("İptal") }
                    }
                )
            }

            // Müşteri QR Kodu Diyaloğu
            if (orderToShowQr != null) {
                val order = orderToShowQr!!
                val qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${order.id}"

                AlertDialog(
                    onDismissRequest = { orderToShowQr = null },
                    title = {
                        Text("Müşteri / Konum QR Kodu", textAlign = TextAlign.Center)
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                AsyncImage(
                                    model = qrCodeUrl,
                                    contentDescription = "QR Kod",
                                    modifier = Modifier
                                        .size(200.dp)
                                        .padding(10.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tıklanabilir ID Alanı
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                modifier = Modifier.clickable {
                                    clipboardManager.setText(AnnotatedString(order.id))
                                    Toast.makeText(context, "ID Kopyalandı", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text(
                                    text = "ID: ${order.id} (Kopyala)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { orderToShowQr = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Kapat")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AdminOrderCard(
    order: OrderModel,
    onCopy: (String) -> Unit,
    onApprove: () -> Unit,
    onRejectClick: () -> Unit,
    onShowQrClick: () -> Unit
) {
    val date = order.createdAt?.toDate()
    val formattedDate = if (date != null) {
        SimpleDateFormat("dd MMM, HH:mm", Locale("tr")).format(date)
    } else "-"

    val (statusText, statusColor, containerColor) = when (order.status) {
        "pending" -> Triple("Aktif (Çalışılıyor)", Color(0xFF1976D2), Color(0xFFE3F2FD))
        "pending_approval" -> Triple("ONAY BEKLİYOR", Color(0xFFE65100), Color(0xFFFFE0B2))
        "completed" -> Triple("Tamamlandı", Color(0xFF2E7D32), Color(0xFFE8F5E9))
        "cancelled" -> Triple("İptal", Color.Gray, Color(0xFFEEEEEE))
        else -> Triple(order.status, Color.Gray, Color.White)
    }

    val showActions = order.status == "pending_approval"
    val showQrButton = order.status == "pending"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Kart Başlığı (Tarih ve Durum)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(formattedDate, fontSize = 12.sp, color = Color.Gray)
                }
                Surface(
                    color = containerColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = order.items.firstOrNull()?.title ?: "Hizmet",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Uzman ID: ${order.userId.takeLast(6)}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            if (!order.note.isNullOrBlank() && order.note.contains("RED")) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = order.note,
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider(
                color = Color.LightGray.copy(0.2f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Alt Kısım (Fiyat ve Butonlar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.totalPrice} TL",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    if (showQrButton) {
                        OutlinedButton(
                            onClick = onShowQrClick,
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.QrCode,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mekan Kodu", fontSize = 12.sp)
                        }
                    }

                    if (showActions) {
                        OutlinedButton(
                            onClick = onRejectClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) { Text("Reddet") }

                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Onayla")
                        }
                    }
                }
            }
        }
    }
}