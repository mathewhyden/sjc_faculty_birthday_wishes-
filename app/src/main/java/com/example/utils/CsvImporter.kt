package com.example.utils

import android.content.Context
import android.net.Uri
import com.example.data.entity.FacultyEntity
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvImporter {

    /**
     * Parses CSV or TSV text into a list of FacultyEntity records.
     * Supports both header-based mapping and index-based fallback.
     */
    fun parseCsv(csvText: String): List<FacultyEntity> {
        val lines = csvText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (lines.isEmpty()) return emptyList()

        val delimiter = detectDelimiter(lines.first())
        val firstRowTokens = parseRow(lines.first(), delimiter)

        val isHeader = firstRowTokens.any { token ->
            val clean = token.lowercase()
            clean.contains("name") || clean.contains("dept") || clean.contains("id") || clean.contains("dob")
        }

        val (headerMap, dataRows) = if (isHeader) {
            val map = buildHeaderMap(firstRowTokens)
            map to lines.drop(1)
        } else {
            emptyMap<String, Int>() to lines
        }

        val result = mutableListOf<FacultyEntity>()

        for ((index, rowStr) in dataRows.withIndex()) {
            val tokens = parseRow(rowStr, delimiter)
            if (tokens.isEmpty()) continue

            val staffId: String
            val name: String
            val dept: String
            val desig: String
            val dob: String
            val mobile: String
            val category: String

            if (headerMap.isNotEmpty()) {
                name = headerMap["name"]?.let { tokens.getOrNull(it) }?.trim() ?: ""
                staffId = headerMap["id"]?.let { tokens.getOrNull(it) }?.trim()
                    ?: "SJC${1000 + index}"
                dept = headerMap["dept"]?.let { tokens.getOrNull(it) }?.trim() ?: "GEN"
                desig = headerMap["desig"]?.let { tokens.getOrNull(it) }?.trim()
                    ?: "Faculty Member"
                dob = normalizeDob(headerMap["dob"]?.let { tokens.getOrNull(it) }?.trim() ?: "01-01-1985")
                mobile = headerMap["mobile"]?.let { tokens.getOrNull(it) }?.trim() ?: ""
                category = headerMap["category"]?.let { tokens.getOrNull(it) }?.trim()
                    ?: if (desig.contains("Assistant", ignoreCase = true) || desig.contains("Associate", ignoreCase = true) || desig.contains("Professor", ignoreCase = true)) "Teaching" else "Non-Teaching"
            } else {
                // Index-based fallback:
                // 0: ID, 1: Name, 2: Dept, 3: Designation, 4: DOB, 5: Mobile, 6: Category
                staffId = tokens.getOrNull(0)?.trim() ?: "SJC${1000 + index}"
                name = tokens.getOrNull(1)?.trim() ?: ""
                dept = tokens.getOrNull(2)?.trim() ?: "GEN"
                desig = tokens.getOrNull(3)?.trim() ?: "Faculty Member"
                dob = normalizeDob(tokens.getOrNull(4)?.trim() ?: "01-01-1985")
                mobile = tokens.getOrNull(5)?.trim() ?: ""
                category = tokens.getOrNull(6)?.trim()
                    ?: if (desig.contains("Professor", ignoreCase = true) || desig.contains("Assistant", ignoreCase = true)) "Teaching" else "Non-Teaching"
            }

            if (name.isNotBlank()) {
                result.add(
                    FacultyEntity(
                        staffId = staffId.ifBlank { "SJC${1000 + index}" },
                        name = name,
                        departmentCode = dept.uppercase().ifBlank { "GEN" },
                        designation = desig,
                        dob = dob,
                        mobile = mobile,
                        category = category.ifBlank { "Teaching" }
                    )
                )
            }
        }

        // Deduplicate repetitive records
        return result.distinctBy {
            it.staffId.trim().lowercase() to it.name.trim().lowercase()
        }
    }

    /**
     * Reads text from an asset file using the provided Context.
     */
    fun readTextFromAssets(context: Context, fileName: String): String {
        return try {
            context.assets.open(fileName).use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Reads text from a content Uri using the provided Context.
     */
    fun readTextFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                reader.readText()
            }
        } ?: ""
    }

    private fun detectDelimiter(firstLine: String): Char {
        val commas = firstLine.count { it == ',' }
        val tabs = firstLine.count { it == '\t' }
        val semicolons = firstLine.count { it == ';' }
        return when {
            tabs > commas && tabs > semicolons -> '\t'
            semicolons > commas -> ';'
            else -> ','
        }
    }

    private fun parseRow(line: String, delimiter: Char): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var insideQuotes = false

        for (ch in line) {
            when {
                ch == '"' -> insideQuotes = !insideQuotes
                ch == delimiter && !insideQuotes -> {
                    tokens.add(sb.toString().trim().removeSurrounding("\""))
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        tokens.add(sb.toString().trim().removeSurrounding("\""))
        return tokens
    }

    private fun buildHeaderMap(headers: List<String>): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        for ((idx, rawHeader) in headers.withIndex()) {
            val h = rawHeader.lowercase().replace("[^a-z0-9]".toRegex(), "")
            when {
                h.contains("name") -> map["name"] = idx
                h.contains("staffid") || h.contains("empid") || h.contains("code") || h == "id" -> map["id"] = idx
                h.contains("dept") || h.contains("department") -> map["dept"] = idx
                h.contains("desig") || h.contains("designation") || h.contains("post") || h.contains("role") -> map["desig"] = idx
                h.contains("dob") || h.contains("birth") || h.contains("dateofbirth") -> map["dob"] = idx
                h.contains("mob") || h.contains("phone") || h.contains("contact") || h.contains("whatsapp") -> map["mobile"] = idx
                h.contains("cat") || h.contains("category") || h.contains("type") -> map["category"] = idx
            }
        }
        return map
    }

    /**
     * Converts varied date representations (DD/MM/YYYY, YYYY-MM-DD, D-M-YYYY) to standard DD-MM-YYYY.
     */
    fun normalizeDob(raw: String): String {
        val clean = raw.trim()
        if (clean.isBlank()) return "01-01-1985"

        // If format is YYYY-MM-DD
        if (clean.matches("^\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}$".toRegex())) {
            val parts = clean.split("[-/]".toRegex())
            val y = parts[0]
            val m = parts[1].padStart(2, '0')
            val d = parts[2].padStart(2, '0')
            return "$d-$m-$y"
        }

        // If format is DD/MM/YYYY or DD-MM-YYYY
        val parts = clean.split("[-/.]".toRegex())
        if (parts.size >= 2) {
            val d = parts[0].padStart(2, '0')
            val m = parts[1].padStart(2, '0')
            val y = if (parts.size >= 3) parts[2] else "1985"
            return "$d-$m-$y"
        }

        return clean
    }

    /**
     * Serializes a list of faculty entities to a standard CSV string.
     */
    fun exportToCsv(facultyList: List<FacultyEntity>): String {
        val sb = StringBuilder()
        sb.append("Staff ID,Name,Department,Designation,DOB,Mobile,Category\n")
        for (f in facultyList) {
            val name = if (f.name.contains(",") || f.name.contains("\"")) "\"${f.name.replace("\"", "\"\"")}\"" else f.name
            val desig = if (f.designation.contains(",") || f.designation.contains("\"")) "\"${f.designation.replace("\"", "\"\"")}\"" else f.designation
            sb.append("${f.staffId},$name,${f.departmentCode},$desig,${f.dob},${f.mobile},${f.category}\n")
        }
        return sb.toString()
    }
}
