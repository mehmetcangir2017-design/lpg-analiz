package com.murat.lpganaliz.ocr

import java.util.Locale

/**
 * Akaryakıt/LPG fişlerinden tutar, litre ve tarihi çıkarmaya çalışır.
 *
 * Türkiye'deki fişlerde tipik olarak şu satırlar bulunur:
 *   "LPG (OTOGAZ)   25,34 LT  x  21,90 TL  554,95"
 *   "TOPKDV          50,45"
 *   "TOPLAM          554,95 TL"
 *   "TARİH 13.10.2026 17:42"
 *
 * Sayılar hem "1.234,56" (TR) hem "1234.56" (EN) biçiminde gelebilir.
 */
object FisAyristirici {

    data class Sonuc(
        val litre: Double? = null,
        val tutar: Double? = null,
        val birimFiyat: Double? = null,
        val tarih: Long? = null,
        val istasyon: String? = null
    )

    private val TARIH_REGEX = Regex("""(\d{1,2})[.\-/](\d{1,2})[.\-/](\d{2,4})""")
    private val SAAT_REGEX = Regex("""(\d{1,2}):(\d{2})""")

    // Örn: 25,34 LT   /  25.34 L  /  1.234,56 LİTRE
    private val LITRE_REGEX = Regex(
        """(\d{1,3}(?:[.\s]\d{3})*(?:,\d{1,3})|\d+(?:[.,]\d{1,3}))\s*(?:LT|LİTRE|LITRE|L\b)""",
        RegexOption.IGNORE_CASE
    )

    // Örn: 554,95 TL  /  1.234,56
    private val TUTAR_REGEX = Regex(
        """(\d{1,3}(?:[.\s]\d{3})*,\d{2})\s*(?:TL|TRY|₺)?""",
        RegexOption.IGNORE_CASE
    )

    // Örn: 21,90 TL/LT  veya  x 21,90
    private val BIRIM_REGEX = Regex(
        """(\d{1,3}(?:[.,]\d{1,3}))\s*(?:TL\s*\/\s*LT|TL\s*\/\s*L|₺\s*\/\s*L)""",
        RegexOption.IGNORE_CASE
    )

    fun ayristir(ham: String): Sonuc {
        val metin = ham.replace('\u00A0', ' ')
        val satirlar = metin.lines().map { it.trim() }.filter { it.isNotEmpty() }

        val litre = litreBul(metin, satirlar)
        val tutar = tutarBul(satirlar)
        val birim = birimBul(metin) ?: if (litre != null && tutar != null && litre > 0) tutar / litre else null
        val tarih = tarihBul(metin)

        return Sonuc(
            litre = litre,
            tutar = tutar,
            birimFiyat = birim,
            tarih = tarih,
            istasyon = istasyonBul(satirlar)
        )
    }

    /** "25,34 LT" gibi en net kalıbı arar; bulamazsa LPG/OTOGAZ satırına bakar. */
    private fun litreBul(metin: String, satirlar: List<String>): Double? {
        val adaylar = LITRE_REGEX.findAll(metin)
            .mapNotNull { sayiOku(it.groupValues[1]) }
            .filter { it in 1.0..200.0 }
            .toList()
        if (adaylar.isNotEmpty()) return adaylar.max()

        // LPG/OTOGAZ içeren satırdan ilk makul litre benzeri sayıyı al
        for (s in satirlar) {
            val u = s.uppercase(Locale("tr"))
            if (u.contains("LPG") || u.contains("OTOGAZ") || u.contains("AUTOGAS")) {
                val sayilar = TUTAR_REGEX.findAll(u).mapNotNull { sayiOku(it.groupValues[1]) }.toList()
                return sayilar.firstOrNull { it in 5.0..120.0 }
            }
        }
        return null
    }

    private fun tutarBul(satirlar: List<String>): Double? {
        // Öncelik: TOPLAM / TOPKDV / TUTAR / TOPLAM TUTAR satırları
        val anahtar = listOf("TOPLAM", "TOPKDV", "TUTAR", "ÖDENEN", "ODENEN", "GENEL")
        val adaylar = ArrayList<Double>()
        for (s in satirlar) {
            val u = s.uppercase(Locale("tr"))
            if (anahtar.any { u.contains(it) }) {
                val son = TUTAR_REGEX.findAll(u).mapNotNull { sayiOku(it.groupValues[1]) }.toList()
                adaylar.addAll(son.filter { it in 1.0..20000.0 })
            }
        }
        if (adaylar.isNotEmpty()) return adaylar.max()

        // TOPLAM yoksa fişteki en büyük para tutarı
        val tum = TUTAR_REGEX.findAll(satirlar.joinToString("\n"))
            .mapNotNull { sayiOku(it.groupValues[1]) }
            .filter { it in 20.0..20000.0 }
        return tum.maxOrNull()
    }

    private fun birimBul(metin: String): Double? = BIRIM_REGEX.find(metin)
        ?.let { sayiOku(it.groupValues[1]) }
        ?.takeIf { it in 1.0..100.0 }

    private fun tarihBul(metin: String): Long? {
        val m = TARIH_REGEX.find(metin) ?: return null
        val g = m.groupValues[1].toIntOrNull() ?: return null
        val a = m.groupValues[2].toIntOrNull() ?: return null
        var y = m.groupValues[3].toIntOrNull() ?: return null
        if (y < 100) y += 2000
        if (g !in 1..31 || a !in 1..12 || y !in 2000..2100) return null
        val cal = java.util.Calendar.getInstance()
        cal.set(y, a - 1, g, 12, 0, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        SAAT_REGEX.find(metin)?.let { s ->
            val hh = s.groupValues[1].toIntOrNull()
            val mm = s.groupValues[2].toIntOrNull()
            if (hh != null && mm != null && hh in 0..23 && mm in 0..59) {
                cal.set(java.util.Calendar.HOUR_OF_DAY, hh)
                cal.set(java.util.Calendar.MINUTE, mm)
            }
        }
        return cal.timeInMillis
    }

    private fun istasyonBul(satirlar: List<String>): String? {
        val anahtar = listOf("PETROL", "AKARYAKIT", "LPG", "ISTASYON", "İSTASYON", "OPET", "SHELL",
            "PETRO", "POAS", "AYTEMIZ", "AYTEMİZ", "TOTAL", "ALPET", "LUKOIL", "SOCAR", "KADOİL")
        return satirlar.take(6).firstOrNull { s ->
            val u = s.uppercase(Locale("tr"))
            u.length in 4..40 && anahtar.any { u.contains(it) }
        }
    }

    /**
     * TR ("1.234,56" → 1234.56) veya EN ("1234.56" → 1234.56) biçimini okur.
     */
    fun sayiOku(hamGirdi: String): Double? {
        var s = hamGirdi.trim().replace(" ", "").replace("\u00A0", "")
        if (s.isEmpty()) return null
        val sonVirgul = s.lastIndexOf(',')
        val sonNokta = s.lastIndexOf('.')
        s = when {
            sonVirgul > sonNokta -> s.replace(".", "").replace(',', '.')   // 1.234,56
            sonNokta > sonVirgul -> s.replace(",", "")                      // 1,234.56
            else -> s.replace(',', '.')
        }
        return s.toDoubleOrNull()
    }
}
