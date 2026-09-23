package com.example.model

enum class StaffCategory(val displayName: String) {
    TEACHING("Teaching Faculty"),
    NON_TEACHING("Non-Teaching Staff"),
    JESUIT_LEADERSHIP("Jesuit Leadership");

    companion object {
        fun fromString(value: String): StaffCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: TEACHING
        }
    }
}
