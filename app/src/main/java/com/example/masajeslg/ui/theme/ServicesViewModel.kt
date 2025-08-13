package com.example.masajeslg.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.masajeslg.data.AppDatabase
import com.example.masajeslg.data.ServiceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServicesViewModel(
    private val repo: ServiceRepository,
    private val db: AppDatabase
) : ViewModel() {

    val services = repo.getActive().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Snackbars
    private val _uiMsg = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val uiMsg = _uiMsg.asSharedFlow()

    fun add(name: String, minutes: Int, price: Double) = viewModelScope.launch {
        runCatching { repo.add(name, minutes, price) }
            .onSuccess { _uiMsg.tryEmit("Servicio creado") }
            .onFailure { _uiMsg.tryEmit("Error al crear: ${it.message ?: "desconocido"}") }
    }

    fun update(id: Long, name: String, minutes: Int, price: Double) = viewModelScope.launch {
        runCatching { repo.update(id, name, minutes, price) }
            .onSuccess { _uiMsg.tryEmit("Cambios guardados") }
            .onFailure { _uiMsg.tryEmit("Error al editar: ${it.message ?: "desconocido"}") }
    }

    fun delete(id: Long) = viewModelScope.launch {
        val inUse = db.appointmentDao().countByService(id) > 0
        if (inUse) {
            _uiMsg.tryEmit("No se puede eliminar: hay turnos que usan este servicio")
        } else {
            runCatching { repo.delete(id) }
                .onSuccess { _uiMsg.tryEmit("Servicio eliminado") }
                .onFailure { _uiMsg.tryEmit("Error al eliminar: ${it.message ?: "desconocido"}") }
        }
    }
}

class ServicesViewModelFactory(private val ctx: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.get(ctx)
        return ServicesViewModel(ServiceRepository(db.serviceDao()), db) as T
    }
}
