package com.samueljuma.core.presentation.designsystem

import androidx.compose.ui.graphics.Color

// ============================================================================
// Notion-inspired neutral palette.
// Near-monochrome, content-first: warm paper whites + warm near-black ink in
// light, the #191919 family in dark, with a single quiet blue accent.
//
// Raw color literals live ONLY in this file. Everything in the app should
// consume semantic roles via MaterialTheme.colorScheme (see Theme.kt).
// ============================================================================

// --- Light neutrals (warm paper) ---
val PaperWhite = Color(0xFFFFFFFF)      // background / surface
val PaperWarm1 = Color(0xFFF7F6F3)      // surfaceContainerLow
val PaperWarm2 = Color(0xFFF1F0EE)      // surfaceContainer / surfaceVariant
val PaperWarm3 = Color(0xFFEBEAE8)      // surfaceContainerHigh
val PaperWarm4 = Color(0xFFE3E2DF)      // surfaceContainerHighest
val InkWarm = Color(0xFF37352F)         // primary text (warm near-black)
val InkMuted = Color(0xFF787774)        // secondary text
val OutlineLight = Color(0xFFD3D1CB)    // outline
val OutlineVariantLight = Color(0xFFEBEAE8)

// --- Dark neutrals ---
val Coal = Color(0xFF191919)            // background / surface
val Coal900 = Color(0xFF141414)         // surfaceContainerLowest
val Coal800 = Color(0xFF1F1F1F)         // surfaceContainerLow
val Coal700 = Color(0xFF252525)         // surfaceContainer
val Coal600 = Color(0xFF2F2F2F)         // surfaceContainerHigh / surfaceVariant
val Coal500 = Color(0xFF373737)         // surfaceContainerHighest
val Mist = Color(0xFFEBEBEB)            // primary text on dark
val MistMuted = Color(0xFF9B9B9B)       // secondary text on dark
val OutlineDark = Color(0xFF434343)     // outline
val OutlineVariantDark = Color(0xFF373737)

// --- Accent (the one quiet color) ---
val AccentBlue = Color(0xFF2383E2)      // primary (light)
val AccentBlueContainer = Color(0xFFD3E5F7)
val OnAccentBlueContainer = Color(0xFF0B4A8F)
val AccentBlueDark = Color(0xFF529CCA)  // primary (dark)
val AccentBlueContainerDark = Color(0xFF1F3A52)
val OnAccentBlueContainerDark = Color(0xFFBBD9F0)
val OnAccentDarkInk = Color(0xFF11243B) // onPrimary in dark

// --- Status / error ---
val ErrorLight = Color(0xFFEB5757)
val ErrorDark = Color(0xFFFF6B6B)
val OnErrorDark = Color(0xFF2A0E0E)

// --- Shared ---
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
