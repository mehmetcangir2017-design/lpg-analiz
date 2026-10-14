package com.murat.lpganaliz.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.Composable
import kotlin.math.abs

private val tr = Locale("tr", "TR")

fun Double.tl(kurus: Int = 2): String = String.format(tr, "%,.${kurus}f ₺", this)
fun Double.litre(kurus: Int = 2): String = String.format(tr, "%,.${kurus}f L", this)
fun Double.sayi(kurus: Int = 2): String = String.format(tr, "%,.${kurus}f", this)
fun Double.litreFiyat(): String = String.format(tr, "%,.2f ₺/L", this)
fun Int.km(): String = String.format(tr, "%,d km", this)

fun Long.tarihKisa(): String = SimpleDateFormat("dd.MM.yyyy", tr).format(Date(this))
fun Long.tarihUzun(): String = SimpleDateFormat("dd.MM.yyyy HH:mm", tr).format(Date(this))
fun Long.ayAdi(): String = SimpleDateFormat("MMMM yyyy", tr).format(Date(this))
fun Long.ddMMMMyyyy(): String = SimpleDateFormat("dd MMMM yyyy HH:mm", tr).format(Date(this))

fun Double.yuzde(ondalik: Int = 1): String = String.format(tr, "%%%.${ondalik}f", this)

/** Aylık bir seride trend: son değerin ilk değere göre yüzde değişimi. */
fun trendYuzde(degerler: List<Double>): Double? {
    if (degerler.size < 2) return null
    val ilk = degerler.first()
    val son = degerler.last()
    if (ilk <= 0.0) return null
    return (son - ilk) / ilk * 100.0
}

fun yaklasikEsit(a: Double, b: Double, tolerans: Double = 0.01): Boolean = abs(a - b) < tolerans

@Composable
private fun _bos() {}
