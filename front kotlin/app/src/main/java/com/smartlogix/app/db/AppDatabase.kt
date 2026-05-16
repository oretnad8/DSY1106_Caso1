package com.smartlogix.app.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// DAOS
import com.smartlogix.app.db.daos.*

// ENTITIES
import com.smartlogix.app.db.entities.*

@Database(
    entities = [
        ProductoLocal::class,
        UbicacionLocal::class,
        SolicitudMovimientoLocal::class,
        AprobacionLocal::class,
        UsuarioLocal::class,
        ConteoLocal::class,
        AsignacionUbicacionLocal::class
    ],
    version = 9, // Incrementado para soportar estantes multinivel en P3
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs
    abstract fun productoDao(): ProductoDao
    abstract fun ubicacionDao(): UbicacionDao
    abstract fun solicitudMovimientoDao(): SolicitudMovimientoDao
    abstract fun aprobacionDao(): AprobacionDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun conteoDao(): ConteoDao
    abstract fun asignacionUbicacionDao(): AsignacionUbicacionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "SmartLogix_wms_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


