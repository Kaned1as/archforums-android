package com.kanedias.holywarsoo.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

/**
 * An entity representing an offline draft.
 * A draft contains just some text content and the date it was created, for easy sorting.
 *
 * @author Kanedias
 *
 * Created on 22.08.18
 */
@Entity(tableName = "offline_draft", indices = [Index("ctx_key", unique = true)])
data class OfflineDraft(
    /**
     * Inner id in the database, not used
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    /**
     * Date this draft was created
     */
    @ColumnInfo(name = "created_at", typeAffinity = ColumnInfo.INTEGER)
    val createdAt: Date,

    /**
     * Key to find this draft easily, must be unique
     */
    @ColumnInfo(name = "ctx_key")
    val ctxKey: String,

    /**
     * Title of this draft, if applicable
     */
    @ColumnInfo(name = "title")
    val title: String? = null,

    /**
     * Contents of this draft
     */
    @ColumnInfo(name = "content", typeAffinity = ColumnInfo.UNICODE)
    val content: String
)