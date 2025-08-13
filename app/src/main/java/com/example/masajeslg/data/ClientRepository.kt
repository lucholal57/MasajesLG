package com.example.masajeslg.data

class ClientRepository(private val dao: ClientDao) {

    fun getAll() = dao.getAll()

    suspend fun add(name: String, phone: String?, notes: String?) {
        dao.insert(Client(name = name.trim(), phone = phone?.trim(), notes = notes?.trim()))
    }

    suspend fun update(id: Long, name: String, phone: String?, notes: String?) {
        val current = dao.getById(id) ?: return
        dao.update(current.copy(name = name.trim(), phone = phone?.trim(), notes = notes?.trim()))
    }

    suspend fun delete(id: Long) {
        dao.delete(id)
    }
}
