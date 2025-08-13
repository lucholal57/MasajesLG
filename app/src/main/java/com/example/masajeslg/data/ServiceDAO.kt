package com.example.masajeslg.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE active = 1 ORDER BY name ASC")
    fun getActive(): Flow<List<Service>>

    @Query("SELECT * FROM services ORDER BY active DESC, name ASC")
    fun getAll(): Flow<List<Service>> // <- solo si usás 'getAll'

    @Query("SELECT * FROM services WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Service?

    @Insert suspend fun insert(s: Service)
    @Update suspend fun update(s: Service)

    @Query("UPDATE services SET active = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun delete(id: Long)

}
