package com.example.outfitai.ui.outfits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.outfitai.Config
import com.example.outfitai.domain.weather.HourPoint
import com.example.outfitai.domain.weather.WeatherForecast
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private fun wmoLabel(code: Int): String = when (code) {
    0          -> "Clear"
    1, 2, 3    -> "Partly Cloudy"
    45, 48     -> "Foggy"
    51, 53, 55 -> "Drizzle"
    61, 63, 65 -> "Rain"
    71, 73, 75 -> "Snow"
    80, 81, 82 -> "Showers"
    95, 96, 99 -> "Thunderstorm"
    else       -> "Cloudy"
}

private fun wmoIcon(code: Int): ImageVector = when (code) {
    0            -> Icons.Outlined.WbSunny
    1, 2, 3      -> Icons.Outlined.Cloud
    45, 48       -> Icons.Outlined.Cloud
    51, 53, 55,
    61, 63, 65,
    80, 81, 82   -> Icons.Outlined.Grain
    71, 73, 75   -> Icons.Outlined.AcUnit
    95, 96, 99   -> Icons.Outlined.Thunderstorm
    else         -> Icons.Outlined.Cloud
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherForecastSheet(
    isLoading: Boolean,
    forecast: WeatherForecast?,
    error: String?,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onRetry: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(width = 36.dp, height = 4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = colors.outlineVariant,
                ) {}
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            // Header
            Text(
                text = Config.DEFAULT_LOCATION_LABEL,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.outline,
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Couldn't load weather",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurface,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onRetry) {
                                Text("Retry")
                            }
                        }
                    }
                }

                forecast != null -> {
                    ForecastContent(
                        forecast = forecast,
                        onDismiss = onDismiss,
                        onApply = onApply,
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastContent(
    forecast: WeatherForecast,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    // Current conditions hero
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = wmoIcon(forecast.currentCode),
            contentDescription = wmoLabel(forecast.currentCode),
            modifier = Modifier.size(56.dp),
            tint = colors.onSurface,
        )
        Column {
            Text(
                text = "${forecast.currentTempC.toInt()}°C",
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
                color = colors.onSurface,
                lineHeight = 52.sp,
            )
            Text(
                text = wmoLabel(forecast.currentCode),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.outline,
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Hourly strip — every 3 hours, up to 8 entries
    val hourly = forecast.hours
        .filterIndexed { i, _ -> i % 3 == 0 }
        .take(8)

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        items(hourly) { hour ->
            HourCard(hour)
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Suggested filter caption
    val climate = forecast.toClimate()
    Text(
        text = "Suggested filter: $climate",
        style = MaterialTheme.typography.bodyMedium,
        color = colors.outline,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Action buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onDismiss) {
            Text("Close")
        }
        Button(
            onClick = onApply,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
            ),
        ) {
            Text("Apply to filters")
        }
    }
}

@Composable
private fun HourCard(hour: HourPoint) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .width(64.dp)
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = hour.displayTime,
                style = MaterialTheme.typography.labelSmall,
                color = colors.outline,
            )
            Icon(
                imageVector = wmoIcon(hour.code),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = colors.onSurface,
            )
            Text(
                text = "${hour.tempC.toInt()}°",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
            )
        }
    }
}
