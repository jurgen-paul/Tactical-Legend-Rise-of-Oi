package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.GearType
import com.example.data.model.HeroClass
import com.example.data.model.Rarity

class Converters {
    @TypeConverter
    fun fromHeroClass(value: HeroClass): String = value.name

    @TypeConverter
    fun toHeroClass(value: String): HeroClass = HeroClass.valueOf(value)

    @TypeConverter
    fun fromGearType(value: GearType): String = value.name

    @TypeConverter
    fun toGearType(value: String): GearType = GearType.valueOf(value)

    @TypeConverter
    fun fromRarity(value: Rarity): String = value.name

    @TypeConverter
    fun toRarity(value: String): Rarity = Rarity.valueOf(value)
}
