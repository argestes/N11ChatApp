package tr.yigitunlu.n11chatapp.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import tr.yigitunlu.n11chatapp.domain.model.MessageOption
import tr.yigitunlu.n11chatapp.domain.model.ServerMessageType
import tr.yigitunlu.n11chatapp.domain.model.SyncStatus

/**
 * Type converters for Room to handle complex types
 */
class RoomConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromMessageOptionList(options: List<MessageOption>?): String? {
        return options?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toMessageOptionList(optionsString: String?): List<MessageOption>? {
        if (optionsString == null) return null
        val type = object : TypeToken<List<MessageOption>>() {}.type
        return gson.fromJson(optionsString, type)
    }

    // ServerMessageType converters
    @TypeConverter
    fun fromServerMessageType(type: ServerMessageType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toServerMessageType(typeName: String?): ServerMessageType? {
        return typeName?.let { ServerMessageType.valueOf(it) }
    }

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toSyncStatus(statusName: String?): SyncStatus? {
        return statusName?.let { SyncStatus.valueOf(it) }
    }
}
