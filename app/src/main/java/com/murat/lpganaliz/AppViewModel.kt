package com.murat.lpganaliz

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.murat.lpganaliz.analiz.AylikOzet
import com.murat.lpganaliz.analiz.GenelOzet
import com.murat.lpganaliz.analiz.LpgAnaliz
import com.murat.lpganaliz.analiz.TuketimAraligi
import com.murat.lpganaliz.data.AppDatabase
import com.murat.lpganaliz.data.AracProfili
import com.murat.lpganaliz.data.YakitKaydi
import com.murat.lpganaliz.ocr.FisAyristirici
import com.murat.lpganaliz.ocr.FisOkuyucu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).dao()

    val kayitlar: StateFlow<List<YakitKaydi>> = dao.tumKayitlar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val arac: StateFlow<AracProfili?> = dao.aracAkisi()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ---- Yeni kayıt / fiş okuma ekranı durumu ----
    data class FisDurumu(
        val okunuyor: Boolean = false,
        val hata: String? = null,
        val hamMetin: String? = null,
        val litre: String = "",
        val tutar: String = "",
        val istasyon: String = "",
        val tarih: Long = System.currentTimeMillis(),
        val ocrUygulandi: Boolean = false
    )

    private val _fis = MutableStateFlow(FisDurumu())
    val fis: StateFlow<FisDurumu> = _fis

    fun fisLitre(v: String) { _fis.value = _fis.value.copy(litre = v) }
    fun fisTutar(v: String) { _fis.value = _fis.value.copy(tutar = v) }
    fun fisIstasyon(v: String) { _fis.value = _fis.value.copy(istasyon = v) }
    fun fisTarih(v: Long) { _fis.value = _fis.value.copy(tarih = v) }
    fun fisSifirla() {
        val yol = _fis.value.fisYolu
        yol?.let { java.io.File(it).delete() }
        _fis.value = FisDurumu()
    }

    /** Kameradan çekilen fotoğrafı kalıcı klasöre taşır ve OCR başlatır. */
    fun fisGorselAyarla(uri: Uri, context: android.content.Context) {
        viewModelScope.launch {
            val klasor = java.io.File(context.filesDir, "fisler").apply { mkdirs() }
            val hedef = java.io.File(klasor, "fis_${System.currentTimeMillis()}.jpg")
            val kopyalandi = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { giris ->
                        hedef.outputStream().use { cikis -> giris.copyTo(cikis) }
                    }
                    true
                }.getOrDefault(false)
            }
            if (!kopyalandi) {
                _fis.value = _fis.value.copy(hata = "Fotoğraf kaydedilemedi, tekrar deneyin.")
                return@launch
            }
            _fis.value = _fis.value.copy(fisYolu = hedef.absolutePath, hata = null)
            fisOku(Uri.fromFile(hedef))
        }
    }

    /** Kamera ile çekilen fiş fotoğrafını OCR'dan geçirir ve alanları doldurur. */
    fun fisOku(uri: Uri) {
        _fis.value = _fis.value.copy(okunuyor = true, hata = null)
        viewModelScope.launch {
            val metin = withContext(Dispatchers.IO) { FisOkuyucu.oku(getApplication(), uri).getOrNull() }
            if (metin.isNullOrBlank()) {
                _fis.value = _fis.value.copy(
                    okunuyor = false,
                    hata = "Fiş okunamadı. Fotoğrafı net ve fişin tamamı görünecek şekilde tekrar çekin veya değerleri elle girin."
                )
                return@launch
            }
            val s = FisAyristirici.ayristir(metin)
            _fis.value = _fis.value.copy(
                okunuyor = false,
                hata = null,
                hamMetin = metin,
                litre = s.litre?.let { bicimle(it) } ?: _fis.value.litre,
                tutar = s.tutar?.let { bicimle(it) } ?: _fis.value.tutar,
                istasyon = s.istasyon ?: _fis.value.istasyon,
                tarih = s.tarih ?: _fis.value.tarih,
                ocrUygulandi = true
            )
        }
    }

    private fun bicimle(d: Double): String = String.format(java.util.Locale("tr", "TR"), "%.2f", d)

    /** Yeni kaydı kaydeder. Basit doğrulama yapar. */
    fun kaydet(km: String, tamDepo: Boolean, fisYolu: String?, notlar: String): Boolean {
        val kmDegeri = km.trim().toIntOrNull()
        val litre = FisAyristirici.sayiOku(_fis.value.litre.replace(" ", ""))
        val tutar = FisAyristirici.sayiOku(_fis.value.tutar.replace(" ", "")) ?: 0.0
        if (kmDegeri == null || kmDegeri <= 0) {
            _fis.value = _fis.value.copy(hata = "Geçerli bir ana km değeri girin.")
            return false
        }
        if (litre == null || litre <= 0) {
            _fis.value = _fis.value.copy(hata = "Alınan litre miktarını girin (fişten okunamadıysa elle yazın).")
            return false
        }
        viewModelScope.launch {
            dao.ekle(
                YakitKaydi(
                    tarih = _fis.value.tarih,
                    km = kmDegeri,
                    litre = litre,
                    tutar = tutar,
                    istasyon = _fis.value.istasyon,
                    fisYolu = fisYolu,
                    ocrMetni = _fis.value.hamMetin,
                    tamDepo = tamDepo,
                    notlar = notlar
                )
            )
        }
        return true
    }

    fun kaydiSil(kayit: YakitKaydi) {
        viewModelScope.launch { dao.sil(kayit.id) }
    }

    fun kaydiGuncelle(kayit: YakitKaydi) {
        viewModelScope.launch { dao.guncelle(kayit) }
    }

    fun aracKaydet(profil: AracProfili) {
        viewModelScope.launch { dao.aracKaydet(profil) }
    }

    fun hepsiniSil() {
        viewModelScope.launch { dao.hepsiniSil() }
    }

    // ---- Analiz yardımcıları ----
    fun ozet(liste: List<YakitKaydi>): GenelOzet? = LpgAnaliz.genelOzet(liste)
    fun araliklar(liste: List<YakitKaydi>): List<TuketimAraligi> = LpgAnaliz.araliklar(liste)
    fun aylik(liste: List<YakitKaydi>): List<AylikOzet> = LpgAnaliz.aylikOzet(liste)
}
