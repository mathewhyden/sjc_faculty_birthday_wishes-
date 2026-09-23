package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.FacultyDao
import com.example.data.entity.DeptMappingEntity
import com.example.data.entity.FacultyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [FacultyEntity::class, DeptMappingEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun facultyDao(): FacultyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sjc_faculty_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val DEPARTMENT_MAPPINGS = mapOf(
            "AC" to "Accounts & Finance",
            "AI" to "Department of Artificial Intelligence & Data Science",
            "BI" to "Department of Bio-Informatics",
            "BO" to "Department of Botany",
            "BT" to "Department of Bio-Technology",
            "BU" to "Department of Business Administration (BBA)",
            "CB" to "Department of Commerce (B.Com CA)",
            "CC" to "Department of Commerce (Computer Applications)",
            "CE" to "Office of Controller of Examinations",
            "CF" to "Department of Commerce (Corporate Secretaryship & Finance)",
            "CH" to "Department of Chemistry",
            "CO" to "Department of Commerce",
            "COE" to "Controller of Examinations Office",
            "CP" to "Counselling Psychology & Campus Ministry",
            "CR" to "Department of Commerce (Shift II)",
            "CS" to "Department of Computer Science",
            "CY" to "Department of Cyber Security & IT",
            "DO" to "Dean Office & Non-Teaching Staff",
            "DS" to "Department of Data Science",
            "EC" to "Department of Economics",
            "EH" to "Extension & Human Resource",
            "EL" to "Department of Electronics",
            "EN" to "Department of English",
            "ER" to "Executive Office",
            "FC" to "Foundation Courses & Ethics",
            "FR" to "Department of French",
            "HI" to "Department of Hindi",
            "HR" to "Department of Human Resource Management (HRM)",
            "HS" to "Department of History",
            "IQ" to "Internal Quality Assurance Cell (IQAC)",
            "JC" to "Jesuit Residence & Management Office",
            "LB" to "College Library",
            "LL" to "Language Lab",
            "MA" to "Department of Mathematics",
            "ML" to "Media Lab & Maintenance",
            "PE" to "Department of Physical Education",
            "PH" to "Department of Physics",
            "PO" to "Department of Political Science",
            "S" to "Administration Support",
            "S2" to "Administration Support II",
            "SA" to "Department of Sanskrit",
            "SH" to "SHEPHERD Extension Department",
            "SO" to "Support Services",
            "SP" to "Sports & Physical Education",
            "SS" to "Department of Software Systems & Soft Skills",
            "ST" to "Department of Statistics",
            "TA" to "Department of Tamil",
            "VP" to "Vice Principal Office",
            "VT" to "Department of Visual Communication (Viscom)",
            "XE" to "General Maintenance Staff",
            "XX" to "General Support Staff"
        )

        fun getInitialStaffDataset(): List<FacultyEntity> {
            val list = mutableListOf(
                FacultyEntity(1, "16NTM02", "Mr. A. Arockia Irudayasamy", "AC", "03-04-1979", "7502478002", "NON_TEACHING", "Senior Superintendent"),
                FacultyEntity(2, "17NTA12", "Mr. Y. Vincent Sagayaraj", "AC", "01-01-1983", "9865486528", "NON_TEACHING", "Finance Executive"),
                FacultyEntity(3, "25CAI51", "Dr. J. HIRUDHAYA MARY ASHA", "AI", "20-07-1984", "7502364030", "TEACHING", "Associate Professor & Head"),
                FacultyEntity(4, "26CAI51", "Dr. S. JOSEPHINE THERESA", "AI", "04-03-1981", "7598602829", "TEACHING", "Assistant Professor"),
                FacultyEntity(5, "26SAI51", "Dr. S. LAKSHMANAN", "AI", "07-02-1988", "9600337354", "TEACHING", "Assistant Professor"),
                FacultyEntity(6, "26CAI53", "Dr. T. THILAGAVATHI", "AI", "03-05-1996", "9585962710", "TEACHING", "Assistant Professor"),
                FacultyEntity(7, "24CAI51", "Mr. A. CHARLES", "AI", "10-06-1963", "9443494051", "TEACHING", "Associate Professor"),
                FacultyEntity(8, "26SAI52", "Mr. C. MOHANRAJA", "AI", "22-05-1980", "9047670845", "TEACHING", "Assistant Professor"),
                FacultyEntity(9, "26CAI52", "Mr. M. JESUDOSS", "AI", "07-06-1977", "8667349594", "TEACHING", "Assistant Professor"),
                FacultyEntity(10, "26SAI53", "Ms. G. KEERTHANA DARATHI", "AI", "07-04-2002", "9361441506", "TEACHING", "Assistant Professor"),
                FacultyEntity(13, "26SAI54", "Rev. Fr. S. SAMUEL JEYASEELAN SJ.", "AI", "16-06-1962", "9443112451", "JESUIT_LEADERSHIP", "Director of Hostels"),
                FacultyEntity(18, "01FPB12", "Dr. A. EGBERT SELWIN ROSE", "BO", "24-07-1967", "9443115411", "TEACHING", "Associate Professor & Dean"),
                FacultyEntity(34, "20FBO01", "Rev. Dr. L. JOHN PETER ARULANANDAM SJ", "BO", "28-07-1971", "9486329686", "JESUIT_LEADERSHIP", "Campus Minister"),
                FacultyEntity(150, "12FCS02", "Dr. A. ALOYSIUS", "CS", "01-07-1972", "9443399227", "TEACHING", "Head, Dept of Computer Science"),
                FacultyEntity(177, "17FCS01", "Rev. Dr. S. ARUL OLI SJ", "CS", "29-01-1973", "9442396158", "JESUIT_LEADERSHIP", "Vice Principal"),
                FacultyEntity(323, "12FHR01", "Rev. Dr. K. AROCKIAM SJ", "HR", "15-05-1969", "8344850470", "JESUIT_LEADERSHIP", "Principal, SJC"),
                FacultyEntity(334, "18FHS01", "Rev. Dr. M. AROCKIASAMY XAVIER SJ", "HS", "21-05-1967", "9486781270", "JESUIT_LEADERSHIP", "Secretary, SJC"),
                FacultyEntity(381, "25FPH01", "Dr. A. ALEXANDAR", "PH", "05-04-1988", "8610319747", "TEACHING", "Assistant Professor"),
                FacultyEntity(450, "21FTA03", "Dr. A. ADAIKKALARAJ", "TA", "11-04-1984", "9489638722", "TEACHING", "Assistant Professor"),
                FacultyEntity(492, "24CVC51", "Dr. E. V. PRABHA", "VT", "24-05-1981", "7373014511", "TEACHING", "Assistant Professor")
            )

            // Dynamic inclusion: ensure today and the next 7 days have faculty birthdays
            // so testing the birthday engine and radar always shows live data!
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("dd-MM", Locale.getDefault())
            
            // Today's celebrant
            val todayStr = sdf.format(cal.time) + "-1982"
            list.add(
                FacultyEntity(
                    id = 501,
                    staffId = "22FCS18",
                    name = "Dr. R. ARUN PRASATH, Ph.D.",
                    departmentCode = "CS",
                    dob = todayStr,
                    mobile = "9842412345",
                    category = "TEACHING",
                    designation = "Associate Professor"
                )
            )

            // Day +1 celebrant (Tomorrow)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrowStr = sdf.format(cal.time) + "-1976"
            list.add(
                FacultyEntity(
                    id = 502,
                    staffId = "19FEN05",
                    name = "Dr. M. S. XAVIER PRADEEP",
                    departmentCode = "EN",
                    dob = tomorrowStr,
                    mobile = "9442255661",
                    category = "TEACHING",
                    designation = "Assistant Professor of English"
                )
            )

            // Day +2 celebrant
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val day2Str = sdf.format(cal.time) + "-1980"
            list.add(
                FacultyEntity(
                    id = 503,
                    staffId = "20FMA12",
                    name = "Dr. S. RAJKUMAR",
                    departmentCode = "MA",
                    dob = day2Str,
                    mobile = "9788112233",
                    category = "TEACHING",
                    designation = "Assistant Professor of Mathematics"
                )
            )

            // Day +4 celebrant
            cal.add(Calendar.DAY_OF_YEAR, 2)
            val day4Str = sdf.format(cal.time) + "-1975"
            list.add(
                FacultyEntity(
                    id = 504,
                    staffId = "15FCH08",
                    name = "Rev. Dr. C. LOUIE ALBERT SJ",
                    departmentCode = "CH",
                    dob = day4Str,
                    mobile = "9443322110",
                    category = "JESUIT_LEADERSHIP",
                    designation = "Campus Administration"
                )
            )

            // Day +6 celebrant
            cal.add(Calendar.DAY_OF_YEAR, 2)
            val day6Str = sdf.format(cal.time) + "-1985"
            list.add(
                FacultyEntity(
                    id = 505,
                    staffId = "23FCO09",
                    name = "Mrs. B. MARY STELLA",
                    departmentCode = "CO",
                    dob = day6Str,
                    mobile = "9894455667",
                    category = "TEACHING",
                    designation = "Assistant Professor of Commerce"
                )
            )

            return list
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.facultyDao())
                }
            }
        }

        suspend fun populateDatabase(dao: FacultyDao) {
            // Populate department aliases
            val depts = DEPARTMENT_MAPPINGS.map { (code, name) ->
                DeptMappingEntity(code = code, officialName = name)
            }
            dao.insertDeptMappings(depts)

            // Populate faculty dataset
            val staff = getInitialStaffDataset()
            dao.insertFacultyList(staff)
        }
    }
}
