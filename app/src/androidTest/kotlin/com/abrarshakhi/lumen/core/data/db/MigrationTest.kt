package com.abrarshakhi.lumen.core.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
 * Verifies the hand-written 1→2 migration.
 *
 * Worth testing rather than trusting: the migration creates an external-content FTS4 table,
 * whose synchronisation triggers Room installs automatically on a fresh database but which
 * a migration has to write out by hand. Get those wrong and the index stays silently empty
 * — note search would simply never find anything, with no error to notice.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        LumenDatabase::class.java,
    )

    @Test
    fun migrate1To2_preservesDataAndBuildsWorkingFtsIndex() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                "INSERT INTO usage_stats (ranking_key, launch_count, last_launched_at) " +
                    "VALUES ('app:demo', 7, 1700000000000)",
            )
            close()
        }

        // Validates the resulting schema against the exported 2.json.
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        // Existing data must survive: usage stats are learned over real use.
        db.query("SELECT launch_count FROM usage_stats WHERE ranking_key = 'app:demo'").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(7, cursor.getInt(0))
        }

        // The triggers must actually populate the index, or search finds nothing.
        db.execSQL(
            "INSERT INTO notes (title, body, created_at, updated_at) " +
                "VALUES ('Shopping', 'milk and bread', 1, 1)",
        )
        // `notes` and `notes_fts` both have a `title` column, so it must be qualified.
        db.query(
            "SELECT notes.title FROM notes JOIN notes_fts ON notes.rowid = notes_fts.rowid " +
                "WHERE notes_fts MATCH 'milk*'",
        )
            .use { cursor ->
                assertEquals(true, cursor.moveToFirst(), "FTS index was not populated by the triggers")
                assertEquals("Shopping", cursor.getString(0))
            }

        db.close()
    }

    @Test
    fun migrate2To3_addsDocumentIndexAndKeepsNotes() {
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                "INSERT INTO notes (title, body, created_at, updated_at) " +
                    "VALUES ('Keep me', 'body', 1, 1)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_1_2, MIGRATION_2_3)

        db.query("SELECT title FROM notes").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("Keep me", cursor.getString(0))
        }
        // The new table must exist and be queryable.
        db.query("SELECT COUNT(*) FROM file_index").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }

        db.close()
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}
