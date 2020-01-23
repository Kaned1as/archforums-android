package com.kanedias.holywarsoo.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kanedias.holywarsoo.database.entities.OfflineDraft

/**
 * Descriptor for local sqlite database. This is later parsed by Room annotation processor
 *
 * @author Kanedias
 *
 * Created on 2020-01-12
 */
@Database(entities = [OfflineDraft::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class LocalDatabase: RoomDatabase() {
    abstract fun draftDao(): DraftDao
}