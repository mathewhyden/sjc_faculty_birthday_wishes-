package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FacultyEntity
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RoyalNavyBlue

@Composable
fun TodayBirthdayCard(
    faculty: FacultyEntity,
    resolvedDeptName: String,
    isGenerating: Boolean,
    isTestMode: Boolean,
    onSendGreetingClick: () -> Unit,
    onPreviewClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val goldGradient = Brush.linearGradient(
        colors = listOf(
            MetallicGold,
            Color(0xFFFFF1BD),
            MetallicGold,
            Color(0xFF997A1E)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.5.dp, goldGradient, RoundedCornerShape(20.dp))
            .testTag("today_birthday_card_${faculty.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Official SJC Crest, Birthday Badge & Mode Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(com.example.R.drawable.ic_sjc_crest),
                        contentDescription = "St. Joseph's College Crest",
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        color = CrimsonRed,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = "Celebration",
                                tint = MetallicGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TODAY'S BIRTHDAY",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }

                Surface(
                    color = if (isTestMode) Color(0xFFEFF6FF) else Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isTestMode) Color(0xFF93C5FD) else Color(0xFF86EFAC)
                    )
                ) {
                    Text(
                        text = if (isTestMode) "TEST MODE" else "PROD MODE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (isTestMode) Color(0xFF1D4ED8) else Color(0xFF15803D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Professor Profile Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Monogram Avatar
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(RoyalNavyBlue, Color(0xFF0F172A))
                            )
                        )
                        .border(2.5.dp, MetallicGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = extractInitials(faculty.name),
                        color = MetallicGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = faculty.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )

                    if (faculty.designation.isNotBlank()) {
                        Text(
                            text = faculty.designation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (androidx.compose.foundation.isSystemInDarkTheme()) MetallicGold else CrimsonRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }

                    Text(
                        text = resolvedDeptName,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFFCBD5E1) else Color(0xFF475569),
                        fontSize = 12.sp,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ID: ${faculty.staffId}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = if (androidx.compose.foundation.isSystemInDarkTheme()) MetallicGold else Color(0xFF64748B),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = faculty.mobile,
                            fontSize = 11.sp,
                            color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFFE2E8F0) else Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons: Preview HD Card & Send WhatsApp
            if (onPreviewClick != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPreviewClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            tint = RoyalNavyBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Preview Card",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalNavyBlue
                        )
                    }

                    Button(
                        onClick = onSendGreetingClick,
                        enabled = !isGenerating,
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("send_whatsapp_button_${faculty.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalNavyBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MetallicGold,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MetallicGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Send WhatsApp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = onSendGreetingClick,
                    enabled = !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("send_whatsapp_button_${faculty.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalNavyBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MetallicGold,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Rendering HD Gift Card...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MetallicGold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MetallicGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate Gift Card & Send WhatsApp",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun extractInitials(name: String): String {
    val clean = name
        .replace("Dr.", "", ignoreCase = true)
        .replace("Rev.", "", ignoreCase = true)
        .replace("Fr.", "", ignoreCase = true)
        .replace("Mr.", "", ignoreCase = true)
        .replace("Mrs.", "", ignoreCase = true)
        .replace("Ms.", "", ignoreCase = true)
        .replace("Ph.D.", "", ignoreCase = true)
        .replace("SJ", "", ignoreCase = true)
        .replace(".", " ")
        .trim()

    val parts = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "SJ"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts[0].first()}${parts[1].first()}".uppercase()
    }
}
