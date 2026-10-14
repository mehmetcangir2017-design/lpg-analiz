package com.murat.lpganaliz

import com.murat.lpganaliz.data.YakitKaydi
import com.murat.lpganaliz.ocr.FisAyristirici
import com.murat.lpganaliz.analiz.LpgAnaliz
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FisAyristiriciTest {

    @Test
    fun trSayiOku() {
        assertEquals(1234.56, FisAyristirici.sayiOku("1.234,56")!!, 0.0001)
        assertEquals(55.4, FisAyristirici.sayiOku("55,40")!!, 0.0001)
        assertEquals(1234.56, FisAyristirici.sayiOku("1234.56")!!, 0.0001)
    }

    @Test
    fun tipikLpgFisi() {
        val fis = """
            ENESLER PETROL AKARYAKIT
            OTOGAZ (LPG)
            TARİH 13.10.2026 17:42
            FİŞ NO 0000123
            LPG (OTOGAZ)   25,34 LT x 21,90 TL/LT   554,95
            TOPKDV          50,45
            TOPLAM          554,95 TL
        """.trimIndent()

        val s = FisAyristirici.ayristir(fis)
        assertNotNull(s.litre)
        assertEquals(25.34, s.litre!!, 0.01)
        assertEquals(554.95, s.tutar!!, 0.01)
        assertEquals(21.90, s.birimFiyat!!, 0.01)
        assertNotNull(s.tarih)
    }

    @Test
    fun litreUzunYazim() {
        val fis = "Petrol Ofisi\nLITRE: 32,10\nTUTAR: 700,00 TL\n"
        val s = FisAyristirici.ayristir(fis)
        assertEquals(32.10, s.litre!!, 0.01)
    }
}

class LpgAnalizTest {

    private fun kayit(km: Int, litre: Double, tutar: Double, gun: Long) = YakitKaydi(
        tarih = 1_700_000_000_000L + gun * 86_400_000L,
        km = km,
        litre = litre,
        tutar = tutar
    )

    @Test
    fun yuzKmTuketimHesaplanir() {
        val kayitlar = listOf(
            kayit(100_000, 30.0, 600.0, 0),
            kayit(100_500, 40.0, 800.0, 10),
            kayit(101_000, 35.0, 700.0, 20)
        )
        val araliklar = LpgAnaliz.araliklar(kayitlar)
        assertEquals(2, araliklar.size)
        // 500 km'de 40 litre -> 8 L/100km
        assertEquals(8.0, araliklar[0].yuzKmTuketim, 0.001)
        // 500 km'de 35 litre -> 7 L/100km
        assertEquals(7.0, araliklar[1].yuzKmTuketim, 0.001)
    }

    @Test
    fun genelOzetDegerleri() {
        val kayitlar = listOf(
            kayit(100_000, 30.0, 600.0, 0),
            kayit(100_500, 40.0, 800.0, 10),
            kayit(101_000, 35.0, 700.0, 20)
        )
        val o = LpgAnaliz.genelOzet(kayitlar)!!
        assertEquals(3, o.kayitSayisi)
        assertEquals(1000, o.toplamMesafe)
        assertEquals(105.0, o.toplamLitre, 0.001)
        assertEquals(2100.0, o.toplamTutar, 0.001)
        assertEquals(7.5, o.ortalamaYuzKm, 0.001)
        assertEquals(20.0, o.ortalamaLitreFiyat, 0.001)
        assertEquals(2.1, o.kmBasiMaliyet, 0.001)
    }

    @Test
    fun mesafeVeTahminCalisir() {
        val kayitlar = listOf(kayit(100_000, 40.0, 800.0, 0))
        val t = LpgAnaliz.mesafeVeTahmin(kayitlar, 100_400)!!
        assertEquals(400, t.first)
        assertEquals(10.0, t.third, 0.001) // 40*100/400
    }

    @Test
    fun depoMenzili37LitreIle() {
        val kayitlar = listOf(
            kayit(100_000, 30.0, 600.0, 0),
            kayit(100_500, 40.0, 800.0, 10),
            kayit(101_000, 35.0, 700.0, 20)
        )
        // Ortalama tüketim 7.5 L/100km -> 37 L depo ile ~493 km
        val o = LpgAnaliz.genelOzet(kayitlar, 37.0)!!
        assertEquals(37.0 * 100.0 / 7.5, o.depoMenzili, 0.01)
    }

    @Test
    fun depoDurumuTahmini() {
        val kayitlar = listOf(
            kayit(100_000, 30.0, 600.0, 0),
            kayit(100_500, 40.0, 800.0, 10)
        )
        // Son dolum 100.500'te, 100.700'de bakılıyor: 200 km x 8 L/100km = 16 L yakılmış
        val d = LpgAnaliz.depoDurumu(kayitlar, 100_700, 37.0)!!
        assertEquals(200, d.gidilenKm)
        assertEquals(16.0, d.yakilanLitre, 0.01)
        assertEquals(21.0, d.kalanLitre, 0.01)
        assertEquals(21.0 * 100.0 / 8.0, d.kalanMenzil, 0.01)
        assertTrue(d.guvenilir)
    }

    @Test
    fun tekKayittaDepoDurumuGuvenilmez() {
        val d = LpgAnaliz.depoDurumu(listOf(kayit(100_000, 30.0, 600.0, 0)), 100_100, 37.0)!!
        assertTrue(!d.guvenilir)
    }
}
