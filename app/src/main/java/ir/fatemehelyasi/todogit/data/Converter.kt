package ir.fatemehelyasi.todogit.data

import androidx.room.TypeConverter
import ir.fatemehelyasi.todogit.data.models.Priority

class Converter {

    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String): Priority {
        return Priority.valueOf(priority)
    }

}