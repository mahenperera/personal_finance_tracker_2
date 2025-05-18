package com.example.mad_lab_exam_3

import androidx.room.*

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<Transaction>

    @Insert
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE category = :category")
    suspend fun getByCategory(category: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE type = :type")
    suspend fun getByType(type: String): List<Transaction>

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
