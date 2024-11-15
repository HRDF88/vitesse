package com.example.vitesse.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.vitesse.data.dao.CandidateDtoDao
import com.example.vitesse.data.entity.CandidateDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.reflect.KParameter

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
     * Callback utilisé pour effectuer des actions lors de la création de la base de données.
     */
    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // Lancer une coroutine pour insérer des données par défaut dans la base de données
            scope.launch {
                // Récupérer le DAO une fois que la base de données est initialisée
                val candidateDtoDao = INSTANCE?.candidateDtoDao() // Accéder au DAO après la création de la base
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
                            profilePicture = "profile",
                            favorite = false
                        )
                        // Insertion des données par défaut
                        it.insertCandidate(defaultCandidate)
                    } catch (e: Exception) {
                        // Gérer toute exception lors de l'insertion des données par défaut
                        Log.e("AppDatabaseCallback", "Erreur lors de l'insertion des données par défaut", e)
                    }
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        private val UPGRADE_MIGRATION_V1_V2 = object: Migration(1,2) {

            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE candidate ADD COLUMN nouveauchamp TEXT DEFAULT 0 NOT NULL")
            }
        }

        /**
         * Méthode pour obtenir une instance de la base de données.
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