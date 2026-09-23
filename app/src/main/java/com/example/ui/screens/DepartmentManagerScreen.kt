package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.DeptMappingEntity
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RoyalNavyBlue
import com.example.ui.theme.SlateLight
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DepartmentManagerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val deptList by viewModel.allDeptMappings.collectAsStateWithLifecycle()
    val allFaculty by viewModel.allFaculty.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf<DeptMappingEntity?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var deptToDelete by remember { mutableStateOf<DeptMappingEntity?>(null) }

    val filteredDepts = remember(deptList, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) deptList else deptList.filter {
            it.code.lowercase().contains(q) || it.officialName.lowercase().contains(q)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("department_manager_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    isCreatingNew = true
                    showAddEditDialog = DeptMappingEntity("", "")
                },
                containerColor = RoyalNavyBlue,
                contentColor = MetallicGold,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .testTag("add_dept_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Dept Mapping")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateLight)
                .padding(innerPadding)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Dynamic Department Alias Mapper",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RoyalNavyBlue,
                    fontSize = 18.sp
                )
                Text(
                    text = "Resolves 2-letter Excel codes ('CS', 'AI', 'PH') into official institutional titles.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_dept_input"),
                    placeholder = { Text("Filter department by code or title...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = RoyalNavyBlue
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
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
            }

            // Results count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredDepts.size} Active Department Mappings",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569)
                )
            }

            // Dept Mappings List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredDepts, key = { it.code }) { dept ->
                    val staffInDept = remember(allFaculty, dept.code) {
                        allFaculty.count { it.departmentCode.equals(dept.code, ignoreCase = true) }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dept_item_${dept.code}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = RoyalNavyBlue,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = dept.code,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        color = MetallicGold,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dept.officialName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkCharcoal,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "$staffInDept faculty & staff registered",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        isCreatingNew = false
                                        showAddEditDialog = dept
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = RoyalNavyBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { deptToDelete = dept },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    showAddEditDialog?.let { currentDept ->
        var code by remember { mutableStateOf(currentDept.code) }
        var name by remember { mutableStateOf(currentDept.officialName) }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddEditDialog = null },
            title = {
                Text(
                    text = if (isCreatingNew) "Add Department Code" else "Edit Department Title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RoyalNavyBlue
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase(); error = null },
                        label = { Text("Code (e.g. CS, AI, ME)") },
                        enabled = isCreatingNew,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_dept_code")
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; error = null },
                        label = { Text("Official Department Title") },
                        placeholder = { Text("e.g. Department of Computer Science") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_dept_title")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.isBlank()) {
                            error = "Department code cannot be empty"
                            return@Button
                        }
                        if (name.isBlank()) {
                            error = "Department official title cannot be empty"
                            return@Button
                        }
                        viewModel.saveDeptMapping(code, name)
                        showAddEditDialog = null
                        Toast.makeText(context, "Department alias saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyBlue),
                    modifier = Modifier.testTag("save_dept_btn")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddEditDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation
    deptToDelete?.let { dept ->
        AlertDialog(
            onDismissRequest = { deptToDelete = null },
            title = { Text("Delete Department Alias", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to remove the alias for '${dept.code} - ${dept.officialName}'?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDeptMapping(dept.code)
                        deptToDelete = null
                        Toast.makeText(context, "Department alias removed", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deptToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
