package com.example.foodsave.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

// Food Item Table
@Parcelize
@Entity(tableName = "food_items")
data class FoodItem(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val name: String,
    val quantity: Int,
    val expiryDate: String,
    val imagePath: String? = null,
    val imageUrl: String? = null,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
) : Parcelable