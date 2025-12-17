package com.example.uzmankapinda.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    showBackArrow: Boolean = true, // Varsayılan olarak geri oku göster
    trailingIcon: @Composable (() -> Unit)? = null // Varsayılan olarak sağda ikon yok
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
            .padding(bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp)
        ) {
            // Üst Satır (Geri Butonu + Sağ İkon)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Geri Butonu (Varsa)
                if (showBackArrow) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                } else {
                    // Geri butonu yoksa boşluk bırak ki başlık kaymasın (isteğe bağlı)
                    Spacer(modifier = Modifier.size(1.dp))
                }

                // Sağ Taraftaki İkon (Varsa)
                if (trailingIcon != null) {
                    trailingIcon()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Başlık ve Alt Başlık
            Text(
                text = title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}