package com.example.masajeslg.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Client::class, Service::class, Appointment::class],
    version = 3,
    exportSchema = false
)

@TypeConverters(Converters::class) // <-- AQUI
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun serviceDao(): ServiceDao
    abstract fun appointmentDao(): AppointmentDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS services (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        price REAL NOT NULL,
                        active INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS appointments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        clientId INTEGER NOT NULL,
                        serviceId INTEGER NOT NULL,
                        startAt INTEGER NOT NULL,
                        endAt INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        notes TEXT
                    )
                """.trimIndent())
                // Opcional: índices de búsqueda
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_appointments_startAt ON appointments(startAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_appointments_clientId ON appointments(clientId)")
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "masajeslg.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration() // <-- Agregado
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
