package com.kelompok4.resepin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompok4.resepin.ui.theme.Border
import com.kelompok4.resepin.ui.theme.Primary
import com.kelompok4.resepin.ui.theme.PrimaryContainer
import com.kelompok4.resepin.ui.theme.PrimaryDark
import com.kelompok4.resepin.ui.theme.Surface
import com.kelompok4.resepin.ui.theme.TextMuted
import com.kelompok4.resepin.ui.theme.TextPrimary

/** Lima tab utama sesuai komponen "BottomNav". */
enum class MainTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector = icon) {
    Beranda("Beranda", Icons.Outlined.Home),
    Cari("Cari", Icons.Outlined.Search),
    Komunitas("Komunitas", Icons.Outlined.Group),
    Playlist("Playlist", Icons.AutoMirrored.Outlined.QueueMusic),
    Favorit("Favorit", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
}

@Composable
fun BottomNavBar(selected: MainTab, onSelect: (MainTab) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Surface)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = Border)
        Row(
            Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainTab.entries.forEach { tab ->
                val isSelected = tab == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) PrimaryContainer else Surface)
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                            contentDescription = tab.label,
                            tint = when {
                                isSelected && tab == MainTab.Favorit -> Primary
                                isSelected -> PrimaryDark
                                else -> TextMuted
                            },
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) TextPrimary else TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
