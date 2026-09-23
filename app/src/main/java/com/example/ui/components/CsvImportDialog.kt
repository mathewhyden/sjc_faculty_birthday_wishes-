package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RoyalNavyBlue

@Composable
fun CsvImportDialog(
    onDismiss: () -> Unit,
    onImportText: (String) -> Unit,
    onImportUri: (Uri) -> Unit,
    onImportOfficialDataset: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var pastedCsvText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportUri(uri)
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("CSV", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = RoyalNavyBlue)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Import Professor Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = RoyalNavyBlue
                    )
                    Text(
                        text = "Batch upload staff from CSV or text",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = RoyalNavyBlue
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Choose File", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Paste Text", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Select .CSV or .TXT File from Device",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RoyalNavyBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap below to pick your professor spreadsheet from Downloads, Google Drive, or local storage.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    filePickerLauncher.launch(
                                        arrayOf("text/*", "application/*", "*/*")
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Browse & Select CSV File", fontWeight = FontWeight.Bold)
                            }

                            if (onImportOfficialDataset != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        onImportOfficialDataset()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF92400E)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("⚡ Instant Load Official 510 SJC Staff", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    Column {
                        Text(
                            text = "Paste CSV Rows Below:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalNavyBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = pastedCsvText,
                            onValueChange = { pastedCsvText = it },
                            placeholder = {
                                Text(
                                    "Staff ID, Name, Department, Designation, DOB, Mobile, Category\n" +
                                    "SJC101, Dr. A. John, CS, Associate Professor, 16-09-1980, 9876543210, Teaching\n" +
                                    "SJC102, Rev. Dr. Francis SJ, MAT, Professor, 24-10-1975, 9443215678, Teaching",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            maxLines = 10
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (pastedCsvText.isNotBlank()) {
                                    onImportText(pastedCsvText)
                                    onDismiss()
                                }
                            },
                            enabled = pastedCsvText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Import Pasted Rows", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Format Guide
                Surface(
                    color = Color(0xFFFEFCE8),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Accepted Column Format:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF854D0E)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Columns: ID, Name, Department, Designation, DOB, Mobile, Category\n" +
                                   "• Date of Birth: DD-MM-YYYY (e.g. 16-09-1980) or DD/MM/YYYY\n" +
                                   "• Departments: CS, MAT, PHY, CHE, ENG, TAM, COM, HIS, ECO, etc.",
                            fontSize = 10.sp,
                            color = Color(0xFF713F12),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        }
    )
}
