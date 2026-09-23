package com.example.data.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "faculty")
data class FacultyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val staffId: String,
    val name: String,
    val departmentCode: String,
    val dob: String, // Format: DD-MM-YYYY
    val mobile: String,
    val category: String, // TEACHING, NON_TEACHING, JESUIT_LEADERSHIP
    val designation: String = "",
    val photoUrl: String = ""
) {
    @get:Ignore
    val deptCode: String
        get() = departmentCode
}

typealias FacultyMember = FacultyEntity
