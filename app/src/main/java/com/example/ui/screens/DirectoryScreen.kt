package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.FacultyEntity
import com.example.model.StaffCategory
import com.example.ui.components.AddFacultyDialog
import com.example.ui.components.CardPreviewDialog
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RoyalNavyBlue
import com.example.ui.theme.SlateLight
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DirectoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val facultyList by viewModel.filteredFaculty.collectAsStateWithLifecycle()
    val totalCount by viewModel.allFaculty.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showCsvDialog by remember { mutableStateOf(false) }
    var editingFaculty by remember { mutableStateOf<FacultyEntity?>(null) }
    var facultyToDelete by remember { mutableStateOf<FacultyEntity?>(null) }
    var previewFaculty by remember { mutableStateOf<FacultyEntity?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("directory_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = RoyalNavyBlue,
                contentColor = MetallicGold,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .testTag("add_faculty_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Faculty")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateLight)
                .padding(innerPadding)
        ) {
            // Search Bar Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Faculty & Staff Directory",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoyalNavyBlue,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${totalCount.size} Registered SJC Staff Members",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { viewModel.reloadOfficialStaffDirectory(forceCleanReplace = false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFEF3C7),
                                contentColor = Color(0xFF92400E)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("510 Staff", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.exportDirectoryToCsv(context) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("Export", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalNavyBlue)
                        }

                        Button(
                            onClick = { showCsvDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEFF6FF),
                                contentColor = RoyalNavyBlue
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("Import", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("directory_search_input"),
                    placeholder = { Text("Search by name, ID, department, mobile...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = RoyalNavyBlue
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalNavyBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.onCategorySelected(null) },
                            label = { Text("All (${totalCount.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalNavyBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_all")
                        )
                    }

                    items(StaffCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = {
                                viewModel.onCategorySelected(if (selectedCategory == cat.name) null else cat.name)
                            },
                            label = { Text(cat.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalNavyBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_${cat.name}")
                        )
                    }
                }
            }

            // Results count banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${facultyList.size} of ${totalCount.size} staff members",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }

            // Staff List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (facultyList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No faculty or staff found matching “$searchQuery”",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(facultyList, key = { it.id }) { faculty ->
                        FacultyListItem(
                            faculty = faculty,
                            resolvedDept = viewModel.resolveDeptName(faculty.departmentCode),
                            onSendGiftCard = {
                                previewFaculty = faculty
                            },
                            onCallClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${faculty.mobile}")
                                }
                                context.startActivity(dialIntent)
                            },
                            onEditClick = { editingFaculty = faculty },
                            onDeleteClick = { facultyToDelete = faculty }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog || editingFaculty != null) {
        AddFacultyDialog(
            initialFaculty = editingFaculty,
            onDismiss = {
                showAddDialog = false
                editingFaculty = null
            },
            onSave = { savedFaculty ->
                viewModel.saveFaculty(savedFaculty, isEdit = editingFaculty != null)
                showAddDialog = false
                editingFaculty = null
                Toast.makeText(context, "Faculty record saved successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    facultyToDelete?.let { faculty ->
        AlertDialog(
            onDismissRequest = { facultyToDelete = null },
            title = { Text("Delete Staff Record", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to remove ${faculty.name} (${faculty.staffId}) from the directory?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFaculty(faculty)
                        facultyToDelete = null
                        Toast.makeText(context, "Staff record deleted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { facultyToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // CSV Import Dialog
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
private fun FacultyListItem(
    faculty: FacultyEntity,
    resolvedDept: String,
    onSendGiftCard: () -> Unit,
    onCallClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("faculty_item_${faculty.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(RoyalNavyBlue)
                        .border(1.5.dp, MetallicGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = faculty.name.take(2).uppercase(),
                        color = MetallicGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = faculty.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit Record") },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onEditClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = Color.Red) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                                    onClick = {
                                        showMenu = false
                                        onDeleteClick()
                                    }
                                )
                            }
                        }
                    }

                    if (faculty.designation.isNotBlank()) {
                        Text(
                            text = faculty.designation,
                            style = MaterialTheme.typography.bodySmall,
                            color = CrimsonRed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = resolvedDept,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Badges Row: ID, Dept Code, DOB
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = faculty.staffId,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkCharcoal
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        val cleanDeptBadge = com.example.utils.CanvasCardDrawer.getDepartmentCleanName(
                            if (resolvedDept.isNotBlank() && !resolvedDept.equals("Department of ${faculty.departmentCode}", ignoreCase = true)) {
                                resolvedDept
                            } else {
                                faculty.departmentCode
                            }
                        )
                        val displayDeptBadge = if (cleanDeptBadge.isNotBlank() && cleanDeptBadge.length > 2) cleanDeptBadge else resolvedDept
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = displayDeptBadge,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cake,
                                contentDescription = "DOB",
                                tint = CrimsonRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = faculty.dob,
                                fontSize = 11.sp,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Call & Send Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = onCallClick,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = faculty.mobile,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }
                }

                Surface(
                    onClick = onSendGiftCard,
                    modifier = Modifier.weight(1f),
                    color = RoyalNavyBlue,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            tint = MetallicGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gift Card",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
