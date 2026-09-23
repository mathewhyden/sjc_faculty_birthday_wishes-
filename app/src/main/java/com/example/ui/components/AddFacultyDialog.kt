package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FacultyEntity
import com.example.model.StaffCategory
import com.example.ui.theme.RoyalNavyBlue

@Composable
fun AddFacultyDialog(
    initialFaculty: FacultyEntity? = null,
    onDismiss: () -> Unit,
    onSave: (FacultyEntity) -> Unit
) {
    val isEdit = initialFaculty != null

    var name by remember { mutableStateOf(initialFaculty?.name ?: "") }
    var staffId by remember { mutableStateOf(initialFaculty?.staffId ?: "") }
    var departmentCode by remember { mutableStateOf(initialFaculty?.departmentCode ?: "") }
    var dob by remember { mutableStateOf(initialFaculty?.dob ?: "") }
    var mobile by remember { mutableStateOf(initialFaculty?.mobile ?: "") }
    var designation by remember { mutableStateOf(initialFaculty?.designation ?: "") }
    var selectedCategory by remember {
        mutableStateOf(
            initialFaculty?.category ?: StaffCategory.TEACHING.name
        )
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Edit Faculty Record" else "Add New Faculty / Staff",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = RoyalNavyBlue
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Full Name (with title/qualification)") },
                    placeholder = { Text("e.g. Dr. R. ARUN PRASATH, Ph.D.") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_faculty_name")
                )

                // Staff ID
                OutlinedTextField(
                    value = staffId,
                    onValueChange = { staffId = it; errorMessage = null },
                    label = { Text("Staff ID") },
                    placeholder = { Text("e.g. 26CAI51, 16NTM02") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_faculty_id")
                )

                // Department Code
                OutlinedTextField(
                    value = departmentCode,
                    onValueChange = { departmentCode = it.uppercase(); errorMessage = null },
                    label = { Text("Dept Code (2-3 letters)") },
                    placeholder = { Text("e.g. CS, AI, BO, PH, AC") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_faculty_dept")
                )

                // Designation
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Designation / Title") },
                    placeholder = { Text("e.g. Associate Professor & Head") },
                    leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_faculty_designation")
                )

                // Date of Birth
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it; errorMessage = null },
                    label = { Text("Date of Birth (DD-MM-YYYY)") },
                    placeholder = { Text("DD-MM-YYYY (e.g. 20-07-1984)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_faculty_dob")
                )

                // Mobile Number
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it; errorMessage = null },
                    label = { Text("WhatsApp / Mobile Number") },
                    placeholder = { Text("10-digit mobile number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_faculty_mobile")
                )

                // Category Selection Chips
                Text(
                    text = "Staff Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StaffCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = cat.name },
                            label = { Text(cat.displayName, fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_category_${cat.name}")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Faculty name is required."
                        return@Button
                    }
                    if (staffId.isBlank()) {
                        errorMessage = "Staff ID is required."
                        return@Button
                    }
                    if (departmentCode.isBlank()) {
                        errorMessage = "Department Code is required."
                        return@Button
                    }
                    if (dob.isBlank() || !dob.contains("-")) {
                        errorMessage = "Valid DOB is required (DD-MM-YYYY)."
                        return@Button
                    }
                    if (mobile.isBlank()) {
                        errorMessage = "Mobile number is required for WhatsApp."
                        return@Button
                    }

                    val updated = FacultyEntity(
                        id = initialFaculty?.id ?: 0,
                        staffId = staffId.trim(),
                        name = name.trim(),
                        departmentCode = departmentCode.trim().uppercase(),
                        dob = dob.trim(),
                        mobile = mobile.trim(),
                        category = selectedCategory,
                        designation = designation.trim()
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyBlue),
                modifier = Modifier.testTag("dialog_save_button")
            ) {
                Text(if (isEdit) "Save Changes" else "Add Faculty")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_cancel_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
