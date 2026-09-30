package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RoyalNavyBlue
import com.example.ui.viewmodel.UpcomingBirthdayData

@Composable
fun UpcomingBirthdayItem(
    item: UpcomingBirthdayData,
    resolvedDept: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .clickable(onClick = onClick)
            .testTag("upcoming_birthday_item_${item.faculty.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Relative Tag + Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (item.daysRemaining == 1) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item.relativeLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = if (item.daysRemaining == 1) Color(0xFF92400E) else Color(0xFF334155),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = CrimsonRed,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.formattedDate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CrimsonRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Avatar & Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(RoyalNavyBlue)
                        .border(1.5.dp, MetallicGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.faculty.name.take(2).uppercase(),
                        color = MetallicGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.faculty.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val cleanDept = com.example.utils.CanvasCardDrawer.getDepartmentCleanName(
                        if (resolvedDept.isNotBlank() && !resolvedDept.equals("Department of ${item.faculty.departmentCode}", ignoreCase = true)) {
                            resolvedDept
                        } else {
                            item.faculty.departmentCode
                        }
                    )
                    val fullDeptDisplay = if (cleanDept.isNotBlank() && cleanDept.length > 2) cleanDept else resolvedDept
                    Text(
                        text = "$fullDeptDisplay • ${item.faculty.designation.ifBlank { "Faculty" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Advance review badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap to Preview Card",
                    fontSize = 11.sp,
                    color = RoyalNavyBlue,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Preview",
                    tint = RoyalNavyBlue,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
