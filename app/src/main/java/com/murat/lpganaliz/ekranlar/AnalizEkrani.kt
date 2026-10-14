package com.murat.lpganaliz.ekranlar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.murat.lpganaliz.AppViewModel
import com.murat.lpganaliz.analiz.LpgAnaliz
import com.murat.lpganaliz.data.YakitKaydi
import com.murat.lpganaliz.ui.CizgiGrafik
import com.murat.lpganaliz.ui.Nokta
import com.murat.lpganaliz.ui.OzetKarti
import com.murat.lpganaliz.ui.SutunGrafik
import com.murat.lpganaliz.ui.ayAdi
import com.murat.lpganaliz.ui.km
import com.murat.lpganaliz.ui.litre
import com.murat.lpganaliz.ui.sayi
import com.murat.lpganaliz.ui.tarihKisa
import com.murat.lpganaliz.ui.tl
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalizEkrani(
    vm: AppViewModel,
    kayitlar: List<YakitKaydi>,
    geri: () -> Unit
) {
    val tr = Locale("tr", "TR")
    val sirali = kayitlar.sortedBy { it.km }
    val ozet = LpgAnaliz.genelOzet(sirali)
    val araliklar = LpgAnaliz.araliklar(sirali)
    val aylik = LpgAnaliz.aylikOzet(sirali)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LPG Analizi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = geri) { Icon(Icons.Default.ArrowBack, contentDescription = "Geri") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { pad ->
        if (ozet == null || sirali.size < 2) {
            Column(
                modifier = Modifier.fillMaxSize().padding(pad).padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(40.dp))
                Spacer(Modifier.size(12.dp))
                Text("Analiz için en az 2 fiş gerekiyor.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.size(8.dp))
                Text(
                    "İlk fiş başlangıç noktasıdır. İkinci fişten sonra 100 km'de kaç litre LPG yaktığın, km başı maliyetin ve aylık grafiklerin otomatik oluşur.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // --- Büyük göstergeler ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("ORTALAMA TÜKETİM", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${ozet.ortalamaYuzKm.sayi(2)} L / 100 km",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${ozet.kmBasiMaliyet.sayi(2)} ₺ / km · son ${ozet.toplamMesafe.km()} için",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "En iyi ${ozet.enIyiYuzKm.sayi(2)} · En kötü ${ozet.enKotuYuzKm.sayi(2)} L/100km",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OzetKarti(
                    baslik = "Toplam LPG",
                    deger = "${ozet.toplamLitre.sayi(1)} L",
                    altYazi = "${ozet.kayitSayisi} fiş",
                    modifier = Modifier.weight(1f)
                )
                OzetKarti(
                    baslik = "Toplam harcama",
                    deger = ozet.toplamTutar.tl(0),
                    altYazi = "ort. ${ozet.ortalamaLitreFiyat.tl(2)}/L",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OzetKarti(
                    baslik = "Günlük yol",
                    deger = "%.0f km".format(tr, ozet.gundeOrtalamaKm),
                    modifier = Modifier.weight(1f)
                )
                OzetKarti(
                    baslik = "Bir depoyla",
                    deger = "≈ %.0f km".format(tr, ozet.tahminiMenzil),
                    altYazi = "ort. %.1f L/tam dolum".format(tr, ozet.ortalamaDepoLitre),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OzetKarti(
                    baslik = "En ucuz / pahalı",
                    deger = "${ozet.enUcuzLitre.sayi(2)} – ${ozet.enPahaliLitre.sayi(2)} ₺/L",
                    modifier = Modifier.weight(1f)
                )
                OzetKarti(
                    baslik = "Dönem",
                    deger = "${ozet.ilkTarih.tarihKisa()} – ${ozet.sonTarih.tarihKisa()}",
                    altYazi = "${ozet.ilkKm.km()} → ${ozet.sonKm.km()}",
                    modifier = Modifier.weight(1f)
                )
            }

            // --- Grafikler ---
            Card {
                Column(Modifier.padding(12.dp)) {
                    CizgiGrafik(
                        baslik = "Fiş bazında tüketim (L/100 km)",
                        noktalar = araliklar.map {
                            Nokta(
                                etiket = SimpleDateFormat("dd.MM.yy", tr).format(java.util.Date(it.tarih)),
                                deger = it.yuzKmTuketim
                            )
                        },
                        birim = "L/100km"
                    )
                }
            }

            Card {
                Column(Modifier.padding(12.dp)) {
                    CizgiGrafik(
                        baslik = "LPG litre fiyatı değişimi (₺/L)",
                        noktalar = sirali.filter { it.litre > 0 && it.tutar > 0 }.map {
                            Nokta(
                                etiket = SimpleDateFormat("dd.MM.yy", tr).format(java.util.Date(it.tarih)),
                                deger = it.tutar / it.litre
                            )
                        },
                        birim = "₺/L",
                        renk = Color(0xFFE07A1F)
                    )
                }
            }

            Card {
                Column(Modifier.padding(12.dp)) {
                    SutunGrafik(
                        baslik = "Aylık LPG tüketimi",
                        noktalar = aylik.map { Nokta("${it.ay.toString().padStart(2, '0')}/${it.yil}", it.litre) },
                        birim = "L"
                    )
                }
            }

            Card {
                Column(Modifier.padding(12.dp)) {
                    SutunGrafik(
                        baslik = "Aylık harcama",
                        noktalar = aylik.map { Nokta("${it.ay.toString().padStart(2, '0')}/${it.yil}", it.tutar) },
                        birim = "₺",
                        renk = Color(0xFF357ABD)
                    )
                }
            }

            // --- Aylık tablo ---
            Card {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Aylık döküm", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth()) {
                        TabloBaslik("Ay", 1.4f)
                        TabloBaslik("Litre", 1f)
                        TabloBaslik("Tutar", 1f)
                        TabloBaslik("Yol", 1f)
                        TabloBaslik("L/100", 1f)
                    }
                    aylik.forEach {
                        Row(Modifier.fillMaxWidth()) {
                            TabloHucre(it.ayAdiKisa(), 1.4f)
                            TabloHucre(it.litre.sayi(1), 1f)
                            TabloHucre(it.tutar.tl(0), 1f)
                            TabloHucre("${it.km}", 1f)
                            TabloHucre(if (it.km > 0) (it.litre * 100.0 / it.km).sayi(2) else "—", 1f)
                        }
                    }
                }
            }

            // --- Fiş listesi özeti ---
            Card {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Fiş bazında tüketim", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (araliklar.isEmpty()) {
                        Text("Yeterli veri yok.", style = MaterialTheme.typography.bodySmall)
                    }
                    araliklar.reversed().take(30).forEach { a ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(a.tarih.tarihKisa(), style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${a.baslangicKm.km()} → ${a.bitisKm.km()}  (${a.mesafe} km)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                Text(
                                    "${a.yuzKmTuketim.sayi(2)} L/100km",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${a.litre.litre(1)} · ${a.kmBasiMaliyet.sayi(2)} ₺/km",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Yöntem notu", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Tüketim, ardışık iki fiş arasındaki km farkı ile ikinci fişte alınan litre üzerinden hesaplanır. Depoyu her seferinde tam doldurursan sonuç en doğru hâlini alır; yarım dolumlarda değer sapabilir. Yakıt katkısı, benzinle kullanım veya klima gibi etkenler tüketimi değiştirir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.size(20.dp))
        }
    }
}

private fun com.murat.lpganaliz.analiz.AylikOzet.ayAdiKisa(): String =
    String.format(Locale("tr", "TR"), "%02d/%d", ay, yil)

@Composable
private fun androidx.compose.foundation.layout.RowScope.TabloBaslik(metin: String, agirlik: Float) {
    Text(
        metin,
        modifier = Modifier.weight(agirlik),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TabloHucre(metin: String, agirlik: Float) {
    Text(metin, modifier = Modifier.weight(agirlik), style = MaterialTheme.typography.bodySmall)
}
