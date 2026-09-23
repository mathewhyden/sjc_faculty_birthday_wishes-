package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FacultyEntity
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RoyalNavyBlue
import com.example.utils.CanvasCardDrawer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Interactive Preview Dialog that showcases the generated High-Definition (1200 x 675)
 * St. Joseph's College Birthday Gift Card with instant dispatch capabilities.
 */
@Composable
fun CardPreviewDialog(
    faculty: FacultyEntity,
    resolvedDeptName: String,
    onDismiss: () -> Unit,
    onSendGreeting: () -> Unit,
    isSending: Boolean = false
) {
    val context = LocalContext.current
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun renderBitmap() {
        scope.launch {
            isLoading = true
            val bmp = withContext(Dispatchers.Default) {
                CanvasCardDrawer.generateGreetingCardBitmap(context, faculty, resolvedDeptName)
            }
            previewBitmap = bmp
            isLoading = false
        }
    }

    LaunchedEffect(faculty.id) {
        renderBitmap()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_preview_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RoyalNavyBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            tint = MetallicGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Official SJC Gift Card",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoyalNavyBlue,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "1200 x 675 High-Definition Preview",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Recipient: ${faculty.name} (${faculty.staffId})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkCharcoal,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = "$resolvedDeptName • DOB: ${faculty.dob}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Card Preview Frame
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1200f / 675f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, MetallicGold, RoundedCornerShape(12.dp)),
                    color = RoyalNavyBlue,
                    shadowElevation = 4.dp
                ) {
                    if (isLoading || previewBitmap == null) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = MetallicGold,
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Rendering Gift Card...",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        previewBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Generated SJC Birthday Gift Card",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Includes SJC Crest & College Leadership Signatures",
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B)
                    )
                    IconButton(
                        onClick = { renderBitmap() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Preview",
                            tint = RoyalNavyBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSendGreeting,
                enabled = !isSending && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalNavyBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("send_preview_card_button")
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MetallicGold,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sending...", fontSize = 13.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MetallicGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send via WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
