package tr.yigitunlu.n11chatapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tr.yigitunlu.n11chatapp.data.local.converter.RoomConverters
import tr.yigitunlu.n11chatapp.data.local.dao.ChatMessageDao
import tr.yigitunlu.n11chatapp.data.local.entity.ChatMessageEntity

/**
 * Room database for chat application
 */
@Database(
    entities = [ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        private const val DATABASE_NAME = "chat_database"

        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getInstance(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
