package com.abrarshakhi.lumen.core.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UsageStatEntity::class,
        AppIndexEntity::class,
        NoteEntity::class,
        NoteFtsEntity::class,
        FileIndexEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class LumenDatabase : RoomDatabase() {

    abstract fun usageStatDao(): UsageStatDao

    abstract fun appIndexDao(): AppIndexDao

    abstract fun noteDao(): NoteDao

    abstract fun fileIndexDao(): FileIndexDao

    companion object {
        const val NAME = "lumen.db"

        fun create(context: Context): LumenDatabase =
            Room.databaseBuilder(context, LumenDatabase::class.java, NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
    }
}
