package com.example.outfitai.ui.trips

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import com.example.outfitai.ui.components.AppBottomBar
import com.example.outfitai.ui.components.BottomBarDest
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.outfitai.ui.theme.Spacing
import com.example.outfitai.ui.trips.steps.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private data class TripHeaderData(
    val title: String,
    val subtitle: String? = null,
    val isBigTitle: Boolean = false,
)

private fun headerForStep(state: TripPlannerUiState): TripHeaderData {
    return when (state.step) {
        TripStep.WHERE_TO        -> TripHeaderData("Where to?")
        TripStep.DATES           -> TripHeaderData("Trip dates")
        TripStep.BAG_TYPE        -> TripHeaderData("Pack your bag")
        TripStep.ACTIVITIES      -> TripHeaderData("Add activities")
        TripStep.ASSIGN_ACTIVITIES -> TripHeaderData("Plan your days")
        TripStep.LOADING         -> TripHeaderData("Planning your trip…")
        TripStep.REVIEW          -> {
            val plan = state.plan
            if (plan != null) {
                val fmt   = DateTimeFormatter.ofPattern("MMM d")
                val start = LocalDate.parse(plan.startDate).format(fmt)
                val end   = LocalDate.parse(plan.endDate).format(fmt)
                val nights = ChronoUnit.DAYS.between(
                    LocalDate.parse(plan.startDate), LocalDate.parse(plan.endDate)
                )
                TripHeaderData(
                    title      = "${plan.flag} ${plan.city}",
                    subtitle   = "$nights nights · $start – $end · ${plan.bagSize.replace('_', ' ')}",
                    isBigTitle = true,
                )
            } else {
                TripHeaderData("")
            }
        }
    }
}

private fun previousStep(step: TripStep): TripStep? = when (step) {
    TripStep.WHERE_TO, TripStep.LOADING -> null
    TripStep.DATES              -> TripStep.WHERE_TO
    TripStep.BAG_TYPE           -> TripStep.DATES
    TripStep.ACTIVITIES         -> TripStep.BAG_TYPE
    TripStep.ASSIGN_ACTIVITIES  -> TripStep.ACTIVITIES
    TripStep.REVIEW             -> TripStep.ASSIGN_ACTIVITIES
}

@Composable
fun TripPlannerRoute(
    onClose: () -> Unit,
    onSaved: (collectionId: Int) -> Unit,
    onStudioClick: () -> Unit = {},
    onWardrobeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    vm: TripPlannerViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.savedCollectionId) {
        state.savedCollectionId?.let { onSaved(it) }
    }

    TripPlannerScreen(
        state                    = state,
        onClose                  = onClose,
        onStudioClick            = onStudioClick,
        onWardrobeClick          = onWardrobeClick,
        onProfileClick           = onProfileClick,
        onAddClick               = onAddClick,
        onDestinationSelected    = vm::selectDestination,
        onContinueFromWhere      = { vm.goToStep(TripStep.DATES) },
        onStartDate              = vm::setStartDate,
        onEndDate                = vm::setEndDate,
        onContinueFromDates      = { vm.goToStep(TripStep.BAG_TYPE) },
        onBagSize                = vm::setBagSize,
        onContinueFromBag        = { vm.goToStep(TripStep.ACTIVITIES) },
        onToggleActivity         = vm::toggleActivity,
        onContinueFromActivities = { vm.goToStep(TripStep.ASSIGN_ACTIVITIES) },
        onToggleActivityForDay   = vm::toggleActivityForDay,
        onQuickFill              = vm::quickFillDay,
        onGenerate               = vm::generateTrip,
        onSave                   = vm::saveTrip,
        onBack                   = { vm.goToStep(it) },
        onClearError             = vm::clearError,
    )
}

@Composable
private fun TripPlannerScreen(
    state: TripPlannerUiState,
    onClose: () -> Unit,
    onStudioClick: () -> Unit,
    onWardrobeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddClick: () -> Unit,
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
    onQuickFill: (java.time.LocalDate) -> Unit,
    onGenerate: () -> Unit,
    onSave: (String) -> Unit,
    onBack: (TripStep) -> Unit,
    onClearError: () -> Unit,
) {
    val headerData = headerForStep(state)

    Scaffold(
        topBar = {
            TripTopBar(
                step       = state.step,
                headerData = headerData,
                onClose    = onClose,
                onBack     = onBack,
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(bottom = Spacing.xxl)) {
                AppBottomBar(
                    active      = BottomBarDest.TRIP,
                    onTrip      = {},
                    onStudio    = onStudioClick,
                    onAdd       = onAddClick,
                    onWardrobe  = onWardrobeClick,
                    onProfile   = onProfileClick,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (state.step) {
                TripStep.WHERE_TO -> WhereToStep(
                    destinations = state.destinations,
                    loading      = state.destinationsLoading,
                    selected     = state.selectedDestination,
                    onSelect     = onDestinationSelected,
                    onContinue   = onContinueFromWhere,
                )
                TripStep.DATES -> DatesStep(
                    startDate  = state.startDate,
                    endDate    = state.endDate,
                    onStartDate = onStartDate,
                    onEndDate   = onEndDate,
                    onContinue  = onContinueFromDates,
                )
                TripStep.BAG_TYPE -> BagTypeStep(
                    selected    = state.bagSize,
                    destination = state.selectedDestination,
                    startDate   = state.startDate,
                    endDate     = state.endDate,
                    onSelect    = onBagSize,
                    onContinue  = onContinueFromBag,
                )
                TripStep.ACTIVITIES -> ActivitiesStep(
                    selected   = state.selectedActivities,
                    onToggle   = onToggleActivity,
                    onContinue = onContinueFromActivities,
                )
                TripStep.ASSIGN_ACTIVITIES -> AssignActivitiesStep(
                    startDate          = state.startDate,
                    endDate            = state.endDate,
                    selectedActivities = state.selectedActivities,
                    dayActivities      = state.dayActivities,
                    onToggleActivityForDay = onToggleActivityForDay,
                    onQuickFill        = onQuickFill,
                    onGenerate         = onGenerate,
                )
                TripStep.LOADING -> LoadingStep(
                    destination = state.selectedDestination?.city ?: "",
                )
                TripStep.REVIEW -> ReviewStep(
                    plan         = state.plan,
                    isSaving     = state.isSaving,
                    error        = state.error,
                    onSave       = onSave,
                    onClearError = onClearError,
                )
            }
        }
    }
}

@Composable
private fun TripTopBar(
    step: TripStep,
    headerData: TripHeaderData,
    onClose: () -> Unit,
    onBack: (TripStep) -> Unit,
) {
    val totalSteps  = 5
    val currentStep = when (step) {
        TripStep.WHERE_TO          -> 1
        TripStep.DATES             -> 2
        TripStep.BAG_TYPE          -> 3
        TripStep.ACTIVITIES        -> 4
        TripStep.ASSIGN_ACTIVITIES -> 5
        TripStep.LOADING, TripStep.REVIEW -> 5
    }
    val prevStep = previousStep(step)

    Column(modifier = Modifier.statusBarsPadding()) {
        if (step != TripStep.LOADING && step != TripStep.REVIEW) {
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color      = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (prevStep != null) {
                IconButton(onClick = { onBack(prevStep) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (headerData.title.isNotEmpty()) {
                    Text(
                        text      = headerData.title,
                        style     = if (headerData.isBigTitle) MaterialTheme.typography.headlineLarge
                                    else MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center,
                    )
                }
                headerData.subtitle?.let { sub ->
                    Text(
                        text      = sub,
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}
