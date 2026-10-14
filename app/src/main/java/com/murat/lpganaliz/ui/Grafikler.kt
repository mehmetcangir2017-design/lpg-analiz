package com.murat.lpganaliz.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Nokta(val etiket: String, val deger: Double)

/**
 * Basit, bagimliliksiz cizgi grafigi. Noktalara dokununca degeri gosterir.
 */
@Composable
fun CizgiGrafik(
    baslik: String,
    noktalar: List<Nokta>,
    modifier: Modifier = Modifier,
    birim: String = "",
    renk: Color = MaterialTheme.colorScheme.primary
) {
    if (noktalar.isEmpty()) {
        Text(
            "Gosterilecek veri yok",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(12.dp)
        )
        return
    }

    var secili by remember { mutableStateOf<Int?>(null) }
    val minD = noktalar.minOf { it.deger }
    val maxD = noktalar.maxOf { it.deger }
    val yayilim = (maxD - minD).takeIf { it > 0.0 } ?: (maxD * 0.2).coerceAtLeast(1.0)
    val alt = (minD - yayilim * 0.15).coerceAtLeast(0.0)
    val ust = maxD + yayilim * 0.15

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(baslik, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            val aciklama = secili?.let { "${noktalar[it].etiket}: ${noktalar[it].deger.sayi(2)} $birim" }
                ?: "Ort. ${noktalar.map { it.deger }.average().sayi(2)} $birim"
            Text(
                aciklama,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(top = 8.dp)
                .pointerInput(noktalar) {
                    detectTapGestures { offset ->
                        val genislik = size.width.toFloat()
                        val adim = if (noktalar.size > 1) genislik / (noktalar.size - 1) else genislik
                        val idx = if (noktalar.size > 1) (offset.x / adim).toInt().coerceIn(0, noktalar.size - 1) else 0
                        secili = if (secili == idx) null else idx
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                val w = size.width
                val h = size.height
                val aralik = (ust - alt).takeIf { it > 0 } ?: 1.0
                fun y(d: Double) = (h - ((d - alt) / aralik) * h).toFloat()

                for (i in 0..4) {
                    val yy = h * i / 4f
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.25f),
                        start = Offset(0f, yy),
                        end = Offset(w, yy),
                        strokeWidth = 1f
                    )
                }

                if (noktalar.size == 1) {
                    drawCircle(renk, radius = 8f, center = Offset(w / 2f, y(noktalar[0].deger)))
                } else {
                    val adim = w / (noktalar.size - 1)
                    val path = Path()
                    noktalar.forEachIndexed { i, n ->
                        val x = adim * i
                        val yy = y(n.deger)
                        if (i == 0) path.moveTo(x, yy) else path.lineTo(x, yy)
                    }
                    drawPath(path, color = renk, style = Stroke(width = 4f))
                    noktalar.forEachIndexed { i, n ->
                        drawCircle(
                            color = renk,
                            radius = if (secili == i) 9f else 5f,
                            center = Offset(adim * i, y(n.deger))
                        )
                    }
                }
            }
        }

        Text(
            text = "${noktalar.first().etiket}  ->  ${noktalar.last().etiket}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )
    }
}

/**
 * Basit sutun grafigi (aylik litre / tutar icin).
 */
@Composable
fun SutunGrafik(
    baslik: String,
    noktalar: List<Nokta>,
    modifier: Modifier = Modifier,
    birim: String = "",
    renk: Color = MaterialTheme.colorScheme.secondary
) {
    if (noktalar.isEmpty()) {
        Text(
            "Gosterilecek veri yok",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(12.dp)
        )
        return
    }
    val maxD = noktalar.maxOf { it.deger }.takeIf { it > 0 } ?: 1.0
    var secili by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(baslik, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            val aciklama = secili?.let { "${noktalar[it].etiket}: ${noktalar[it].deger.sayi(2)} $birim" }
                ?: "Toplam ${noktalar.sumOf { it.deger }.sayi(2)} $birim"
            Text(
                aciklama,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .padding(top = 8.dp)
                .pointerInput(noktalar) {
                    detectTapGestures { offset ->
                        val adim = size.width.toFloat() / noktalar.size
                        val idx = (offset.x / adim).toInt().coerceIn(0, noktalar.size - 1)
                        secili = if (secili == idx) null else idx
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(170.dp)) {
                val w = size.width
                val h = size.height
                val adim = w / noktalar.size
                val bosluk = adim * 0.22f
                noktalar.forEachIndexed { i, n ->
                    val oran = (n.deger / maxD).toFloat().coerceIn(0f, 1f)
                    val bh = h * oran
                    drawRoundRect(
                        color = if (secili == i) renk else renk.copy(alpha = 0.75f),
                        topLeft = Offset(adim * i + bosluk / 2, h - bh),
                        size = Size(adim - bosluk, bh),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }
        }
        Text(
            text = noktalar.joinToString("  ") { it.etiket.take(3) },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )
    }
}
