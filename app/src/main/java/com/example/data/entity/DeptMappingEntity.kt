package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dept_mappings")
data class DeptMappingEntity(
    @PrimaryKey
    val code: String,
    val officialName: String
)
