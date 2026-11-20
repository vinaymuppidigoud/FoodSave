package com.example.foodsave.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    // Database table
    entities = [FoodItem::class],
    version = 1,
    exportSchema = false
)
abstract class FoodDatabase : RoomDatabase() {

    // Get DAO
    abstract fun foodDao(): FoodDao

    companion object {

        // Database instance
        @Volatile
        private var INSTANCE: FoodDatabase? = null

        // Get database
        fun getDatabase(context: Context): FoodDatabase {

            return INSTANCE ?: synchronized(this) {

                // Create database
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoodDatabase::class.java,
                    "foodsave_database"
                ).build()

                INSTANCE = instance

                instance
            }
        }
    }
}