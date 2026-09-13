package com.lockguard.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lockguard.app.domain.model.SecurityLevel
import com.lockguard.app.ui.theme.StatusAmber
import com.lockguard.app.ui.theme.StatusGreen
import com.lockguard.app.ui.theme.StatusRed

@Composable
fun StatusBadge(
    level: SecurityLevel,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor, dotColor) = when (level) {
        SecurityLevel.MAXIMUM, SecurityLevel.PROTECTED -> Quadruple(
            Color(0x2210B981),
            StatusGreen.copy(alpha = 0.5f),
            StatusGreen,
            StatusGreen
        )
        SecurityLevel.PARTIAL -> Quadruple(
            Color(0x22F59E0B),
            StatusAmber.copy(alpha = 0.5f),
            StatusAmber,
            StatusAmber
        )
        SecurityLevel.ACTION_REQUIRED, SecurityLevel.DISABLED -> Quadruple(
            Color(0x22EF4444),
            StatusRed.copy(alpha = 0.5f),
            StatusRed,
            StatusRed
        )
    }

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(50))
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = level.label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
