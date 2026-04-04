package com.example.dev_mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dev_mobile.data.local.dao.FestivalDao
import com.example.dev_mobile.data.local.dao.PendingOperationDao
import com.example.dev_mobile.data.local.dao.ReservantDao
import com.example.dev_mobile.data.local.dao.ReservationDao
import com.example.dev_mobile.data.local.entity.FestivalEntity
import com.example.dev_mobile.data.local.entity.PendingOperationEntity
import com.example.dev_mobile.data.local.entity.ReservantEntity
import com.example.dev_mobile.data.local.entity.ReservationEntity

@Database(
    entities = [
        FestivalEntity::class,
        ReservantEntity::class,
        ReservationEntity::class,
        PendingOperationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun festivalDao(): FestivalDao
    abstract fun reservantDao(): ReservantDao
    abstract fun reservationDao(): ReservationDao
    abstract fun pendingOperationDao(): PendingOperationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "festijeux_offline.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}