package com.mindandmotion.app.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindandmotion.app.ui.components.AppTopBar
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun JournalCalendarScreen(
    viewModel: JournalViewModel,
    onOpenDay: (LocalDate) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(topBar = { AppTopBar(title = "Journal") }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            MonthHeader(
                month = state.visibleMonth,
                onPrevious = viewModel::onPreviousMonth,
                onNext = viewModel::onNextMonth
            )
            WeekdayLabels()
            CalendarGrid(
                month = state.visibleMonth,
                markedDates = state.markedDates,
                selectedDate = state.selectedDate,
                onDayClick = { date ->
                    viewModel.onDateSelected(date)
                    onOpenDay(date)
                }
            )
        }
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Luna precedentă")
        }
        val label = month.month.getDisplayName(TextStyle.FULL, Locale("ro"))
            .replaceFirstChar { it.uppercase() } + " ${month.year}"
        Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Luna următoare")
        }
    }
}

@Composable
private fun WeekdayLabels() {
    val labels = listOf("L", "Ma", "Mi", "J", "V", "S", "D")
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        labels.forEach { label ->
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    markedDates: Set<LocalDate>,
    selectedDate: LocalDate,
    onDayClick: (LocalDate) -> Unit
) {
    val firstOfMonth = month.atDay(1)
    // dayOfWeek.value: Luni=1 ... Duminică=7 -> offset ca săptămâna să înceapă lunea
    val leadingBlanks = firstOfMonth.dayOfWeek.value - 1
    val daysInMonth = month.lengthOfMonth()
    val cells: List<LocalDate?> = List(leadingBlanks) { null } + (1..daysInMonth).map { month.atDay(it) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.padding(8.dp)
    ) {
        items(cells) { date ->
            DayCell(
                date = date,
                isMarked = date != null && date in markedDates,
                isSelected = date != null && date == selectedDate,
                isToday = date != null && date == LocalDate.now(),
                onClick = { date?.let(onDayClick) }
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    isMarked: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    date == null -> Color.Transparent
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = date != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${date.dayOfMonth}",
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (isMarked && !isSelected) {
                    Box(
                        Modifier
                            .padding(top = 2.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                }
            }
        }
    }
}
