package com.example.uzmankapinda.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppBackButton(
    onBack: () -> Unit
) {
    IconButton(onClick = onBack) {
        Icon(
            imageVector = Icons.Rounded.ArrowBack,
            contentDescription = "Geri",
            modifier = Modifier.size(26.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}
