package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RoyalNavyBlue
import com.example.ui.theme.SlateLight
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTestMode by viewModel.isTestMode.collectAsStateWithLifecycle()
    val testPhoneNumber by viewModel.testPhoneNumber.collectAsStateWithLifecycle()

    var editTestNumberText by remember(testPhoneNumber) { mutableStateOf(testPhoneNumber) }
    var isEditingNumber by remember { mutableStateOf(false) }
    var showCsvDialog by remember { mutableStateOf(false) }
    var previewFaculty by remember { mutableStateOf<FacultyEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateLight)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Institutional Crest Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = RoyalNavyBlue
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Official St. Joseph's College Crest
                    Image(
                        painter = painterResource(R.drawable.ic_sjc_crest),
                        contentDescription = "St. Joseph's College Crest",
                        modifier = Modifier.size(92.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ST. JOSEPH'S COLLEGE",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "(AUTONOMOUS)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MetallicGold
                    )
                    Text(
                        text = "Tiruchirappalli - 620 002, Tamil Nadu, India",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "Motto: “Pro Bono Et Vero” (For the Good and the True)",
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MetallicGold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sjctni.edu/"))
                            context.startActivity(browserIntent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MetallicGold,
                            contentColor = DarkCharcoal
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Visit Official Website", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // WhatsApp Verification & Test Mode
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Science,
                                    contentDescription = null,
                                    tint = if (isTestMode) Color(0xFF1D4ED8) else Color(0xFF16A34A),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "WhatsApp Dispatch Mode",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkCharcoal
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isTestMode) "Safe Test Mode (Routing to Test Mobile)" else "Live Production Mode (Routing to Faculty)",
                                fontSize = 12.sp,
                                color = if (isTestMode) Color(0xFF1D4ED8) else Color(0xFF16A34A),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Switch(
                            checked = !isTestMode,
                            onCheckedChange = { isProd ->
                                viewModel.toggleTestMode(!isProd)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF16A34A),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF2563EB)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Test Phone Configuration Box
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Configured Test Mobile:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E40AF),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "+91 $testPhoneNumber",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E3A8A),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            if (isEditingNumber) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editTestNumberText,
                                    onValueChange = { editTestNumberText = it },
                                    label = { Text("Test Mobile Number") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            viewModel.setTestPhoneNumber(editTestNumberText)
                                            isEditingNumber = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyBlue),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Save Number")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            editTestNumberText = "8754254943"
                                            viewModel.setTestPhoneNumber("8754254943")
                                            isEditingNumber = false
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Reset to 8754254943")
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { isEditingNumber = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDBEAFE), contentColor = Color(0xFF1E40AF)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Change Test Number", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // How to Check Guide
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "HOW TO CHECK / TEST:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalNavyBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1. Safe Test Mode is currently enabled.\n" +
                                       "2. Tap the button below to trigger an immediate test dispatch.\n" +
                                       "3. The app renders the high-definition greeting card featuring the official SJC crest, attaches the collegiate message, and routes to WhatsApp for +91 $testPhoneNumber.\n" +
                                       "4. Review the generated card and message directly in WhatsApp!",
                                fontSize = 12.sp,
                                color = Color(0xFF334155),
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons for Preview and Test Dispatch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                previewFaculty = FacultyEntity(
                                    id = 999999L,
                                    staffId = "SJC-FAC-01",
                                    name = "Rev. Dr. S. Joseph SJ",
                                    departmentCode = "CS",
                                    designation = "Associate Professor of Computer Science",
                                    dob = "16-09-1978",
                                    mobile = testPhoneNumber,
                                    category = "Teaching"
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = RoyalNavyBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Preview Card",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = RoyalNavyBlue
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.testWhatsAppDispatch(context)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Test Dispatch",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // CSV Staff Data Import Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "STAFF CSV DATA IMPORTER",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalNavyBlue,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Upload your custom professor details spreadsheet or paste CSV rows",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.reloadOfficialStaffDirectory(forceCleanReplace = false) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("510 Staff", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalNavyBlue)
                            }

                            OutlinedButton(
                                onClick = { viewModel.exportDirectoryToCsv(context) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("💾 Backup CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                            }

                            Button(
                                onClick = { showCsvDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoyalNavyBlue,
                                    contentColor = MetallicGold
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("📥 Import CSV", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Leadership Hierarchy
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INSTITUTIONAL LEADERSHIP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalNavyBlue,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LeadershipRow("Rector", "Rev. Dr. Pavulraj Michael SJ", "President of the Governing Body")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))
                    LeadershipRow("Secretary", "Rev. Dr. M. Arockiasamy Xavier SJ", "Secretary, St. Joseph's College (Autonomous)")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))
                    LeadershipRow("Principal", "Rev. Dr. K. Arockiam SJ", "Academic Head & Executive Authority")
                }
            }
        }

        // System Architecture Specifications
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SYSTEM SPECIFICATIONS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalNavyBlue,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SpecItem("Local Room DB", "Offline SQLite Database with Live Staff Directory Flow")
                    SpecItem("HD Canvas Drawer", "1200 x 675 px Native Android Graphics Canvas with Official SJC Crest")
                    SpecItem("Birthday Radar", "Real-time auto-matching & 7-day advance forecast")
                    SpecItem("Dynamic Mapper", "51 Active Department Code Aliases")
                    SpecItem("CSV Import Engine", "Supports custom staff lists via file picker or direct text paste")
                    SpecItem("Theme & Palette", "Royal Navy Blue (#002B49), Metallic Gold (#D4AF37), Crimson (#800000)")
                }
            }
        }
    }

    if (showCsvDialog) {
        com.example.ui.components.CsvImportDialog(
            onDismiss = { showCsvDialog = false },
            onImportText = { text ->
                viewModel.importFacultyFromCsvText(text)
            },
            onImportUri = { uri ->
                viewModel.importFacultyFromCsvUri(context, uri)
            },
            onImportOfficialDataset = {
                viewModel.reloadOfficialStaffDirectory()
            }
        )
    }

    // High-Definition Birthday Gift Card Preview Dialog
    previewFaculty?.let { faculty ->
        CardPreviewDialog(
            faculty = faculty,
            resolvedDeptName = viewModel.resolveDeptName(faculty.departmentCode),
            onDismiss = { previewFaculty = null },
            onSendGreeting = {
                viewModel.generateAndSendGiftCard(context, faculty)
                previewFaculty = null
            }
        )
    }
}

@Composable
private fun LeadershipRow(role: String, name: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color(0xFFF1F5F9),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = role,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalNavyBlue
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DarkCharcoal
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkCharcoal
        )
        Text(
            text = value,
            fontSize = 11.sp,
            color = Color(0xFF64748B)
        )
    }
}
