package com.mindandmotion.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mindandmotion.app.data.journal.JournalDao
import com.mindandmotion.app.data.journal.JournalEntryEntity
import com.mindandmotion.app.data.task.TaskDao
import com.mindandmotion.app.data.task.TaskEntity

@Database(
    entities = [TaskEntity::class, JournalEntryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun journalDao(): JournalDao

    companion object {
        private const val DB_NAME = "mind_and_motion.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
