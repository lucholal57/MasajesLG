package com.example.masajeslg.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.masajeslg.data.AppDatabase
import com.example.masajeslg.data.DayStat
import com.example.masajeslg.data.ServiceStat
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class StatsViewModel(private val db: AppDatabase) : ViewModel() {

    private val _range = MutableStateFlow(last30Days())
    val range = _range.asStateFlow()

    val byDay: StateFlow<List<DayStat>> =
        range.flatMapLatest { (start, end) -> db.appointmentDao().statsByDay(start, end) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val byService: StateFlow<List<ServiceStat>> =
        range.flatMapLatest { (start, end) -> db.appointmentDao().statsByService(start, end) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val totalCount: StateFlow<Int> =
        byDay.map { it.sumOf { s -> s.count } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val totalRevenue: StateFlow<Double> =
        byDay.map { it.sumOf { s -> s.total } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    fun setRange(start: Long, end: Long) {
        viewModelScope.launch { _range.emit(start to end) }
    }

    companion object {
        private fun dayStartEnd(timeMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
            val cal = Calendar.getInstance().apply {
                timeInMillis = timeMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val end = cal.timeInMillis
            return start to end
        }

        // incluye hoy completo + 29 días anteriores = 30 días
        private fun last30Days(): Pair<Long, Long> {
            val (todayStart, todayEnd) = dayStartEnd()
            val cal = Calendar.getInstance().apply { timeInMillis = todayStart }
            cal.add(Calendar.DAY_OF_YEAR, -29)
            val start = cal.timeInMillis
            return start to todayEnd
        }
    }

}

class StatsViewModelFactory(private val ctx: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        StatsViewModel(AppDatabase.get(ctx)) as T
}
