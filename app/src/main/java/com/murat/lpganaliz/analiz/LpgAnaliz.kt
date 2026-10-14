package com.murat.lpganaliz.analiz

import com.murat.lpganaliz.data.YakitKaydi

/**
 * İki alım arasındaki tüketim bilgisi.
 */
data class TuketimAraligi(
    val baslangicKm: Int,
    val bitisKm: Int,
    val mesafe: Int,
    val litre: Double,
    val tutar: Double,
    val tarih: Long,
    val yuzKmTuketim: Double,
    val kmBasiMaliyet: Double,
    val istasyon: String
)

data class AylikOzet(
    val yil: Int,
    val ay: Int,
    val litre: Double,
    val tutar: Double,
    val km: Int,
    val ortalamaFiyat: Double
)

data class GenelOzet(
    val kayitSayisi: Int,
    val toplamLitre: Double,
    val toplamTutar: Double,
    val toplamMesafe: Int,
    val ortalamaYuzKm: Double,
    val enIyiYuzKm: Double,
    val enKotuYuzKm: Double,
    val ortalamaLitreFiyat: Double,
    val kmBasiMaliyet: Double,
    val ilkKm: Int,
    val sonKm: Int,
    val ilkTarih: Long,
    val sonTarih: Long,
    val gundeOrtalamaKm: Double,
    /** Ortalama tam-dolum litresine göre menzil (eski davranış, geriye uyumluluk). */
    val tahminiMenzil: Double,
    val ortalamaDepoLitre: Double,
    /** Depo hacmine (örn. 37 L) göre tam depo menzili. */
    val depoMenzili: Double,
    val enUcuzLitre: Double,
    val enPahaliLitre: Double
)

/**
 * Depodaki tahmini LPG durumu.
 *
 * Mantık: son tam dolumdan bu yana gidilen km × ortalama tüketim = yakılan LPG.
 * Depo hacminden çıkarılınca kalan litre ve kalan menzil bulunur.
 */
data class DepoDurumu(
    val depoLitre: Double,
    val ortalamaYuzKm: Double,
    /** Son tam dolumun km'si (yoksa son kayıt). */
    val sonDolumKm: Int,
    /** Son dolumdan bu yana gidilen km. */
    val gidilenKm: Int,
    /** Tahmini yakılan litre. */
    val yakilanLitre: Double,
    /** Tahmini kalan litre (0'a kelepçelenir). */
    val kalanLitre: Double,
    /** Kalan litreyle gidilebilecek tahmini km. */
    val kalanMenzil: Double,
    /** Depo doluluk oranı 0..1. */
    val dolulukOrani: Double,
    /** Veri yetersizse (örn. tek kayıt, tüketim bilinmiyor) false. */
    val guvenilir: Boolean
)

/**
 * LPG tüketim analizlerini üretir.
 *
 * Tüketim mantığı: Ardışık iki alım arasındaki km farkı, ikinci alımda alınan
 * litre ile kapatılmıştır (tam dolum / "tank-to-tank" yöntemi).
 * Yani 500 km sonra 40 litre aldıysan 100 km'de 8 litre yakmışsın demektir.
 */
object LpgAnaliz {

    /** Kayıtları km'ye göre artan sırayla verir (ilk kayıt başlangıç kabul edilir). */
    fun normalize(kayitlar: List<YakitKaydi>): List<YakitKaydi> =
        kayitlar.filter { it.litre > 0 && it.km > 0 }.sortedBy { it.km }

    fun araliklar(kayitlar: List<YakitKaydi>): List<TuketimAraligi> {
        val s = normalize(kayitlar)
        if (s.size < 2) return emptyList()
        val sonuc = ArrayList<TuketimAraligi>(s.size - 1)
        for (i in 1 until s.size) {
            val onceki = s[i - 1]
            val simdi = s[i]
            val mesafe = simdi.km - onceki.km
            if (mesafe <= 0) continue
            // simdi'de alınan litre, onceki dolum ile simdi arasındaki mesafede yakılmıştır
            val tuketim = simdi.litre * 100.0 / mesafe
            sonuc.add(
                TuketimAraligi(
                    baslangicKm = onceki.km,
                    bitisKm = simdi.km,
                    mesafe = mesafe,
                    litre = simdi.litre,
                    tutar = simdi.tutar,
                    tarih = simdi.tarih,
                    yuzKmTuketim = tuketim,
                    kmBasiMaliyet = if (mesafe > 0) simdi.tutar / mesafe else 0.0,
                    istasyon = simdi.istasyon
                )
            )
        }
        return sonuc
    }

