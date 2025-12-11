package com.francotte.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.francotte.billing.PremiumPlan
import com.francotte.billing.PremiumUiState
import com.francotte.billing.PremiumViewModel


const val PREMIUM_ROUTE = "premium_route"

fun NavController.navigateToPremiumScreen(navOptions: NavOptions? = null) {
    this.navigate(PREMIUM_ROUTE, navOptions)
}


fun NavGraphBuilder.premiumScreen() {
    composable(route = PREMIUM_ROUTE) {
        PremiumRoute()
    }
}


@Composable
fun PremiumRoute(
    viewModel: PremiumViewModel= hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as Activity

    LaunchedEffect(uiState.message) {
        // Ici tu peux déclencher un SnackBar avec uiState.message si non nul
    }

    PremiumScreen(
        uiState = uiState,
        onOfferClick = { plan -> viewModel.onOfferClicked(activity, plan) }
    )
}

@Composable
fun PremiumScreen(
    uiState: PremiumUiState,
    onOfferClick: (PremiumPlan) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00C27A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Premium",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "My Recipes Store",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Souscrire à l’offre premium My Recipes Store",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "S'abonner pour retirer la publicité de l’application.",
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    uiState.offers.forEach { offer ->
                        val isHighlighted = offer.plan == PremiumPlan.Yearly

                        OfferButton(
                            text = when (offer.plan) {
                                PremiumPlan.Monthly   -> offer.title
                                PremiumPlan.Quarterly -> offer.title
                                PremiumPlan.Yearly    -> offer.title
                            },
                            highlighted = isHighlighted,
                            onClick = { onOfferClick(offer.plan) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (uiState.isPremium) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Vous êtes Premium 🎉",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00C27A)
                        )
                    }
                }
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
    val background = if (highlighted) Color(0xFF00C27A) else Color.Transparent
    val contentColor = if (highlighted) Color.White else Color(0xFF00C27A)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
