package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.entity.FacultyEntity
import com.example.ui.components.CardPreviewDialog
import com.example.ui.components.TodayBirthdayCard
import com.example.ui.components.UpcomingBirthdayItem
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RoyalNavyBlue
import com.example.ui.theme.SlateLight
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToDirectory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todayBirthdays by viewModel.todayBirthdays.collectAsStateWithLifecycle()
    val upcomingBirthdays by viewModel.upcomingBirthdays.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val isTestMode by viewModel.isTestMode.collectAsStateWithLifecycle()
    val testPhoneNumber by viewModel.testPhoneNumber.collectAsStateWithLifecycle()
    val generatingForId by viewModel.generatingForId.collectAsStateWithLifecycle()

    var showTestPhoneDialog by remember { mutableStateOf(false) }
    var selectedUpcomingFaculty by remember { mutableStateOf<FacultyEntity?>(null) }
    var previewFaculty by remember { mutableStateOf<FacultyEntity?>(null) }

    val todayDateStr = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        sdf.format(Calendar.getInstance().time)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateLight)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Prestigious SJC Institutional Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                RoyalNavyBlue,
                                Color(0xFF00192B)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Official SJC Crest Emblem
                        Image(
                            painter = painterResource(R.drawable.ic_sjc_crest),
                            contentDescription = "St. Joseph's College Crest",
                            modifier = Modifier.size(54.dp)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SJC BIRTHDAY WISHES",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                                fontFamily = FontFamily.Serif
                            )
                            Text(
                                text = "ST. JOSEPH'S COLLEGE (AUTONOMOUS)",
                                color = MetallicGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            )
                            Text(
                                text = "Tiruchirappalli - 620 002 • “Pro Bono Et Vero”",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Date Pill
                    Surface(
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MetallicGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = todayDateStr,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 2. Dual-Mode WhatsApp Dispatcher Selector Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("dual_mode_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isTestMode) Color(0xFF93C5FD) else Color(0xFF86EFAC)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isTestMode) Color(0xFFEFF6FF) else Color(0xFFF0FDF4)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isTestMode) Icons.Default.Security else Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = if (isTestMode) Color(0xFF1D4ED8) else Color(0xFF15803D),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isTestMode) "TEST MODE ACTIVE" else "PRODUCTION MODE",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTestMode) Color(0xFF1D4ED8) else Color(0xFF15803D)
                                )
                                if (isTestMode) {
                                    IconButton(
                                        onClick = { showTestPhoneDialog = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit test phone",
                                            tint = Color(0xFF1D4ED8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (isTestMode) {
                                    "Target: ${if (testPhoneNumber.startsWith("91")) "+$testPhoneNumber" else "+91 $testPhoneNumber"} (Office Safe Test)"
                                } else {
                                    "Direct delivery to professor's official mobile"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Switch(
                        checked = !isTestMode,
                        onCheckedChange = { isProd ->
                            viewModel.toggleTestMode(!isProd)
                            val modeMsg = if (isProd) "Switched to Production Mode (Direct to Faculty)" else "Switched to Safe Test Mode"
                            Toast.makeText(context, modeMsg, Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF16A34A),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF2563EB)
                        ),
                        modifier = Modifier.testTag("mode_switch")
                    )
                }
            }
        }

        // 3. MODULE 1: Hero Birthday Showcase
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cake,
                            contentDescription = null,
                            tint = CrimsonRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Today's Celebrants",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal,
                            fontSize = 17.sp
                        )
                    }

                    Surface(
                        color = if (todayBirthdays.isNotEmpty()) CrimsonRed else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${todayBirthdays.size} Today",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = if (todayBirthdays.isNotEmpty()) Color.White else Color(0xFF475569),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (todayBirthdays.isNotEmpty()) {
                    todayBirthdays.forEach { faculty ->
                        TodayBirthdayCard(
                            faculty = faculty,
                            resolvedDeptName = viewModel.resolveDeptName(faculty.departmentCode),
                            isGenerating = generatingForId == faculty.id,
                            isTestMode = isTestMode,
                            onSendGreetingClick = {
                                viewModel.generateAndSendGiftCard(context, faculty)
                            },
                            onPreviewClick = {
                                previewFaculty = faculty
                            },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                } else {
                    // Empty State for Today with option to sample card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(GoldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cake,
                                    contentDescription = null,
                                    tint = MetallicGold,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Staff Birthdays Registered for Today",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DarkCharcoal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Check the 7-Day Birthday Radar below for upcoming celebrations or preview sample card generation.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    previewFaculty = FacultyEntity(
                                        id = 999999L,
                                        staffId = "SJC-FAC-01",
                                        name = "Rev. Dr. S. Joseph SJ",
                                        departmentCode = "CS",
                                        designation = "Associate Professor of Computer Science",
                                        dob = "16-09-1978",
                                        mobile = "8754254943",
                                        category = "Teaching"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoyalNavyBlue,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Celebration,
                                    contentDescription = null,
                                    tint = MetallicGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Preview New Gift Card Design",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Dashboard 2x2 Stat Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "System Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Staff",
                        value = "${stats.totalStaff}",
                        subtitle = "Active Directory",
                        icon = Icons.Default.People,
                        iconColor = RoyalNavyBlue,
                        bgColor = Color(0xFFE0F2FE),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Today's B'day",
                        value = "${stats.todayBirthdaysCount}",
                        subtitle = "Needs Greeting",
                        icon = Icons.Default.Cake,
                        iconColor = CrimsonRed,
                        bgColor = Color(0xFFFEE2E2),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "7-Day Radar",
                        value = "${stats.upcomingBirthdaysCount}",
                        subtitle = "Upcoming Next 7 Days",
                        icon = Icons.Default.NotificationsActive,
                        iconColor = Color(0xFFB45309),
                        bgColor = Color(0xFFFEF3C7),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Active Depts",
                        value = "${stats.activeDepartmentsCount}",
                        subtitle = "Mapped Aliases",
                        icon = Icons.Default.Domain,
                        iconColor = Color(0xFF047857),
                        bgColor = Color(0xFFD1FAE5),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. 7-Day Birthday Radar Carousel (LazyRow)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = RoyalNavyBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "7-Day Birthday Radar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal,
                            fontSize = 17.sp
                        )
                    }

                    Text(
                        text = "Advance Review",
                        fontSize = 12.sp,
                        color = RoyalNavyBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (upcomingBirthdays.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(upcomingBirthdays, key = { it.faculty.id }) { item ->
                            UpcomingBirthdayItem(
                                item = item,
                                resolvedDept = viewModel.resolveDeptName(item.faculty.departmentCode),
                                onClick = {
                                    previewFaculty = item.faculty
                                }
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "No birthdays registered in the next 7 days.",
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFF64748B),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 6. Leadership & Institution Footer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INSTITUTIONAL LEADERSHIP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalNavyBlue,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Rector: Rev. Dr. Pavulraj Michael SJ",
                        fontSize = 12.sp,
                        color = DarkCharcoal
                    )
                    Text(
                        text = "• Secretary: Rev. Dr. M. Arockiasamy Xavier SJ",
                        fontSize = 12.sp,
                        color = DarkCharcoal
                    )
                    Text(
                        text = "• Principal: Rev. Dr. K. Arockiam SJ",
                        fontSize = 12.sp,
                        color = DarkCharcoal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Dialog to change Test Mobile Number
    if (showTestPhoneDialog) {
        var tempPhone by remember { mutableStateOf(testPhoneNumber) }
        AlertDialog(
            onDismissRequest = { showTestPhoneDialog = false },
            title = {
                Text(
                    text = "Configure Test Mobile Number",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "In Test Mode, outgoing WhatsApp birthday cards are safely routed to this mobile number for review before faculty transmission.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempPhone,
                        onValueChange = { tempPhone = it },
                        label = { Text("Test Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempPhone.isNotBlank()) {
                            viewModel.setTestPhoneNumber(tempPhone.trim())
                            showTestPhoneDialog = false
                            Toast.makeText(context, "Test number updated to $tempPhone", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTestPhoneDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal / Dialog for previewing & generating card for upcoming faculty
    selectedUpcomingFaculty?.let { faculty ->
        AlertDialog(
            onDismissRequest = { selectedUpcomingFaculty = null },
            title = {
                Text(
                    text = "Advance Gift Card Review",
                    fontWeight = FontWeight.Bold,
                    color = RoyalNavyBlue
                )
            },
            text = {
                Column {
                    Text(
                        text = "Generate and preview HD Birthday Gift Card for:",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = faculty.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoal
                    )
                    Text(
                        text = "${faculty.designation} • ${viewModel.resolveDeptName(faculty.departmentCode)}",
                        fontSize = 12.sp,
                        color = CrimsonRed
                    )
                    Text(
                        text = "DOB: ${faculty.dob} • Mobile: ${faculty.mobile}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "You can dispatch this card now via WhatsApp (${if (isTestMode) "Routed to Test Mobile: $testPhoneNumber" else "Direct to ${faculty.name}: +${faculty.mobile}"}).",
                        fontSize = 11.sp,
                        color = Color(0xFF334155)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.generateAndSendGiftCard(context, faculty)
                        selectedUpcomingFaculty = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyBlue)
                ) {
                    Text(if (isTestMode) "Generate & Send Card (Test)" else "Send Direct to +${faculty.mobile}")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUpcomingFaculty = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Interactive Card Preview Dialog (Visual HD Card Display)
    previewFaculty?.let { faculty ->
        CardPreviewDialog(
            faculty = faculty,
            resolvedDeptName = viewModel.resolveDeptName(faculty.departmentCode),
            onDismiss = { previewFaculty = null },
            onSendGreeting = {
                viewModel.generateAndSendGiftCard(context, faculty)
                previewFaculty = null
            },
            isSending = generatingForId == faculty.id
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = DarkCharcoal,
                fontSize = 24.sp
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }
    }
}
