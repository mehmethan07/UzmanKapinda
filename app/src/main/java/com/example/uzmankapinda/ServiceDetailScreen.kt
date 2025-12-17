package com.example.uzmankapinda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzmankapinda.ui.theme.UzmanKapindaTheme
import kotlinx.coroutines.launch

@Composable
fun ServiceDetailScreen(
    serviceId: String,
    onBack: () -> Unit,
    onAddToCart: (ServiceModel) -> Unit,
    isUsd: Boolean,
    usdRate: Double?
) {
    var service by remember { mutableStateOf<ServiceModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(serviceId) {
        FirestoreManager.getServiceById(serviceId) { result, _ ->
            service = result
            isLoading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (service != null && !isLoading) {
                val s = service!!
                val isOutOfStock = s.stock.toInt() <= 0

                val displayPrice = if (isUsd && usdRate != null) {
                    val priceInUsd = s.price.toDouble() * usdRate
                    "${"%.2f".format(priceInUsd)} $"
                } else {
                    "${s.price.toDouble()} TL"
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Toplam Fiyat", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = displayPrice,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = {
                                if (!isOutOfStock) {
                                    onAddToCart(s)
                                    scope.launch { snackbarHostState.showSnackbar("Sepete eklendi") }
                                }
                            },
                            enabled = !isOutOfStock,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(54.dp)
                                .width(180.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOutOfStock) Color.Gray else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = if (isOutOfStock) "Tükendi" else "Sepete Ekle",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(50.dp))
            } else if (service == null) {
                Text("Hizmet Bulunamadı", modifier = Modifier.align(Alignment.Center))
            } else {
                val s = service!!
                val isOutOfStock = s.stock.toInt() <= 0

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)))
                        )
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(top = 40.dp, start = 16.dp)
                                .background(Color.Black.copy(0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, "Geri", tint = Color.White)
                        }
                        Icon(
                            imageVector = ServiceIcons.getIcon(s.imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.Center),
                            tint = Color.White.copy(0.9f)
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-30).dp),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = s.category.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                if (isOutOfStock) {
                                    Text(
                                        text = "STOK TÜKENDİ",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    Text(
                                        text = "${s.stock.toInt()} Adet Kaldı",
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = s.title,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 32.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.LocationOn,
                                    null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(s.city, fontSize = 14.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Divider(color = Color.LightGray.copy(0.3f))
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Hizmet Hakkında",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = s.description,
                                fontSize = 15.sp,
                                lineHeight = 24.sp,
                                color = Color.DarkGray.copy(0.9f)
                            )
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}