package com.example.foodsave.camera

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FoodImageManager {

    // Save captured food image
    fun saveImage(
        context: Context,
        bitmap: Bitmap,
        name: String = "${UUID.randomUUID()}.jpg"
    ): String {

        // Create food_images directory
        val directory = File(
            context.filesDir,
            "food_images"
        )

        if (!directory.exists()) {
            val created = directory.mkdirs()
            if (!created) {
                throw Exception("Unable to create food_images directory.")
            }
        }

        // Create image file
        val imageFile = File(directory, name)

        // Save bitmap as JPEG
        FileOutputStream(imageFile).use { output ->

            val success = bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                85,
                output
            )
            if (!success) {
                throw Exception("Unable to compress food image.")
            }
            output.flush()
        }

        // Return saved image path
        return imageFile.absolutePath
    }

    // Get all saved food images
    fun getSavedImages(context: Context): List<File> {

        val directory = File(
            context.filesDir,
            "food_images"
        )

        return directory.listFiles()
            ?.filter {
                it.isFile &&
                        it.extension.equals("jpg", ignoreCase = true)
            }
            ?.sortedByDescending {
                it.lastModified()
            }
            ?: emptyList()
    }

    // Delete a saved food image
    fun deleteImage(imagePath: String): Boolean {

        val imageFile = File(imagePath)

        return if (imageFile.exists() && imageFile.isFile) {
            imageFile.delete()
        } else {
            false
        }
    }
}