package com.kanedias.holywarsoo.database

import androidx.room.*
import com.kanedias.holywarsoo.database.entities.OfflineDraft

/**
 * @author Kanedias
 *
 * Created on 2020-01-12
 */
@Dao
interface DraftDao {

    @Query("SELECT * FROM offline_draft")
    fun getAll(): List<OfflineDraft>

    @Query("SELECT od.* FROM offline_draft od WHERE od.ctx_key = :ctxKey")
    fun getByKey(ctxKey: String): OfflineDraft?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDraft(draft: OfflineDraft)

    @Update
    fun updateDraft(draft: OfflineDraft)

    @Delete
    fun deleteDraft(draft: OfflineDraft)

    @Query("DELETE FROM offline_draft WHERE ctx_key = :ctxKey")
    fun deleteByKey(ctxKey: String)

}