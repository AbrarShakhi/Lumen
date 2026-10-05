package com.abrarshakhi.lumen.core.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `notes` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `body` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL
            )
            """.trimIndent(),
        )

        db.execSQL(
            "CREATE VIRTUAL TABLE IF NOT EXISTS `notes_fts` USING FTS4(" +
                "`title` TEXT NOT NULL, `body` TEXT NOT NULL, content=`notes`)",
        )

        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_notes_fts_BEFORE_UPDATE
            BEFORE UPDATE ON `notes` BEGIN
                DELETE FROM `notes_fts` WHERE `docid` = OLD.`rowid`;
            END
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_notes_fts_BEFORE_DELETE
            BEFORE DELETE ON `notes` BEGIN
                DELETE FROM `notes_fts` WHERE `docid` = OLD.`rowid`;
            END
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_notes_fts_AFTER_UPDATE
            AFTER UPDATE ON `notes` BEGIN
                INSERT INTO `notes_fts`(`docid`, `title`, `body`)
                VALUES (NEW.`rowid`, NEW.`title`, NEW.`body`);
            END
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_notes_fts_AFTER_INSERT
            AFTER INSERT ON `notes` BEGIN
                INSERT INTO `notes_fts`(`docid`, `title`, `body`)
                VALUES (NEW.`rowid`, NEW.`title`, NEW.`body`);
            END
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `file_index` (
                `document_uri` TEXT NOT NULL,
                `tree_uri` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `normalized_name` TEXT NOT NULL,
                `mime_type` TEXT,
                `size_bytes` INTEGER NOT NULL,
                `modified_at` INTEGER NOT NULL,
                `folder` TEXT,
                `indexed_at` INTEGER NOT NULL,
                PRIMARY KEY(`document_uri`)
            )
            """.trimIndent(),
        )
    }
}
