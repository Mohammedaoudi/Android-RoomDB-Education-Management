package ma.ensa.projet.data.converters


import androidx.room.TypeConverter
import java.util.Date

class DateConverter {

    @TypeConverter
    fun getDateFromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun getTimestampFromDate(date: Date?): Long? {
        return date?.time
    }
}
