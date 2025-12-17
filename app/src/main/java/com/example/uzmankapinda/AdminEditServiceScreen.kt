package com.example.uzmankapinda

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzmankapinda.ui.components.AppHeader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditServiceScreen(
    serviceId: String,
    onServiceUpdated: () -> Unit,
    onCancel: () -> Unit
) {
    // Form verileri için state tanımları
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Dropdown state'leri
    var category by remember { mutableStateOf("") }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    var city by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("") }
    var selectedImageId by remember { mutableStateOf("home") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Düzenlenecek veriyi ID ile çekiyoruz
    LaunchedEffect(serviceId) {
        FirestoreManager.getServiceById(serviceId) { service, error ->
            if (service != null) {
                title = service.title
                description = service.description
                category = service.category
                city = service.city
                priceText = service.price.toString()
                stockText = service.stock.toString()

                selectedImageId = if (service.imageUrl.isNotBlank()) service.imageUrl else "home"
                isLoading = false
            } else {
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar(error ?: "Veri yüklenemedi")
                }
            }
        }
    }

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

                AppHeader(
                    title = "Görevi Düzenle",
                    subtitle = "Detayları güncelle",
                    onBack = onCancel
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(20.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {

                                Text(
                                    text = "Görev İkonu",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(ServiceIcons.list) { (id, icon) ->
                                        val isSelected = id == selectedImageId
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary
                                                    else Color.LightGray.copy(alpha = 0.2f)
                                                )
                                                .clickable { selectedImageId = id }
                                                .border(
                                                    width = if (isSelected) 2.dp else 0.dp,
                                                    color = Color.White,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isSelected) Color.White else Color.Gray,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("Görev Başlığı") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("Görev Detayları") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 80.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Kategori Seçimi
                                ExposedDropdownMenuBox(
                                    expanded = isCategoryExpanded,
                                    onExpandedChange = { isCategoryExpanded = !isCategoryExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = category,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("İş Kategorisi") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        modifier = Modifier.fillMaxWidth().menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = isCategoryExpanded,
                                        onDismissRequest = { isCategoryExpanded = false }
                                    ) {
                                        ServiceIcons.categories.forEach { selectionOption ->
                                            DropdownMenuItem(
                                                text = { Text(text = selectionOption) },
                                                onClick = {
                                                    category = selectionOption
                                                    isCategoryExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = { Text("Konum") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = priceText,
                                        onValueChange = { priceText = it },
                                        label = { Text("Ücret (TL)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = stockText,
                                        onValueChange = { stockText = it },
                                        label = { Text("Kontenjan") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        val price = priceText.toDoubleOrNull()
                                        val stock = stockText.toIntOrNull()

                                        if (price != null && stock != null) {
                                            isSaving = true
                                            FirestoreManager.updateService(
                                                serviceId, title, description, category, price, city, stock, selectedImageId
                                            ) { success, _ ->
                                                isSaving = false
                                                if (success) {
                                                    onServiceUpdated()
                                                }
                                            }
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Lütfen sayısal alanları kontrol ediniz")
                                            }
                                        }
                                    },
                                    enabled = !isSaving,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(if (isSaving) "Kaydediliyor..." else "Değişiklikleri Kaydet")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}