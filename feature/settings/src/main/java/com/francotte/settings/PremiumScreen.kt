package com.francotte.settings

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.francotte.designsystem.component.TopAppBar
import com.francotte.ui.LocalBillingController


const val PREMIUM_ROUTE = "premium_route"

fun NavController.navigateToPremiumScreen(navOptions: NavOptions? = null) {
    this.navigate(PREMIUM_ROUTE, navOptions)
}


fun NavGraphBuilder.premiumScreen(onBack: () -> Unit) {
    composable(route = PREMIUM_ROUTE) {
        PremiumRoute(onBack = onBack)
    }
}


@Composable
fun PremiumRoute(
    viewModel: PremiumViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val billingController = LocalBillingController.current

    val currentActivity by rememberUpdatedState(LocalActivity.current)

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PremiumEffect.LaunchPurchase -> {
                    val safeActivity = currentActivity
                    if (safeActivity == null) {
                        return@collect
                    }
                    billingController.launchPurchase(
                        activity = safeActivity,
                        offerToken = effect.offerToken
                    )
                }
            }
        }
    }

    LaunchedEffect(uiState.message) {
        // snackbar si besoin
    }

    PremiumScreen(
        uiState = uiState,
        onOfferClick = { plan -> viewModel.onOfferClicked(plan) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    uiState: PremiumUiState,
    onOfferClick: (PremiumPlan) -> Unit,
    onBack: () -> Unit
) {
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = "Premium",
                navigationIconEnabled = true,
                onNavigationClick = onBack,
                scrollBehavior = topAppBarScrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 16.dp
                ), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MyRecipesStore",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Subscribe to MyRecipesStore Premium offer",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Subscribe (automatic renewal) \n to remove ads from the app.",
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            uiState.offers.forEach { offer ->
                val isHighlighted = offer.plan == PremiumPlan.Yearly

                OfferButton(
                    text = when (offer.plan) {
                        PremiumPlan.Monthly -> offer.title
                        PremiumPlan.Quarterly -> offer.title
                        PremiumPlan.Yearly -> offer.title
                    },
                    highlighted = isHighlighted,
                    onClick = { onOfferClick(offer.plan) }
                )

                Spacer(modifier = Modifier.height(18.dp))
            }

            if (uiState.isPremium) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Vous êtes Premium 🎉",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


@Composable
private fun OfferButton(
    text: String,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    val background = if (highlighted) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor = if (highlighted) Color.White else MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .border(
                BorderStroke(
                    1.dp,
                    if (!highlighted) MaterialTheme.colorScheme.primary else Color.Transparent
                ), shape
            )
            .clickable(onClick = onClick),
        shape = shape,
        color = background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
