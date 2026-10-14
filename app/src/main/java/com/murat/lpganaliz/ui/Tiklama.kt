package com.murat.lpganaliz.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/** Ripple'sız basit tıklama. */
fun Modifier.clickableSade(blok: () -> Unit): Modifier = composed {
    val kaynak = remember { MutableInteractionSource() }
    clickable(interactionSource = kaynak, indication = null, onClick = blok)
}

@Composable
private fun bos() {}
