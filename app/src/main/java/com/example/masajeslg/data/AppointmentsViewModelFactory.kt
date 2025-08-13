package com.example.masajeslg.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.masajeslg.data.AppDatabase
import com.example.masajeslg.data.AppointmentRepository

class AppointmentsViewModelFactory(private val ctx: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.get(ctx)
        val repo = AppointmentRepository(
            appointmentDao = db.appointmentDao(),
            serviceDao = db.serviceDao(),
            clientDao = db.clientDao()
        )
        return AppointmentsViewModel(repo, db) as T
    }
}
