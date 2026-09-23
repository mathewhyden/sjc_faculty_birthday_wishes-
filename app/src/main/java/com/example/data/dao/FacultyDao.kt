package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.DeptMappingEntity
import com.example.data.entity.FacultyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FacultyDao {

    // --- Faculty Queries ---

    @Query("SELECT * FROM faculty ORDER BY name ASC")
    fun getAllFaculty(): Flow<List<FacultyEntity>>

    @Query("SELECT * FROM faculty WHERE id = :id")
    fun getFacultyById(id: Long): Flow<FacultyEntity?>

    @Query("SELECT * FROM faculty WHERE id = :id")
    suspend fun getFacultyByIdDirect(id: Long): FacultyEntity?

    @Query("SELECT * FROM faculty WHERE category = :category ORDER BY name ASC")
    fun getFacultyByCategory(category: String): Flow<List<FacultyEntity>>

    @Query("""
        SELECT * FROM faculty 
        WHERE name LIKE '%' || :query || '%' 
           OR staffId LIKE '%' || :query || '%' 
           OR departmentCode LIKE '%' || :query || '%'
           OR designation LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchFaculty(query: String): Flow<List<FacultyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaculty(faculty: FacultyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacultyList(list: List<FacultyEntity>)

    @Query("""
        DELETE FROM faculty WHERE id NOT IN (
            SELECT MIN(id) FROM faculty GROUP BY LOWER(TRIM(staffId)), LOWER(TRIM(name))
        )
    """)
    suspend fun removeDuplicates()

    @Query("SELECT * FROM faculty")
    suspend fun getAllFacultyDirect(): List<FacultyEntity>

    @Update
    suspend fun updateFaculty(faculty: FacultyEntity)

    @Delete
    suspend fun deleteFaculty(faculty: FacultyEntity)

    @Query("DELETE FROM faculty WHERE id = :id")
    suspend fun deleteFacultyById(id: Long)

    @Query("DELETE FROM faculty")
    suspend fun clearAllFaculty()

    @Query("SELECT COUNT(*) FROM faculty")
    fun getFacultyCount(): Flow<Int>

    // --- Department Mapping Queries ---

    @Query("SELECT * FROM dept_mappings ORDER BY code ASC")
    fun getAllDeptMappings(): Flow<List<DeptMappingEntity>>

    @Query("SELECT * FROM dept_mappings WHERE code = :code")
    suspend fun getDeptMapping(code: String): DeptMappingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeptMapping(dept: DeptMappingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeptMappings(list: List<DeptMappingEntity>)

    @Update
    suspend fun updateDeptMapping(dept: DeptMappingEntity)

    @Query("DELETE FROM dept_mappings WHERE code = :code")
    suspend fun deleteDeptMapping(code: String)

    @Query("SELECT COUNT(*) FROM dept_mappings")
    fun getDeptMappingsCount(): Flow<Int>
}
