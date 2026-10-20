package com.visura.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.ui.theme.BorderGray
import com.visura.ui.theme.LightBlueBg
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.TextGray

internal fun Modifier.cardBorder() = this.border(1.dp, BorderGray, RoundedCornerShape(16.dp))

@Composable
internal fun StatCard(numero: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LightBlueBg)
            .padding(vertical = 14.dp, horizontal = 12.dp)
    ) {
        Text(numero, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 12.sp, color = TextGray)
    }
}