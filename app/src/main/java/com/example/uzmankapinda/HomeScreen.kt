package com.example.uzmankapinda

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzmankapinda.ui.theme.UzmanKapindaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddToCart: (ServiceModel) -> Unit,
    onGoToCart: () -> Unit,
    cartItemCount: Int,
    onServiceClick: (ServiceModel) -> Unit,
    onProfileClick: () -> Unit,
    isUsd: Boolean,
    onCurrencyToggle: () -> Unit,
    usdRate: Double?
) {
    var allServices by remember { mutableStateOf<List<ServiceModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tümü") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()

    fun loadData(isRefresh: Boolean = false) {
        if (isRefresh) isRefreshing = true else isLoading = true

        FirestoreManager.getAllServices { list, error ->
            if (list != null) {
                allServices = list
            }
            isLoading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    val filteredServices = allServices.filter { service ->
        val catMatch = if (selectedCategory == "Tümü") true else service.category.equals(selectedCategory, ignoreCase = true)
        val searchMatch = if (searchText.isBlank()) true else service.title.contains(searchText, true) || service.city.contains(searchText, true)
        catMatch && searchMatch
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF9FAFB),
        floatingActionButton = {
            if (cartItemCount > 0) {
                FloatingActionButton(
                    onClick = onGoToCart,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "$cartItemCount", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            HomeScreenHeader(
                isUsd = isUsd,
                searchText = searchText,
                onSearchChange = { searchText = it },
                onCurrencyToggle = onCurrencyToggle,
                onProfileClick = onProfileClick
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { loadData(isRefresh = true) },
                state = pullRefreshState,
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                ) {

                    item {
                        val finalCategories = listOf("Tümü") + ServiceIcons.categories

                        CategorySelector(
                            categories = finalCategories,
                            selectedCategory = selectedCategory,
                            onCategorySelect = { selectedCategory = it }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if(selectedCategory == "Tümü") "Popüler Hizmetler" else "$selectedCategory Hizmetleri",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "${filteredServices.size} sonuç",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (filteredServices.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.SearchOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(50.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Hizmet bulunamadı.", color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(filteredServices) { service ->
                            ModernServiceCard(
                                service = service,
                                onClick = { onServiceClick(service) },
                                onAddToCart = { onAddToCart(service) },
                                isUsd = isUsd,
                                usdRate = usdRate
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenHeader(
    isUsd: Boolean,
    searchText: String,
    onSearchChange: (String) -> Unit,
    onCurrencyToggle: () -> Unit,
    onProfileClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                )
            )
            .padding(bottom = 24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Merhaba 👋",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Uzman Kapında",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { onCurrencyToggle() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isUsd) "USD ($)" else "TRY (₺)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier
                            .background(Color.White, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profil",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = searchText,
                onValueChange = onSearchChange,
                placeholder = { Text("Hizmet veya şehir ara...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                })
            )
        }
    }
}

@Composable
fun CategorySelector(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onCategorySelect(category) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFEDF2F7))
                            .border(
                                width = 1.dp,
                                color = if(isSelected) Color.Transparent else Color.LightGray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when(category) {
                            "Tümü" -> Icons.Default.Apps
                            "Temizlik" -> Icons.Default.CleaningServices
                            "Tamirat" -> Icons.Default.Build
                            "Tesisat" -> Icons.Default.Plumbing
                            "Boya" -> Icons.Default.FormatPaint
                            "Elektrik" -> Icons.Default.ElectricBolt
                            "Nakliye" -> Icons.Default.LocalShipping
                            "Bilgisayar" -> Icons.Default.Computer
                            "Güvenlik" -> Icons.Default.Security
                            "Bahçe" -> Icons.Default.Grass
                            else -> Icons.Default.Category
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = category,
                            tint = if (isSelected) Color.White else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ModernServiceCard(
    service: ServiceModel,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    isUsd: Boolean,
    usdRate: Double?
) {
    val isOutOfStock = service.stock <= 0
    val displayPrice = if (isUsd && usdRate != null) {
        val priceInUsd = service.price * usdRate
        "${"%.2f".format(priceInUsd)} $"
    } else {
        "${service.price} TL"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = ServiceIcons.getIcon(service.imageUrl),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = service.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isOutOfStock) {
                        Surface(
                            color = Color.Red.copy(alpha=0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "TÜKENDİ",
                                color = Color.Red,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(text = service.city, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color.LightGray.copy(0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = service.category,
                            fontSize = 10.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(horizontal=4.dp, vertical=2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayPrice,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Button(
                        onClick = onAddToCart,
                        enabled = !isOutOfStock,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.LightGray
                        )
                    ) {
                        Text(if(isOutOfStock) "Stok Yok" else "Sepete Ekle", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}