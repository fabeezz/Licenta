package com.example.outfitai.ui.trips

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.outfitai.ui.trips.steps.*

@Composable
fun TripPlannerRoute(
    onClose: () -> Unit,
    onSaved: (collectionId: Int) -> Unit,
    vm: TripPlannerViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.savedCollectionId) {
        state.savedCollectionId?.let { onSaved(it) }
    }

    TripPlannerScreen(
        state = state,
        onClose = onClose,
        onDestinationSelected = vm::selectDestination,
        onContinueFromWhere = { vm.goToStep(TripStep.DATES) },
        onStartDate = vm::setStartDate,
        onEndDate = vm::setEndDate,
        onContinueFromDates = { vm.goToStep(TripStep.BAG_TYPE) },
        onBagSize = vm::setBagSize,
        onContinueFromBag = { vm.goToStep(TripStep.ACTIVITIES) },
        onToggleActivity = vm::toggleActivity,
        onContinueFromActivities = { vm.goToStep(TripStep.ASSIGN_ACTIVITIES) },
        onToggleActivityForDay = vm::toggleActivityForDay,
        onGenerate = vm::generateTrip,
        onSave = vm::saveTrip,
        onBack = { vm.goToStep(it) },
        onClearError = vm::clearError,
    )
}

@Composable
private fun TripPlannerScreen(
    state: TripPlannerUiState,
    onClose: () -> Unit,
    onDestinationSelected: (com.example.outfitai.data.model.DestinationDto) -> Unit,
    onContinueFromWhere: () -> Unit,
    onStartDate: (java.time.LocalDate) -> Unit,
    onEndDate: (java.time.LocalDate) -> Unit,
    onContinueFromDates: () -> Unit,
    onBagSize: (BagSize) -> Unit,
    onContinueFromBag: () -> Unit,
    onToggleActivity: (String) -> Unit,
    onContinueFromActivities: () -> Unit,
    onToggleActivityForDay: (java.time.LocalDate, String) -> Unit,
    onGenerate: () -> Unit,
    onSave: (String) -> Unit,
    onBack: (TripStep) -> Unit,
    onClearError: () -> Unit,
) {
    Scaffold(
        topBar = {
            TripTopBar(
                step = state.step,
                onClose = onClose,
                onBack = onBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (state.step) {
                TripStep.WHERE_TO -> WhereToStep(
                    destinations = state.destinations,
                    loading = state.destinationsLoading,
                    selected = state.selectedDestination,
                    onSelect = onDestinationSelected,
                    onContinue = onContinueFromWhere,
                )
                TripStep.DATES -> DatesStep(
                    startDate = state.startDate,
                    endDate = state.endDate,
                    onStartDate = onStartDate,
                    onEndDate = onEndDate,
                    onContinue = onContinueFromDates,
                )
                TripStep.BAG_TYPE -> BagTypeStep(
                    selected = state.bagSize,
                    destination = state.selectedDestination,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    onSelect = onBagSize,
                    onContinue = onContinueFromBag,
                )
                TripStep.ACTIVITIES -> ActivitiesStep(
                    selected = state.selectedActivities,
                    onToggle = onToggleActivity,
                    onContinue = onContinueFromActivities,
                )
                TripStep.ASSIGN_ACTIVITIES -> AssignActivitiesStep(
                    startDate = state.startDate,
                    endDate = state.endDate,
                    selectedActivities = state.selectedActivities,
                    dayActivities = state.dayActivities,
                    onToggleActivityForDay = onToggleActivityForDay,
                    onGenerate = onGenerate,
                )
                TripStep.LOADING -> LoadingStep(
                    destination = state.selectedDestination?.city ?: "",
                )
                TripStep.REVIEW -> ReviewStep(
                    plan = state.plan,
                    isSaving = state.isSaving,
                    error = state.error,
                    onSave = onSave,
                    onClearError = onClearError,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripTopBar(
    step: TripStep,
    onClose: () -> Unit,
    onBack: (TripStep) -> Unit,
) {
    val totalSteps = 5
    val currentStep = when (step) {
        TripStep.WHERE_TO -> 1
        TripStep.DATES -> 2
        TripStep.BAG_TYPE -> 3
        TripStep.ACTIVITIES -> 4
        TripStep.ASSIGN_ACTIVITIES -> 5
        TripStep.LOADING, TripStep.REVIEW -> 5
    }

    Column {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
        )
        if (step != TripStep.LOADING && step != TripStep.REVIEW) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(totalSteps) { i ->
                    val filled = i < currentStep
                    LinearProgressIndicator(
                        progress = { if (filled) 1f else 0f },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}
