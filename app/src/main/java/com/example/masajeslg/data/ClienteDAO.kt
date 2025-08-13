package com.example.masajeslg.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAll(): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Client?

    @Insert
    suspend fun insert(c: Client): Long   // <-- antes no devolvía nada

    @Update
    suspend fun update(c: Client)

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun delete(id: Long)
}
