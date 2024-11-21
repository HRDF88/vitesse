package com.example.vitesse.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.vitesse.data.dao.CandidateDtoDao
import com.example.vitesse.data.entity.CandidateDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Database class that extends RoomDatabase and represents the local database for the application.
 * This class defines the entities, version, and type converters used by the database.
 * It also provides DAO (Data Access Object) methods for accessing the specific entity tables.
 *
 * @property candidateDtoDao The DAO for accessing CandidateDto data.
 */
@Database(
    entities = [CandidateDto::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun candidateDtoDao(): CandidateDtoDao

    /**
     * Callback used to perform actions during database creation.
     */
    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            //Launch a coroutine to insert default data into the database
            scope.launch {
                val testImageByteArray = ByteArray(10) { it.toByte() }
                // Retrieve the DAO once the database is initialized
                val candidateDtoDao = INSTANCE?.candidateDtoDao()
                candidateDtoDao?.let {
                    try {
                        val defaultCandidate = CandidateDto(
                            firstName = "John",
                            surName = "Doe",
                            phoneNumbers = "1234567890",
                            email = "john.doe@example.com",
                            dateOfBirth = LocalDateTime.now(),
                            expectedSalaryEuros = 30000,
                            note = "Test candidate",
                            profilePicture = testImageByteArray,
                            favorite = false
                        )
                        // Insert default data
                        it.insertCandidate(defaultCandidate)
                    } catch (e: Exception) {
                        // Handle any exception when inserting default data
                        Log.e("AppDatabaseCallback", "Error inserting default data", e)
                    }
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        // method not used to insert a new field during database migration
        /*
        private val UPGRADE_MIGRATION_V1_V2 = object : Migration(1, 2) {

            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE candidate ADD COLUMN nouveauchamp TEXT DEFAULT 0 NOT NULL")
            }
        }
        */

        /**
         * Method to get a database instance.
         */
        fun getDatabase(context: Context, coroutineScope: CoroutineScope): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "VitesseDB"
                )
                    // Ajouter le callback pour insérer des données par défaut après la création
                    .addCallback(AppDatabaseCallback(coroutineScope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}