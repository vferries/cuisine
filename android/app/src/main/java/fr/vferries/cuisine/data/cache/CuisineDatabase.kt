package fr.vferries.cuisine.data.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [RecipeMetaEntity::class, RecipeEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class CuisineDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile private var instance: CuisineDatabase? = null

        fun get(context: Context): CuisineDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                CuisineDatabase::class.java,
                "cuisine.db",
            ).build().also { instance = it }
        }
    }
}
