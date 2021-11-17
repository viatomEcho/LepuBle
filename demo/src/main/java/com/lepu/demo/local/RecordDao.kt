package com.lepu.demo.local

import androidx.room.*
import com.lepu.demo.data.entity.RecordEntity

/**
 * author: wujuan
 * description:
 */
@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecord(entity: RecordEntity):Long

    @Transaction
    @Query("SELECT * FROM records limit :pageSize offset :offset")
    fun getRecordEntityList(offset: Long, pageSize: Int): List<RecordEntity>?

    @Query("SELECT COUNT(id) from records")
    fun getCount(): Long


}