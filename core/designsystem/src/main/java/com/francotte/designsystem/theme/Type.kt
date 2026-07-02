package com.francotte.designsystem.theme

import androidx.compose.material3.Typography

// Material default type scale — reused for its tuned sizes / line-heights / letter-spacing.
private val default = Typography()

/**
 * App type system — a two-family editorial "cookbook" pairing:
 *  - **Playfair** (high-contrast display serif) for all titles: display / headline / title roles.
 *  - **Lora** (reading serif) for body and labels: body / label roles.
 *
 * Only the [androidx.compose.ui.text.font.FontFamily] is overridden; every other metric keeps
 * the Material default that was already validated visually on the detail screen.
 */
val Typography =
    Typography(
        // Titles → Playfair
        displayLarge = default.displayLarge.copy(fontFamily = Playfair),
        displayMedium = default.displayMedium.copy(fontFamily = Playfair),
        displaySmall = default.displaySmall.copy(fontFamily = Playfair),
        headlineLarge = default.headlineLarge.copy(fontFamily = Playfair),
        headlineMedium = default.headlineMedium.copy(fontFamily = Playfair),
        headlineSmall = default.headlineSmall.copy(fontFamily = Playfair),
        titleLarge = default.titleLarge.copy(fontFamily = Playfair),
        titleMedium = default.titleMedium.copy(fontFamily = Playfair),
        titleSmall = default.titleSmall.copy(fontFamily = Playfair),
        // Body & labels → Lora
        bodyLarge = default.bodyLarge.copy(fontFamily = Lora),
        bodyMedium = default.bodyMedium.copy(fontFamily = Lora),
        bodySmall = default.bodySmall.copy(fontFamily = Lora),
        labelLarge = default.labelLarge.copy(fontFamily = Lora),
        labelMedium = default.labelMedium.copy(fontFamily = Lora),
        labelSmall = default.labelSmall.copy(fontFamily = Lora),
    )
