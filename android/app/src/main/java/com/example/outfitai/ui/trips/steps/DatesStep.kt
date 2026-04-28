package com.example.outfitai.ui.trips.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DatesStep(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onStartDate: (LocalDate) -> Unit,
    onEndDate: (LocalDate) -> Unit,
    onContinue: () -> Unit,
) {
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }
    // true = selecting start, false = selecting end
    var selectingStart by remember { mutableStateOf(startDate == null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Trip dates",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                if (selectingStart) "Tap a day to set your start date"
                else "Now tap your end date",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            )

            // Start / End cards
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateCard(
                    label = "START",
                    date = startDate,
                    active = selectingStart,
                    modifier = Modifier.weight(1f),
                    onClick = { selectingStart = true },
                )
                DateCard(
                    label = "END",
                    date = endDate,
                    active = !selectingStart,
                    modifier = Modifier.weight(1f),
                    onClick = { if (startDate != null) selectingStart = false },
                )
            }

            Spacer(Modifier.height(24.dp))

            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = displayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Row {
                    IconButton(onClick = { displayMonth = displayMonth.minusMonths(1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                    }
                    IconButton(onClick = { displayMonth = displayMonth.plusMonths(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                    }
                }
            }

            // Weekday headers
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Calendar grid
            CalendarGrid(
                month = displayMonth,
                startDate = startDate,
                endDate = endDate,
                today = LocalDate.now(),
                onDayClick = { date ->
                    if (selectingStart) {
                        onStartDate(date)
                        selectingStart = false
                    } else {
                        if (startDate != null && date >= startDate) {
                            onEndDate(date)
                        } else {
                            onStartDate(date)
                        }
                    }
                },
            )

            Spacer(Modifier.height(100.dp))
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
        ) {
            Button(
                onClick = onContinue,
                enabled = startDate != null && endDate != null,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .navigationBarsPadding()
                    .height(56.dp),
            ) {
                Text("Save dates", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun DateCard(
    label: String,
    date: LocalDate?,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val fmt = DateTimeFormatter.ofPattern("MMM d")
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(
            width = if (active) 2.dp else 1.dp,
            color = if (active) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                tint = if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = date?.format(fmt) ?: "—",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                    color = if (active) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    startDate: LocalDate?,
    endDate: LocalDate?,
    today: LocalDate,
    onDayClick: (LocalDate) -> Unit,
) {
    val firstDay = month.atDay(1)
    val startDayOfWeek = firstDay.dayOfWeek.value % 7  // Sunday = 0
    val daysInMonth = month.lengthOfMonth()

    val cells = startDayOfWeek + daysInMonth
    val rows = (cells + 6) / 7

    for (row in 0 until rows) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (col in 0 until 7) {
                val cellIndex = row * 7 + col
                val dayNum = cellIndex - startDayOfWeek + 1
                if (dayNum < 1 || dayNum > daysInMonth) {
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    val date = month.atDay(dayNum)
                    val isPast = date < today
                    val isStart = date == startDate
                    val isEnd = date == endDate
                    val isInRange = startDate != null && endDate != null &&
                        date > startDate && date < endDate

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp)
                            .aspectRatio(1f),
                    ) {
                        // Range highlight background
                        if (isInRange) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            )
                        }
                        // Day circle
                        val bgColor = when {
                            isStart || isEnd -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.background.copy(alpha = 0f)
                        }
                        val textColor = when {
                            isStart || isEnd -> MaterialTheme.colorScheme.onPrimary
                            isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.onBackground
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .clickable(enabled = !isPast) { onDayClick(date) },
                        ) {
                            Text(
                                text = dayNum.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor,
                                fontWeight = if (isStart || isEnd) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }
    }
}
