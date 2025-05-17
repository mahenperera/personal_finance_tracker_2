package com.example.mad_lab_exam_3

import androidx.room.*

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAll(): List<Transaction>

    @Insert
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    suspend fun getByCategory(category: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE type = :type")
    suspend fun getByType(type: String): List<Transaction>
}
