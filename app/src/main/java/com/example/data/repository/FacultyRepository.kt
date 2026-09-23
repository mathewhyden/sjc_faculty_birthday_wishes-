package com.example.data.repository

import com.example.data.dao.FacultyDao
import com.example.data.database.AppDatabase
import com.example.data.entity.DeptMappingEntity
import com.example.data.entity.FacultyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class FacultyRepository(private val facultyDao: FacultyDao) {

    val allFaculty: Flow<List<FacultyEntity>> = facultyDao.getAllFaculty()
    val allDeptMappings: Flow<List<DeptMappingEntity>> = facultyDao.getAllDeptMappings()
    val facultyCount: Flow<Int> = facultyDao.getFacultyCount()
    val deptMappingsCount: Flow<Int> = facultyDao.getDeptMappingsCount()

    fun searchFaculty(query: String): Flow<List<FacultyEntity>> {
        return facultyDao.searchFaculty(query)
    }

    fun getFacultyByCategory(category: String): Flow<List<FacultyEntity>> {
        return facultyDao.getFacultyByCategory(category)
    }

    fun getFacultyById(id: Long): Flow<FacultyEntity?> {
        return facultyDao.getFacultyById(id)
    }

    suspend fun getFacultyByIdDirect(id: Long): FacultyEntity? = withContext(Dispatchers.IO) {
        facultyDao.getFacultyByIdDirect(id)
    }

    suspend fun insertFaculty(faculty: FacultyEntity): Long = withContext(Dispatchers.IO) {
        facultyDao.insertFaculty(faculty)
    }

    suspend fun insertFacultyList(list: List<FacultyEntity>) = withContext(Dispatchers.IO) {
        // 1. Deduplicate the incoming list by staffId and name
        val cleanList = list.distinctBy {
            it.staffId.trim().lowercase() to it.name.trim().lowercase()
        }
        // 2. Fetch existing to match and avoid duplicate inserts
        val existing = facultyDao.getAllFacultyDirect()
        val existingStaffIds = existing.associateBy { it.staffId.trim().lowercase() }
        val existingNames = existing.associateBy { it.name.trim().lowercase() }

        val toInsertOrUpdate = cleanList.map { item ->
            val match = existingStaffIds[item.staffId.trim().lowercase()]
                ?: existingNames[item.name.trim().lowercase()]
            if (match != null) {
                item.copy(id = match.id)
            } else {
                item
            }
        }

        facultyDao.insertFacultyList(toInsertOrUpdate)
        facultyDao.removeDuplicates()
    }

    suspend fun removeDuplicates() = withContext(Dispatchers.IO) {
        facultyDao.removeDuplicates()
    }

    suspend fun updateFaculty(faculty: FacultyEntity) = withContext(Dispatchers.IO) {
        facultyDao.updateFaculty(faculty)
    }

    suspend fun deleteFaculty(faculty: FacultyEntity) = withContext(Dispatchers.IO) {
        facultyDao.deleteFaculty(faculty)
    }

    suspend fun deleteFacultyById(id: Long) = withContext(Dispatchers.IO) {
        facultyDao.deleteFacultyById(id)
    }

    suspend fun insertDeptMapping(dept: DeptMappingEntity) = withContext(Dispatchers.IO) {
        facultyDao.insertDeptMapping(dept)
    }

    suspend fun updateDeptMapping(dept: DeptMappingEntity) = withContext(Dispatchers.IO) {
        facultyDao.updateDeptMapping(dept)
    }

    suspend fun clearAllFaculty() = withContext(Dispatchers.IO) {
        facultyDao.clearAllFaculty()
    }

    suspend fun replaceAllFaculty(list: List<FacultyEntity>) = withContext(Dispatchers.IO) {
        val cleanList = list.distinctBy {
            it.staffId.trim().lowercase() to it.name.trim().lowercase()
        }
        facultyDao.clearAllFaculty()
        facultyDao.insertFacultyList(cleanList)
    }

    suspend fun deleteDeptMapping(code: String) = withContext(Dispatchers.IO) {
        facultyDao.deleteDeptMapping(code)
    }

    /**
     * Ensures initial dataset is present even if Room callback didn't fire.
     */
    suspend fun ensureDefaultData() = withContext(Dispatchers.IO) {
        val existingFaculty = facultyDao.getAllFaculty().firstOrNull()
        if (existingFaculty.isNullOrEmpty()) {
            val staff = AppDatabase.getInitialStaffDataset()
            facultyDao.insertFacultyList(staff)
        }

        val existingDepts = facultyDao.getAllDeptMappings().firstOrNull()
        if (existingDepts.isNullOrEmpty()) {
            val depts = AppDatabase.DEPARTMENT_MAPPINGS.map { (code, name) ->
                DeptMappingEntity(code = code, officialName = name)
            }
            facultyDao.insertDeptMappings(depts)
        }
    }
}
