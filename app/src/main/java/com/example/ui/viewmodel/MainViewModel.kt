package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.DeptMappingEntity
import com.example.data.entity.FacultyEntity
import com.example.data.repository.FacultyRepository
import com.example.utils.CanvasCardDrawer
import com.example.utils.WhatsAppSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class UpcomingBirthdayData(
    val faculty: FacultyEntity,
    val daysRemaining: Int,
    val relativeLabel: String,
    val formattedDate: String
)

data class DashboardStats(
    val totalStaff: Int = 0,
    val todayBirthdaysCount: Int = 0,
    val upcomingBirthdaysCount: Int = 0,
    val activeDepartmentsCount: Int = 0
)

class MainViewModel(
    application: Application,
    private val repository: FacultyRepository
) : AndroidViewModel(application) {

    // --- Search & Filters ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // --- Dual Mode WhatsApp Settings ---
    private val _isTestMode = MutableStateFlow(true)
    val isTestMode: StateFlow<Boolean> = _isTestMode.asStateFlow()

    private val _testPhoneNumber = MutableStateFlow("8754254943")
    val testPhoneNumber: StateFlow<String> = _testPhoneNumber.asStateFlow()

    // --- Card Generation Progress State ---
    private val _generatingForId = MutableStateFlow<Long?>(null)
    val generatingForId: StateFlow<Long?> = _generatingForId.asStateFlow()

    // --- Toast / Message Events ---
    private val _eventMessages = MutableSharedFlow<String>()
    val eventMessages: SharedFlow<String> = _eventMessages.asSharedFlow()

    // --- Data Streams from Room ---
    val allFaculty: StateFlow<List<FacultyEntity>> = repository.allFaculty
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeptMappings: StateFlow<List<DeptMappingEntity>> = repository.allDeptMappings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deptMap: StateFlow<Map<String, String>> = repository.allDeptMappings
        .combine(MutableStateFlow(Unit)) { depts, _ ->
            depts.associate { it.code.uppercase() to it.officialName }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Filtered Faculty Directory List
    val filteredFaculty: StateFlow<List<FacultyEntity>> = combine(
        allFaculty,
        _searchQuery,
        _selectedCategory,
        deptMap
    ) { facultyList, query, category, depts ->
        val trimmedQuery = query.trim().lowercase()
        facultyList.filter { faculty ->
            val matchesCategory = category == null || faculty.category.equals(category, ignoreCase = true)
            val deptOfficial = depts[faculty.departmentCode.uppercase()] ?: ""
            val matchesQuery = trimmedQuery.isEmpty() ||
                    faculty.name.lowercase().contains(trimmedQuery) ||
                    faculty.staffId.lowercase().contains(trimmedQuery) ||
                    faculty.departmentCode.lowercase().contains(trimmedQuery) ||
                    deptOfficial.lowercase().contains(trimmedQuery) ||
                    faculty.designation.lowercase().contains(trimmedQuery) ||
                    faculty.mobile.contains(trimmedQuery)

            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Today's Birthdays Stream ---
    val todayBirthdays: StateFlow<List<FacultyEntity>> = allFaculty.combine(MutableStateFlow(Unit)) { list, _ ->
        val today = Calendar.getInstance()
        val curDay = today.get(Calendar.DAY_OF_MONTH)
        val curMonth = today.get(Calendar.MONTH) + 1 // 1-indexed

        list.filter { faculty ->
            val (fDay, fMonth) = parseDobDayMonth(faculty.dob)
            fDay == curDay && fMonth == curMonth
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- 7-Day Upcoming Birthday Radar Stream ---
    val upcomingBirthdays: StateFlow<List<UpcomingBirthdayData>> = allFaculty.combine(MutableStateFlow(Unit)) { list, _ ->
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val result = mutableListOf<UpcomingBirthdayData>()
        val dateDisplayFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

        for (faculty in list) {
            val (fDay, fMonth) = parseDobDayMonth(faculty.dob)
            if (fDay == null || fMonth == null) continue

            // Birthday this year
            val birthdayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.MONTH, fMonth - 1)
                set(Calendar.DAY_OF_MONTH, fDay)
            }

            // If already passed this year, check next year
            if (birthdayCal.before(today)) {
                birthdayCal.add(Calendar.YEAR, 1)
            }

            val diffMillis = birthdayCal.timeInMillis - today.timeInMillis
            val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

            if (diffDays in 1..7) {
                val label = when (diffDays) {
                    1 -> "Tomorrow"
                    2 -> "In 2 days"
                    else -> "In $diffDays days"
                }
                result.add(
                    UpcomingBirthdayData(
                        faculty = faculty,
                        daysRemaining = diffDays,
                        relativeLabel = label,
                        formattedDate = dateDisplayFormat.format(birthdayCal.time)
                    )
                )
            }
        }

        result.sortedBy { it.daysRemaining }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dashboard 2x2 Stats Stream ---
    val dashboardStats: StateFlow<DashboardStats> = combine(
        allFaculty,
        todayBirthdays,
        upcomingBirthdays,
        allDeptMappings
    ) { facultyList, todayList, upcomingList, depts ->
        DashboardStats(
            totalStaff = facultyList.size,
            todayBirthdaysCount = todayList.size,
            upcomingBirthdaysCount = upcomingList.size,
            activeDepartmentsCount = depts.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    init {
        viewModelScope.launch {
            repository.ensureDefaultData()
            loadDirectoryFromAssetsIfNeeded()
        }
    }

    /**
     * Seeds the official 510-record staff directory if count is low or empty,
     * without overwriting or deleting user changes across APK updates.
     */
    fun loadDirectoryFromAssetsIfNeeded() {
        viewModelScope.launch {
            try {
                val currentFaculty = repository.allFaculty.firstOrNull() ?: emptyList()

                // If the app only has the default minimal staff (< 100), automatically populate
                // all 510 staff from the bundled assets so the user immediately sees the entire college directory!
                if (currentFaculty.size < 100) {
                    val csvContent = com.example.utils.CsvImporter.readTextFromAssets(
                        getApplication(),
                        "sjc_staff_directory.csv"
                    )
                    if (csvContent.isNotBlank()) {
                        val parsed = com.example.utils.CsvImporter.parseCsv(csvContent)
                        if (parsed.isNotEmpty()) {
                            repository.insertFacultyList(parsed)
                        }
                    }
                }
                repository.removeDuplicates()
            } catch (e: Exception) {
                // Fallback silently if assets can't be read
            }
        }
    }

    /**
     * Merges the official 510 staff directory with current records without wiping custom entries.
     */
    fun reloadOfficialStaffDirectory(forceCleanReplace: Boolean = false) {
        viewModelScope.launch {
            try {
                val csvContent = com.example.utils.CsvImporter.readTextFromAssets(
                    getApplication(),
                    "sjc_staff_directory.csv"
                )
                if (csvContent.isBlank()) {
                    _eventMessages.emit("Could not read directory file from assets.")
                    return@launch
                }
                val parsed = com.example.utils.CsvImporter.parseCsv(csvContent)
                if (parsed.isEmpty()) {
                    _eventMessages.emit("Failed to parse official directory.")
                    return@launch
                }
                if (forceCleanReplace) {
                    repository.replaceAllFaculty(parsed)
                    _eventMessages.emit("Cleanly reset to official ${parsed.size} staff records!")
                } else {
                    repository.insertFacultyList(parsed)
                    _eventMessages.emit("Merged ${parsed.size} staff records. Existing edits preserved!")
                }
                val prefs = getApplication<android.app.Application>().getSharedPreferences("sjc_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("sjc_official_seeded_v1", true).apply()
            } catch (e: Exception) {
                _eventMessages.emit("Error loading directory: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Exports the entire directory to a CSV backup and opens the Android share sheet.
     */
    fun exportDirectoryToCsv(context: Context) {
        viewModelScope.launch {
            try {
                val list = allFaculty.value
                if (list.isEmpty()) {
                    _eventMessages.emit("Directory is empty. Nothing to export.")
                    return@launch
                }
                val csvContent = com.example.utils.CsvImporter.exportToCsv(list)
                val fileName = "SJC_Staff_Backup_${System.currentTimeMillis()}.csv"

                withContext(Dispatchers.IO) {
                    val cacheFile = java.io.File(context.cacheDir, fileName)
                    cacheFile.writeText(csvContent, Charsets.UTF_8)

                    val contentUri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        cacheFile
                    )

                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "SJC Staff Directory Backup")
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    val chooser = android.content.Intent.createChooser(shareIntent, "Save or Share Staff Backup (CSV)").apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                }
                _eventMessages.emit("Exported ${list.size} staff records! Choose where to save/share.")
            } catch (e: Exception) {
                _eventMessages.emit("Export failed: ${e.localizedMessage}")
            }
        }
    }

    // --- Actions ---

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleTestMode(enabled: Boolean) {
        _isTestMode.value = enabled
    }

    fun setTestPhoneNumber(phone: String) {
        _testPhoneNumber.value = phone
    }

    fun resolveDeptName(code: String): String {
        val clean = code.trim().uppercase()
        val fromStatic = AppDatabase.DEPARTMENT_MAPPINGS[clean]
        if (!fromStatic.isNullOrBlank()) {
            return fromStatic
        }
        val fromDb = deptMap.value[clean]
        if (!fromDb.isNullOrBlank() && !fromDb.equals(code, ignoreCase = true) && !fromDb.equals("Department of $code", ignoreCase = true)) {
            return fromDb
        }
        return CanvasCardDrawer.getDepartmentFullName(clean)
    }

    fun saveFaculty(faculty: FacultyEntity, isEdit: Boolean) {
        viewModelScope.launch {
            if (isEdit) {
                repository.updateFaculty(faculty)
                _eventMessages.emit("Updated faculty: ${faculty.name}")
            } else {
                repository.insertFaculty(faculty)
                _eventMessages.emit("Added new faculty: ${faculty.name}")
            }
        }
    }

    fun deleteFaculty(faculty: FacultyEntity) {
        viewModelScope.launch {
            repository.deleteFaculty(faculty)
            _eventMessages.emit("Deleted faculty: ${faculty.name}")
        }
    }

    fun saveDeptMapping(code: String, officialName: String) {
        viewModelScope.launch {
            val entity = DeptMappingEntity(code.trim().uppercase(), officialName.trim())
            repository.insertDeptMapping(entity)
            _eventMessages.emit("Saved mapping: ${entity.code} → ${entity.officialName}")
        }
    }

    fun deleteDeptMapping(code: String) {
        viewModelScope.launch {
            repository.deleteDeptMapping(code)
            _eventMessages.emit("Removed department code: $code")
        }
    }

    /**
     * Imports faculty records from raw CSV / TSV text content.
     */
    fun importFacultyFromCsvText(csvText: String) {
        viewModelScope.launch {
            try {
                val parsed = com.example.utils.CsvImporter.parseCsv(csvText)
                if (parsed.isEmpty()) {
                    _eventMessages.emit("No valid records found in CSV. Please verify columns.")
                    return@launch
                }
                repository.insertFacultyList(parsed)
                repository.removeDuplicates()
                _eventMessages.emit("Successfully imported ${parsed.size} records. Repetitive entries removed!")
            } catch (e: Exception) {
                _eventMessages.emit("CSV Import Failed: ${e.localizedMessage}")
            }
        }
    }

    fun deduplicateDirectory() {
        viewModelScope.launch {
            repository.removeDuplicates()
            _eventMessages.emit("Directory deduplication complete. Repetitive records removed.")
        }
    }

    /**
     * Imports faculty records from a document / file Uri.
     */
    fun importFacultyFromCsvUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    com.example.utils.CsvImporter.readTextFromUri(context, uri)
                }
                if (content.isBlank()) {
                    _eventMessages.emit("Selected file is empty or could not be read.")
                    return@launch
                }
                importFacultyFromCsvText(content)
            } catch (e: Exception) {
                _eventMessages.emit("File Read Error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Dispatches a test greeting to verify WhatsApp flow using the test phone number.
     */
    fun testWhatsAppDispatch(context: Context) {
        viewModelScope.launch {
            val sampleFaculty = todayBirthdays.value.firstOrNull()
                ?: allFaculty.value.firstOrNull()
                ?: FacultyEntity(
                    staffId = "SJC-TEST",
                    name = "Dr. Test Celebrant",
                    departmentCode = "CS",
                    designation = "Associate Professor",
                    dob = "16-09-1980",
                    mobile = _testPhoneNumber.value,
                    category = "Teaching"
                )
            _eventMessages.emit("Generating test wish for ${sampleFaculty.name} → +91 ${_testPhoneNumber.value}")
            generateAndSendGiftCard(context, sampleFaculty)
        }
    }

    /**
     * Generates HD Bitmap Card and dispatches through WhatsApp sender.
     */
    fun generateAndSendGiftCard(context: Context, faculty: FacultyEntity) {
        viewModelScope.launch {
            _generatingForId.value = faculty.id
            try {
                val resolvedDept = resolveDeptName(faculty.departmentCode)

                // 1. Generate Bitmap on Default Dispatcher
                val bitmap = withContext(Dispatchers.Default) {
                    CanvasCardDrawer.generateGreetingCardBitmap(context, faculty, resolvedDept)
                }

                // 2. Save to Cache & obtain FileProvider Uri on IO Dispatcher
                val imageUri = withContext(Dispatchers.IO) {
                    CanvasCardDrawer.saveCardToCache(context, bitmap, faculty.id)
                }

                // 3. Dispatch WhatsApp Intent
                WhatsAppSender.dispatchBirthdayGreeting(
                    context = context,
                    imageUri = imageUri,
                    faculty = faculty,
                    resolvedDept = resolvedDept,
                    isTestMode = _isTestMode.value,
                    customTestNumber = _testPhoneNumber.value
                )
            } catch (e: Exception) {
                _eventMessages.emit("Failed to generate card: ${e.localizedMessage}")
            } finally {
                _generatingForId.value = null
            }
        }
    }

    private fun parseDobDayMonth(dob: String): Pair<Int?, Int?> {
        val parts = dob.split("-")
        return if (parts.size >= 2) {
            val day = parts[0].toIntOrNull()
            val month = parts[1].toIntOrNull()
            day to month
        } else {
            null to null
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(
                        application,
                        kotlinx.coroutines.GlobalScope
                    )
                    val repo = FacultyRepository(db.facultyDao())
                    return MainViewModel(application, repo) as T
                }
            }
    }
}
