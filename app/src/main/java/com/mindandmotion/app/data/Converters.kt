package com.mindandmotion.app.data

import androidx.room.TypeConverter
import com.mindandmotion.app.data.journal.Mood
import java.time.LocalDate

/**
 * TypeConverters comune pentru AppDatabase (MM-02, [TU]).
 *
 * NOTĂ pentru [TU]: TaskEntity (MM-10) are și el un câmp `dueDate: LocalDate?`, deci
 * va folosi `fromLocalDate`/`toLocalDate` de aici — nu mai e nevoie de o clasă nouă.
 * Dacă mai ai nevoie de un converter pentru enum-ul Priority, adaugă-l TOT în clasa
 * asta (Room ia o singură listă de @TypeConverters per @Database).
 */
class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString() // ISO-8601 (yyyy-MM-dd)

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromMood(mood: Mood?): String? = mood?.name

    @TypeConverter
    fun toMood(value: String?): Mood? = value?.let { Mood.valueOf(it) }
}
