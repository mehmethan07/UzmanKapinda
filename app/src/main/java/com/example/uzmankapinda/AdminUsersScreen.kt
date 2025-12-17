package com.example.uzmankapinda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzmankapinda.ui.components.AppHeader
import com.example.uzmankapinda.ui.theme.UzmanKapindaTheme
import kotlinx.coroutines.launch

@Composable
fun AdminUsersScreen(
    onBack: () -> Unit
) {
    var users by remember { mutableStateOf<List<UserModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // State tanımlamaları
    var userToDelete by remember { mutableStateOf<UserModel?>(null) }
    var userToEdit by remember { mutableStateOf<UserModel?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        FirestoreManager.getAllUsers { list, error ->
            if (error != null) {
                errorMessage = error
                isLoading = false
            } else {
                users = list ?: emptyList()
                isLoading = false
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                AppHeader(
                    title = "Kullanıcılar",
                    subtitle = "Hesapları düzenle, dondur veya sil",
                    onBack = onBack
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    when {
                        isLoading -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        errorMessage != null -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "Hata: $errorMessage", color = Color.Red)
                            }
                        }

                        users.isEmpty() -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Hiç kullanıcı bulunamadı.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        else -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(users) { user ->
                                    AdminUserItemCard(
                                        user = user,
                                        onToggleFreeze = { targetUser ->
                                            val newFrozen = !targetUser.isFrozen

                                            FirestoreManager.updateUserFrozenStatus(
                                                userId = targetUser.id,
                                                isFrozen = newFrozen
                                            ) { success, error ->
                                                scope.launch {
                                                    if (!success) {
                                                        snackbarHostState.showSnackbar(error ?: "İşlem başarısız")
                                                    } else {
                                                        users = users.map {
                                                            if (it.id == targetUser.id) it.copy(isFrozen = newFrozen) else it
                                                        }

                                                        val msg = if (newFrozen) "${targetUser.name} donduruldu." else "${targetUser.name} tekrar aktif edildi."
                                                        snackbarHostState.showSnackbar(msg)
                                                    }
                                                }
                                            }
                                        },
                                        onEditClick = { targetUser -> userToEdit = targetUser },
                                        onDeleteClick = { targetUser -> userToDelete = targetUser }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (userToDelete != null) {
                val u = userToDelete!!
                AlertDialog(
                    onDismissRequest = { userToDelete = null },
                    title = { Text("Kullanıcıyı Sil") },
                    text = {
                        Text("BU işlem geri alınamaz. \"${u.name}\" tamamen silinecek.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                userToDelete = null
                                FirestoreManager.deleteUser(u.id) { success, error ->
                                    scope.launch {
                                        if (success) {
                                            users = users.filterNot { it.id == u.id }
                                            snackbarHostState.showSnackbar("Kullanıcı silindi.")
                                        } else {
                                            snackbarHostState.showSnackbar(error ?: "Silinemedi")
                                        }
                                    }
                                }
                            }
                        ) { Text("Evet, Sil", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { userToDelete = null }) { Text("Vazgeç") }
                    }
                )
            }

            if (userToEdit != null) {
                var newName by remember { mutableStateOf(userToEdit!!.name) }
                AlertDialog(
                    onDismissRequest = { userToEdit = null },
                    title = { Text("Kullanıcıyı Düzenle") },
                    text = {
                        Column {
                            Text("Kullanıcının yeni ismini giriniz:")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("Ad Soyad") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (newName.isNotBlank()) {
                                        val targetId = userToEdit!!.id
                                        FirestoreManager.adminUpdateUserName(targetId, newName.trim()) { success, error ->
                                            scope.launch {
                                                if (success) {
                                                    users = users.map {
                                                        if (it.id == targetId) it.copy(name = newName.trim()) else it
                                                    }
                                                    snackbarHostState.showSnackbar("Kullanıcı güncellendi")
                                                    userToEdit = null
                                                } else {
                                                    snackbarHostState.showSnackbar(error ?: "Güncelleme hatası")
                                                }
                                            }
                                        }
                                    }
                                })
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    val targetId = userToEdit!!.id
                                    FirestoreManager.adminUpdateUserName(targetId, newName.trim()) { success, error ->
                                        scope.launch {
                                            if (success) {
                                                users = users.map {
                                                    if (it.id == targetId) it.copy(name = newName.trim()) else it
                                                }
                                                snackbarHostState.showSnackbar("Kullanıcı güncellendi")
                                                userToEdit = null
                                            } else {
                                                snackbarHostState.showSnackbar(error ?: "Güncelleme hatası")
                                            }
                                        }
                                    }
                                }
                            }
                        ) { Text("Kaydet") }
                    },
                    dismissButton = {
                        TextButton(onClick = { userToEdit = null }) { Text("İptal") }
                    }
                )
            }
        }
    }
}

@Composable
fun AdminUserItemCard(
    user: UserModel,
    onToggleFreeze: (UserModel) -> Unit,
    onEditClick: (UserModel) -> Unit,
    onDeleteClick: (UserModel) -> Unit
) {
    val statusText = if (user.isFrozen) "Dondurulmuş" else "Aktif"
    val statusColor = if (user.isFrozen) Color.Red else Color(0xFF2E7D32)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name.ifBlank { "(İsimsiz)" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = user.email, fontSize = 13.sp, color = Color.Gray)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (user.role == "admin") MaterialTheme.colorScheme.primaryContainer else Color.LightGray.copy(alpha = 0.3f),
                ) {
                    Text(
                        text = user.role.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (user.role == "admin") MaterialTheme.colorScheme.onPrimaryContainer else Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Durum: $statusText", fontSize = 12.sp, color = statusColor)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = { onToggleFreeze(user) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (user.isFrozen) Color(0xFF2E7D32) else Color.DarkGray)
                ) {
                    Text(if (user.isFrozen) "Aktif Et" else "Dondur", fontSize = 11.sp)
                }

                Button(
                    onClick = { onEditClick(user) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Düzenle", fontSize = 11.sp)
                }

                Button(
                    onClick = { onDeleteClick(user) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sil", fontSize = 11.sp)
                }
            }
        }
    }
}