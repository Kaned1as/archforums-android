package com.kanedias.holywarsoo.service

import android.content.Context
import androidx.room.Room
import com.kanedias.holywarsoo.database.LocalDatabase

/**
 * @author Kanedias
 *
 * Created on 2020-01-12
 */
object Database {

    private lateinit var database: LocalDatabase

    fun draftDao() = database.draftDao()

    fun init(ctx: Context) {
        database = Room.databaseBuilder(ctx, LocalDatabase::class.java, "local-database")
            .allowMainThreadQueries()
            .build()
    }
}