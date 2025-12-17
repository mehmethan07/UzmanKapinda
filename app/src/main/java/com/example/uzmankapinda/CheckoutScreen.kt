package com.example.uzmankapinda

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzmankapinda.ui.components.AppHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    onBack: () -> Unit,
    onOrderSuccess: () -> Unit,
    isUsd: Boolean,
    usdRate: Double?
) {
    val totalPrice = cartItems.sumOf { it.service.price * it.quantity }
    val displayTotal = if (isUsd && usdRate != null) {
        "${"%.2f".format(totalPrice * usdRate)} $"
    } else {
        "$totalPrice TL"
    }

    var phoneNumber by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var iban by remember { mutableStateOf("TR") }
    var accountHolder by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("Başvuru İletiliyor...") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    if (isLoading) {
        CustomLoadingDialog(loadingText = loadingMessage)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF5F6F8)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            AppHeader(
                title = "Görevi Kabul Et",
                subtitle = "Ödeme bilgilerini gir",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Toplam Kazanç",
                                fontSize = 14.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = displayTotal,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "İletişim",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 11) phoneNumber = it },
                            label = { Text("Telefon Numarası") },
                            placeholder = { Text("05XX...") },
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Ödeme Bilgileri (IBAN)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = accountHolder,
                            onValueChange = { accountHolder = it },
                            label = { Text("Ad Soyad (Hesap Sahibi)") },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = iban,
                            onValueChange = { if (it.length <= 32) iban = it.uppercase() },
                            label = { Text("IBAN Numarası") },
                            placeholder = { Text("TR00...") },
                            leadingIcon = { Icon(Icons.Default.AccountBalance, null, tint = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ödemeler görev onaylandıktan sonra yapılır.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Not (Opsiyonel)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Teslim süresi veya özel notlar...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (phoneNumber.length < 10) {
                            scope.launch { snackbarHostState.showSnackbar("Geçerli bir telefon giriniz") }
                            return@Button
                        }

                        if (iban.length < 15 || !iban.startsWith("TR")) {
                            scope.launch { snackbarHostState.showSnackbar("Geçerli bir TR IBAN giriniz") }
                            return@Button
                        }

                        if (accountHolder.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Hesap sahibi adı giriniz") }
                            return@Button
                        }

                        isLoading = true
                        val paymentInfo = "Tel: $phoneNumber \nIBAN: $iban \nSahibi: $accountHolder"

                        FirestoreManager.createOrder(
                            cartItems,
                            paymentInfo,
                            note.takeIf { it.isNotBlank() }
                        ) { success, error ->
                            scope.launch {
                                if (!success) {
                                    isLoading = false
                                    snackbarHostState.showSnackbar(error ?: "Hata")
                                } else {
                                    loadingMessage = "Görev Alındı!"
                                    delay(1000)
                                    isLoading = false
                                    onOrderSuccess()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text("Görevi Kabul Et", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}