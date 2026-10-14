package com.murat.lpganaliz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bir LPG alımını (fişi) temsil eder.
 *
 * @param tarih        Alım tarihi (epoch millis).
 * @param km           Alım anındaki aracın ana kilometre göstergesi.
 * @param litre        Alınan LPG miktarı (litre).
 * @param tutar        Ödenen tutar (TL).
 * @param istasyon     İstasyon / marka adı (opsiyonel).
 * @param fisYolu      Çekilen fiş fotoğrafının dosya yolu (opsiyonel).
 * @param ocrMetni     OCR ile okunan ham metin (opsiyonel, hata ayıklama için).
 * @param tamDepo      Deponun tamamen doldurulup doldurulmadığı (tüketim hesabı için).
 * @param notlar       Serbest not alanı.
 */
@Entity(tableName = "yakit_kayitlari")
data class YakitKaydi(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val tarih: Long,
    val km: Int,
    val litre: Double,
    val tutar: Double,
    val istasyon: String = "",
    val fisYolu: String? = null,
    val ocrMetni: String? = null,
    val tamDepo: Boolean = true,
    val notlar: String = ""
)

/**
 * Araç profilinden geçmişe dönük hesaplamada kullanılan "başlangıç kilometresi".
 * Örn: LPG takıldığı andaki km ya da ilk fişten önceki sayaç.
 */
@Entity(tableName = "arac")
data class AracProfili(
    @PrimaryKey val id: Int = 1,
    val marka: String = "Toyota",
    val model: String = "Auris",
    val yil: Int = 2011,
    val motorHacmi: String = "1.6",
    val beygir: Int = 132,
    val baslangicKm: Int = 0,
    /** LPG depo hacmi (litre). Menzil ve depo durumu tahminlerinde kullanılır. */
    val depoLitre: Double = 37.0
)
