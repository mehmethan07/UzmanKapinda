package com.example.uzmankapinda

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzmankapinda.ui.components.AppHeader
import kotlinx.coroutines.launch

@Composable
fun UserOrdersScreen(
    onBack: () -> Unit
) {
    var orders by remember { mutableStateOf<List<OrderModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedOrderForScan by remember { mutableStateOf<OrderModel?>(null) }
    var selectedOrderForCancel by remember { mutableStateOf<OrderModel?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun loadOrders() {
        isLoading = true
        FirestoreManager.getOrdersForCurrentUser { list, error ->
            isLoading = false
            if (error != null) {
                errorMessage = error
            } else {
                orders = list ?: emptyList()
            }
        }
    }

    LaunchedEffect(Unit) { loadOrders() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AppHeader(title = "Siparişlerim", subtitle = "Görevlerini yönet", onBack = onBack)
                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(50.dp))
                    }
                } else if (orders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Henüz siparişin yok.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(orders) { order ->
                            UserOrderCard(
                                order = order,
                                onScanClick = { selectedOrderForScan = order },
                                onCancelClick = { selectedOrderForCancel = order }
                            )
                        }
                    }
                }
            }

            // QR TARAMA SİMÜLASYONU
            if (selectedOrderForScan != null) {
                val order = selectedOrderForScan!!
                var scanInput by remember { mutableStateOf("") }
                var isSubmitting by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { selectedOrderForScan = null },
                    title = { Text("Teslimat Doğrulama") },
                    text = {
                        Column {
                            Text(
                                text = "Görevi tamamlamak için müşteri veya mekandaki QR Kodu okutunuz.",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = scanInput,
                                onValueChange = { scanInput = it },
                                label = { Text("QR Kod (Sipariş ID)") },
                                placeholder = { Text("Simülasyon: ID'yi girin") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                            )
                            if (isSubmitting) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (scanInput.trim() == order.id) {
                                    isSubmitting = true
                                    FirestoreManager.submitOrderForApproval(order.id) { success, error ->
                                        isSubmitting = false
                                        scope.launch {
                                            if (success) {
                                                selectedOrderForScan = null
                                                snackbarHostState.showSnackbar("Teslim edildi! Yönetici onayı bekleniyor.")
                                                loadOrders()
                                            } else {
                                                snackbarHostState.showSnackbar(error ?: "Hata")
                                            }
                                        }
                                    }
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Hatalı QR Kod! Lütfen doğru kodu giriniz.") }
                                }
                            },
                            enabled = !isSubmitting
                        ) { Text("Doğrula ve Teslim Et") }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedOrderForScan = null }) { Text("İptal") }
                    }
                )
            }

            // İPTAL ONAYI
            if (selectedOrderForCancel != null) {
                val order = selectedOrderForCancel!!
                AlertDialog(
                    onDismissRequest = { selectedOrderForCancel = null },
                    title = { Text("Görevi İptal Et") },
                    text = { Text("Bu görevi iptal etmek istediğine emin misin? Stok geri yüklenecektir.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                FirestoreManager.cancelOrder(order) { success, _ ->
                                    selectedOrderForCancel = null
                                    if (success) loadOrders()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("Evet, İptal Et") }
                    },
                    dismissButton = { TextButton(onClick = { selectedOrderForCancel = null }) { Text("Vazgeç") } }
                )
            }
        }
    }
}

@Composable
fun UserOrderCard(
    order: OrderModel,
    onScanClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val dateText = order.createdAt?.toDate()?.let { DateFormat.format("dd.MM.yyyy", it) } ?: "-"

    val statusLabel = when (order.status) {
        "pending" -> "Aktif / Devam Ediyor"
        "pending_approval" -> "Onay Bekliyor"
        "completed" -> "Tamamlandı"
        "cancelled" -> "İptal Edildi"
        else -> order.status
    }

    val statusColor = when (order.status) {
        "pending" -> Color(0xFF1976D2)
        "pending_approval" -> Color(0xFFF57C00)
        "completed" -> Color(0xFF388E3C)
        "cancelled" -> Color.Red
        else -> Color.Gray
    }

    val canDeliver = order.status == "pending"
    val canCancel = order.status == "pending"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sipariş #${order.id.takeLast(5)}", fontSize = 12.sp, color = Color.Gray)
                Text(dateText.toString(), fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = order.items.firstOrNull()?.title ?: "Hizmet",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = statusLabel,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (order.status == "pending_approval") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Yönetici onayı bekleniyor. Ödeme onaydan sonra yapılacaktır.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.LightGray.copy(0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.totalPrice} TL",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canCancel) {
                        OutlinedButton(
                            onClick = onCancelClick,
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) { Text("İptal", fontSize = 12.sp) }
                    }

                    if (canDeliver) {
                        Button(
                            onClick = onScanClick,
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Teslim Et", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}