    fun genelOzet(kayitlar: List<YakitKaydi>, depoLitre: Double = 37.0): GenelOzet? {
        val s = normalize(kayitlar)
        if (s.isEmpty()) return null
        val aralik = araliklar(s)
        val toplamLitre = s.sumOf { it.litre }
        val toplamTutar = s.sumOf { it.tutar }
        val ilkKm = s.first().km
        val sonKm = s.last().km
        val toplamMesafe = (sonKm - ilkKm).coerceAtLeast(0)
        val tuketimler = aralik.map { it.yuzKmTuketim }
        val ortTuketim = if (tuketimler.isEmpty()) 0.0 else tuketimler.average()
        val ortBenzin = if (toplamLitre > 0) toplamTutar / toplamLitre else 0.0
        val gunSayisi = ((s.last().tarih - s.first().tarih) / 86_400_000.0).coerceAtLeast(1.0)
        val gundeKm = toplamMesafe / gunSayisi
        // Tahmini menzil: ortalama tüketime göre ortalama depo dolum litresiyle gidilebilecek km
        val ortDepo = s.filter { it.tamDepo }.map { it.litre }.ifEmpty { listOf(toplamLitre / s.size) }.average()
        val menzil = if (ortTuketim > 0) ortDepo * 100.0 / ortTuketim else 0.0
        val depo = depoLitre.takeIf { it > 0 } ?: 37.0
        val depoMenzili = if (ortTuketim > 0) depo * 100.0 / ortTuketim else 0.0
        val litreFiyatlari = s.filter { it.litre > 0 && it.tutar > 0 }.map { it.tutar / it.litre }

        return GenelOzet(
            kayitSayisi = s.size,
            toplamLitre = toplamLitre,
            toplamTutar = toplamTutar,
            toplamMesafe = toplamMesafe,
            ortalamaYuzKm = ortTuketim,
            enIyiYuzKm = tuketimler.minOrNull() ?: 0.0,
            enKotuYuzKm = tuketimler.maxOrNull() ?: 0.0,
            ortalamaLitreFiyat = ortBenzin,
            kmBasiMaliyet = if (toplamMesafe > 0) toplamTutar / toplamMesafe else 0.0,
            ilkKm = ilkKm,
            sonKm = sonKm,
            ilkTarih = s.first().tarih,
            sonTarih = s.last().tarih,
            gundeOrtalamaKm = gundeKm,
            tahminiMenzil = menzil,
            ortalamaDepoLitre = ortDepo,
            depoMenzili = depoMenzili,
            enUcuzLitre = litreFiyatlari.minOrNull() ?: 0.0,
            enPahaliLitre = litreFiyatlari.maxOrNull() ?: 0.0
        )
    }

    /** Aylık (yıl, ay) toplamları. km = o ay içindeki alım farkları. */
    fun aylikOzet(kayitlar: List<YakitKaydi>): List<AylikOzet> {
        val s = normalize(kayitlar)
        if (s.size < 2) return emptyList()
        val aralik = araliklar(s)
        val map = LinkedHashMap<Pair<Int, Int>, Triple<Double, Double, Int>>()
        for (a in aralik) {
            val c = java.util.Calendar.getInstance().apply { timeInMillis = a.tarih }
            val key = c.get(java.util.Calendar.YEAR) to c.get(java.util.Calendar.MONTH) + 1
            val mevcut = map[key] ?: Triple(0.0, 0.0, 0)
            map[key] = Triple(mevcut.first + a.litre, mevcut.second + a.tutar, mevcut.third + a.mesafe)
        }
        return map.entries.sortedWith(compareBy({ it.key.first }, { it.key.second })).map { (k, v) ->
            AylikOzet(
                yil = k.first,
                ay = k.second,
                litre = v.first,
                tutar = v.second,
                km = v.third,
                ortalamaFiyat = if (v.first > 0) v.second / v.first else 0.0
            )
        }
    }

    /**
     * km girildiğinde o an ile önceki kayıt arasında geçen mesafeyi ve
     * o mesafede yakılan LPG'yi kestirir (giriş ekranındaki canlı panel için).
     */
    fun mesafeVeTahmin(kayitlar: List<YakitKaydi>, km: Int): Triple<Int, Double, Double>? {
        val onceki = normalize(kayitlar).lastOrNull { it.km < km } ?: return null
        val mesafe = km - onceki.km
        if (mesafe <= 0) return null
        val tuketim = if (onceki.litre > 0) onceki.litre * 100.0 / mesafe else 0.0
        return Triple(mesafe, onceki.litre, tuketim)
    }

    /**
     * Mevcut km'ye göre depodaki tahmini LPG durumunu hesaplar.
     *
     * @param kayitlar  Tüm yakıt kayıtları.
     * @param simdikiKm Aracın güncel ana km göstergesi.
     * @param depoLitre Depo hacmi (litre).
     */
    fun depoDurumu(kayitlar: List<YakitKaydi>, simdikiKm: Int, depoLitre: Double = 37.0): DepoDurumu? {
        val s = normalize(kayitlar)
        if (s.isEmpty()) return null
        val depo = depoLitre.takeIf { it > 0 } ?: 37.0
        val ortTuketim = araliklar(s).map { it.yuzKmTuketim }.average().takeIf { it.isFinite() && it > 0 }
        val sonDolum = s.filter { it.tamDepo }.maxByOrNull { it.km } ?: s.maxBy { it.km }
        val gidilen = (simdikiKm - sonDolum.km).coerceAtLeast(0)
        if (ortTuketim == null) {
            return DepoDurumu(
                depoLitre = depo,
                ortalamaYuzKm = 0.0,
                sonDolumKm = sonDolum.km,
                gidilenKm = gidilen,
                yakilanLitre = 0.0,
                kalanLitre = depo,
                kalanMenzil = 0.0,
                dolulukOrani = 1.0,
                guvenilir = false
            )
        }
        val yakilan = gidilen * ortTuketim / 100.0
        val kalan = (depo - yakilan).coerceIn(0.0, depo)
        return DepoDurumu(
            depoLitre = depo,
            ortalamaYuzKm = ortTuketim,
            sonDolumKm = sonDolum.km,
            gidilenKm = gidilen,
            yakilanLitre = yakilan,
            kalanLitre = kalan,
            kalanMenzil = if (ortTuketim > 0) kalan * 100.0 / ortTuketim else 0.0,
            dolulukOrani = (kalan / depo).coerceIn(0.0, 1.0),
            guvenilir = true
        )
    }
}
