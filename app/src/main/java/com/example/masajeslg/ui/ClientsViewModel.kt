package com.example.masajeslg.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.masajeslg.data.AppDatabase
import com.example.masajeslg.data.ClientRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClientsViewModel(
    private val repo: ClientRepository,
    private val db: AppDatabase
) : ViewModel() {

    // lista
    val clients = repo.getAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // mensajes UI (snackbar)
    private val _uiMsg = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val uiMsg = _uiMsg.asSharedFlow()

    fun add(name: String, phone: String?, notes: String?) = viewModelScope.launch {
        runCatching { repo.add(name, phone, notes) }
            .onSuccess { _uiMsg.tryEmit("Cliente creado") }
            .onFailure { _uiMsg.tryEmit("Error al crear: ${it.message ?: "desconocido"}") }
    }

    fun update(id: Long, name: String, phone: String?, notes: String?) = viewModelScope.launch {
        runCatching { repo.update(id, name, phone, notes) }
            .onSuccess { _uiMsg.tryEmit("Cambios guardados") }
            .onFailure { _uiMsg.tryEmit("Error al editar: ${it.message ?: "desconocido"}") }
    }

    fun delete(id: Long) = viewModelScope.launch {
        val inUse = db.appointmentDao().countByClient(id) > 0
        if (inUse) {
            _uiMsg.tryEmit("No se puede eliminar: el cliente tiene turnos")
        } else {
            runCatching { repo.delete(id) }
                .onSuccess { _uiMsg.tryEmit("Cliente eliminado") }
                .onFailure { _uiMsg.tryEmit("Error al eliminar: ${it.message ?: "desconocido"}") }
        }
    }
}

class ClientsViewModelFactory(private val ctx: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.get(ctx)
        return ClientsViewModel(ClientRepository(db.clientDao()), db) as T
    }
}
