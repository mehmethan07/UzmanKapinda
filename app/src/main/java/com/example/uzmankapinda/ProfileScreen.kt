package com.example.uzmankapinda

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzmankapinda.ui.components.AppHeader
import com.example.uzmankapinda.ui.theme.UzmanKapindaTheme
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogoutClick: () -> Unit,
    onViewOrdersClick: () -> Unit
) {
    var user by remember { mutableStateOf<UserModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Diyaloglar
    var showFreezeDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEmailUpdateDialog by remember { mutableStateOf(false) }
    var showNameUpdateDialog by remember { mutableStateOf(false) }
    var showPasswordUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FirestoreManager.getCurrentUserProfile { u, error ->
            if (error != null) {
                isLoading = false
                scope.launch { snackbarHostState.showSnackbar(error) }
            } else {
                user = u
                isLoading = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(innerPadding)
        ) {

            AppHeader(title = "Profilim", subtitle = "Hesap detaylarını yönet", onBack = onBack)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(50.dp))
                }
            } else {
                val isAdmin = user?.role == "admin"

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // PROFİL AVATARI
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(86.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(42.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = user?.name ?: "İsimsiz Kullanıcı",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (isAdmin) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = "YÖNETİCİ",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = user?.email ?: "", fontSize = 14.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // HESAP BİLGİLERİ (İsim, Email, Şifre)
                    Text("Hesap Bilgileri", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column {
                            ProfileMenuItem(icon = Icons.Outlined.Edit, title = "Ad Soyad Düzenle", onClick = { showNameUpdateDialog = true }, endText = user?.name)
                            Divider(color = Color.LightGray.copy(alpha = 0.2f))
                            ProfileMenuItem(icon = Icons.Outlined.Email, title = "E-posta Değiştir", onClick = { showEmailUpdateDialog = true })
                            Divider(color = Color.LightGray.copy(alpha = 0.2f))
                            ProfileMenuItem(icon = Icons.Outlined.Lock, title = "Şifre Değiştir", onClick = { showPasswordUpdateDialog = true })
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // İŞLEMLER (Sadece Normal Kullanıcı İçin)
                    if (!isAdmin) {
                        Text("İşlemler", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column {
                                ProfileMenuItem(icon = Icons.Outlined.ShoppingCart, title = "Siparişlerim", onClick = onViewOrdersClick)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // GÜVENLİK / ÇIKIŞ
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column {
                            if (!isAdmin) {
                                ProfileMenuItem(
                                    icon = Icons.Outlined.Warning,
                                    title = "Hesabı Dondur",
                                    textColor = MaterialTheme.colorScheme.error,
                                    iconColor = MaterialTheme.colorScheme.error,
                                    onClick = { showFreezeDialog = true }
                                )
                                Divider(color = Color.LightGray.copy(alpha = 0.2f))
                            }

                            ProfileMenuItem(
                                icon = Icons.Outlined.ExitToApp,
                                title = "Çıkış Yap",
                                textColor = MaterialTheme.colorScheme.error,
                                iconColor = MaterialTheme.colorScheme.error,
                                onClick = { showLogoutDialog = true }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            // --- DİYALOGLAR ---

            // İsim Güncelleme Diyaloğu
            if (showNameUpdateDialog) {
                var tempName by remember { mutableStateOf(user?.name ?: "") }
                var isSaving by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { if(!isSaving) showNameUpdateDialog = false },
                    title = { Text("İsmini Düzenle") },
                    text = {
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("Ad Soyad") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (tempName.isBlank()) return@KeyboardActions
                                // Mantık confirmButton'da olduğu için buraya sadece boş kontrolü eklenmiştir
                            })
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (tempName.isBlank()) return@Button
                                isSaving = true
                                FirestoreManager.updateUserName(tempName.trim()) { success, error ->
                                    isSaving = false
                                    scope.launch {
                                        if (success) {
                                            user = user?.copy(name = tempName.trim())
                                            showNameUpdateDialog = false
                                            snackbarHostState.showSnackbar("İsim güncellendi")
                                        } else {
                                            snackbarHostState.showSnackbar(error ?: "Hata")
                                        }
                                    }
                                }
                            },
                            enabled = !isSaving
                        ) {
                            if(isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Kaydet")
                        }
                    },
                    dismissButton = { TextButton(onClick = { showNameUpdateDialog = false }) { Text("Vazgeç") } }
                )
            }

            // E-posta Güncelleme Diyaloğu
            if (showEmailUpdateDialog) {
                var newEmailInput by remember { mutableStateOf("") }
                var currentPasswordInput by remember { mutableStateOf("") }
                var isUpdating by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { if (!isUpdating) showEmailUpdateDialog = false },
                    title = { Text("E-posta Değiştir") },
                    text = {
                        Column {
                            Text("Güvenlik için şifreni gir.", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newEmailInput,
                                onValueChange = { newEmailInput = it },
                                label = { Text("Yeni E-posta") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = currentPasswordInput,
                                onValueChange = { currentPasswordInput = it },
                                label = { Text("Mevcut Şifre") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                                // KeyboardActions kaldırıldı, mantık ConfirmButton'da.
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newEmailInput.isBlank() || currentPasswordInput.isBlank()) return@Button
                                isUpdating = true
                                AuthManager.updateEmailWithPassword(currentPasswordInput, newEmailInput.trim()) { success, error ->
                                    isUpdating = false
                                    scope.launch {
                                        if (success) {
                                            showEmailUpdateDialog = false
                                            snackbarHostState.showSnackbar("Doğrulama maili ${newEmailInput} adresine gönderildi. Lütfen onaylayıp tekrar giriş yap.")
                                        } else {
                                            snackbarHostState.showSnackbar(error ?: "Hata")
                                        }
                                    }
                                }
                            },
                            enabled = !isUpdating
                        ) { Text("Onay Maili Gönder") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEmailUpdateDialog = false }) { Text("Vazgeç") }
                    }
                )
            }

            // Şifre Güncelleme Diyaloğu
            if (showPasswordUpdateDialog) {
                var currentPass by remember { mutableStateOf("") }
                var newPass by remember { mutableStateOf("") }
                var isUpdating by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { if(!isUpdating) showPasswordUpdateDialog = false },
                    title = { Text("Şifre Değiştir") },
                    text = {
                        Column {

                            OutlinedTextField(
                                value = currentPass,
                                onValueChange = { currentPass = it },
                                label = { Text("Mevcut Şifre") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newPass,
                                onValueChange = { newPass = it },
                                label = { Text("Yeni Şifre") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (currentPass.isBlank() || newPass.isBlank()) return@Button
                                isUpdating = true
                                AuthManager.updatePasswordSecurely(currentPass, newPass) { success, error ->
                                    isUpdating = false
                                    scope.launch {
                                        if (success) {
                                            showPasswordUpdateDialog = false
                                            snackbarHostState.showSnackbar("Şifre güncellendi")
                                        } else {
                                            snackbarHostState.showSnackbar(error ?: "Hata")
                                        }
                                    }
                                }
                            },
                            enabled = !isUpdating
                        ) {
                            Text("Güncelle")
                        }
                    },
                    dismissButton = { TextButton(onClick = { showPasswordUpdateDialog = false }) { Text("Vazgeç") } }
                )
            }

            // Hesap Dondurma Diyaloğu
            if (showFreezeDialog) {
                AlertDialog(
                    onDismissRequest = { showFreezeDialog = false },
                    title = { Text("Hesabı Dondur") },
                    text = { Text("Hesabın dondurulacak ve çıkış yapılacak. Emin misin?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showFreezeDialog = false
                            FirestoreManager.updateAccountFrozenStatus(true) { success, _ ->
                                if (success) onLogoutClick()
                            }
                        }) { Text("Evet, Dondur", color = Color.Red) }
                    },
                    dismissButton = { TextButton(onClick = { showFreezeDialog = false }) { Text("Vazgeç") } }
                )
            }

            // Çıkış Diyaloğu
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Çıkış Yap") },
                    text = { Text("Uygulamadan çıkış yapmak istiyor musun?") },
                    confirmButton = { TextButton(onClick = { showLogoutDialog = false; onLogoutClick() }) { Text("Çıkış Yap", color = Color.Red) } },
                    dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Vazgeç") } }
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    endText: String? = null,
    textColor: Color = Color.Black,
    iconColor: Color = Color.Gray
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (endText != null) {
                Text(
                    text = endText,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}