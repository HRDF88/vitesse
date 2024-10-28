package com.example.vitesse.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.vitesse.data.dao.CandidateDtoDao
import com.example.vitesse.data.dao.FavoriteCandidateDtoDao
import com.example.vitesse.data.entity.CandidateDto
import com.example.vitesse.data.entity.FavoriteCandidateDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Database class that extends RoomDatabase and represents the local database for the application.
 * This class defines the entities, version, and type converters used by the database.
 * It also provides DAO (Data Access Object) methods for accessing the specific entity tables.
 *
 * @property candidateDtoDao The DAO for accessing CandidateDto data.
 * @property favoriteCandidateDtoDao The DAO for accessing FavoriteDto data.

 */
@Database(
    entities = [CandidateDto::class,FavoriteCandidateDto::class],
    version = 1,
    exportSchema = false
)

abstract class AppDataBase:RoomDatabase() {
    abstract fun candidateDtoDao(): CandidateDtoDao
    abstract fun favoriteCandidateDtoDao(): FavoriteCandidateDtoDao

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {

                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null


        fun getDatabase(context: Context, coroutineScope: CoroutineScope): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "VitesseDB"
                )
                    .addCallback(AppDatabaseCallback(coroutineScope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}