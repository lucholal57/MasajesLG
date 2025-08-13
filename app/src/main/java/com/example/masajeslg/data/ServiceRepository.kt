package com.example.masajeslg.data

class ServiceRepository(private val dao: ServiceDao) {

    fun getActive() = dao.getActive()
    fun getAll() = dao.getAll() // <- opcional si querés mostrar todos

    suspend fun add(name: String, minutes: Int, price: Double) {
        dao.insert(Service(name = name, durationMinutes = minutes, price = price, active = true))
    }

    suspend fun update(id: Long, name: String, minutes: Int, price: Double) {
        val current = dao.getById(id) ?: return
        dao.update(current.copy(name = name, durationMinutes = minutes, price = price))
    }

    suspend fun setActive(id: Long, active: Boolean) {
        dao.setActive(id, active)
    }

    suspend fun delete(id: Long) {
        dao.delete(id)
    }
}
