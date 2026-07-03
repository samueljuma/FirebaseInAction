package com.samueljuma.firebaseinaction.data.notes.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.samueljuma.firebaseinaction.data.notifications.local.NotificationDao
import com.samueljuma.firebaseinaction.data.notifications.local.NotificationEntity

@Database(
    entities = [NoteEntity::class, NotificationEntity::class],
    version = 7,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun notificationDao(): NotificationDao
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE notes ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE notes ADD COLUMN imageUrl TEXT DEFAULT NULL"
                )
            }
        }
        // Renames isPinned → pinned and isSynced → synced.
        // RENAME COLUMN requires SQLite 3.25 (API 30+), so we recreate the table instead.
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE notes_new (
                        id        TEXT NOT NULL PRIMARY KEY,
                        userId    TEXT NOT NULL,
                        title     TEXT NOT NULL,
                        content   TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        pinned    INTEGER NOT NULL DEFAULT 0,
                        synced    INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        imageUrl  TEXT
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO notes_new (id, userId, title, content, createdAt, updatedAt, pinned, synced, isDeleted, imageUrl)
                    SELECT id, userId, title, content, createdAt, updatedAt, isPinned, isSynced, isDeleted, imageUrl
                    FROM notes
                """.trimIndent())
                db.execSQL("DROP TABLE notes")
                db.execSQL("ALTER TABLE notes_new RENAME TO notes")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notifications (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        body TEXT NOT NULL,
                        receivedAt INTEGER NOT NULL,
                        read INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }
        // Adds per-note reminders: reminderAt (when it's due) and reminderFiredAt
        // (null while pending; set once delivered so it can't fire twice).
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN reminderAt INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE notes ADD COLUMN reminderFiredAt INTEGER DEFAULT NULL")
            }
        }
        // Notifications now carry where a tap should navigate (e.g. a reminder's note),
        // instead of always opening the generic inbox.
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notifications ADD COLUMN deepLink TEXT DEFAULT NULL")
            }
        }
    }
}