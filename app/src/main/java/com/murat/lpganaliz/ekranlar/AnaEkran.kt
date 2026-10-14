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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.murat.lpganaliz.analiz.LpgAnaliz
import com.murat.lpganaliz.data.YakitKaydi
import com.murat.lpganaliz.ui.OzetKarti
import com.murat.lpganaliz.ui.km
import com.murat.lpganaliz.ui.litre
import com.murat.lpganaliz.ui.sayi
import kotlinx.coroutines.launch

private val TR_YEREL = java.util.Locale("tr", "TR")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaEkran(
    aracBasligi: String,
    kayitlar: List<YakitKaydi>,
    depoLitre: Double,
    fisEkraniAc: () -> Unit,
    analizAc: () -> Unit,
    gecmisAc: () -> Unit,
    ayarlarAc: () -> Unit
) {
    val kayitlarSayi = kayitlar.size
    val sonKm = kayitlar.maxByOrNull { it.km }?.km
    val ozet = remember(kayitlar, depoLitre) { LpgAnaliz.genelOzet(kayitlar, depoLitre) }
    val sonOrtalama = ozet?.ortalamaYuzKm?.takeIf { kayitlarSayi >= 2 }
    var kmMetni by remember { mutableStateOf("") }
    val kapsam = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    val girilenKm = kmMetni.toIntOrNull()

    // Son kayda göre depo tahmini (km girilmediyse son kayıt baz alınır).
    val sonDepoDurumu = remember(kayitlar, sonKm, depoLitre) {
        if (sonKm != null && kayitlar.isNotEmpty()) LpgAnaliz.depoDurumu(kayitlar, sonKm, depoLitre)
        else null
    }
    // Girilen km'ye göre canlı depo tahmini.
    val girilenDepoDurumu = girilenKm?.takeIf { it > 0 && kayitlar.isNotEmpty() }?.let {
        remember(kayitlar, it, depoLitre) { LpgAnaliz.depoDurumu(kayitlar, it, depoLitre) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("LPG Analiz", fontWeight = FontWeight.Bold)
                        Text(aracBasligi, style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = ayarlarAc) { Icon(Icons.Default.Settings, contentDescription = "Ayarlar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text(
                            "Pompa başındayım",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    OutlinedTextField(
                        value = kmMetni,
                        onValueChange = { kmMetni = it.filter { ch -> ch.isDigit() }.take(7) },
                        label = { Text("Ana km (araç göstergesi)") },
                        placeholder = { Text("örn. 184500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    when {
                        sonKm == null -> Text(
                            "İlk kaydınızı oluşturun; LPG takıldığı andaki km'yi de girdiyseniz başlangıç noktası o olur.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        girilenKm != null && girilenKm > sonKm -> Text(
                            "Son kayıttan bu yana ${(girilenKm - sonKm).km()} yol yapmışsınız.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        girilenKm != null -> Text(
                            "Girilen km son kayıttan büyük olmalı.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )

                        else -> Text(
                            "En son kayıt: ${sonKm.km()} · toplam $kayitlarSayi fiş",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                if (kmMetni.isBlank()) {
                                    kapsam.launch { snackbar.showSnackbar("Önce ana km değerini girin.") }
                                } else {
                                    fisEkraniAc()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(Modifier.size(6.dp))
                            Text("Fişi Çek")
                        }
                        OutlinedButton(onClick = fisEkraniAc) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.size(6.dp))
                            Text("Elle")
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OzetKarti(
                    baslik = "Kayıtlı fiş",
                    deger = kayitlarSayi.toString(),
                    altYazi = sonKm?.let { "son: ${it.km()}" },
                    modifier = Modifier.weight(1f)
                )
                OzetKarti(
                    baslik = "Ortalama tüketim",
                    deger = sonOrtalama?.let { "${it.sayi(2)} L/100km" } ?: "—",
                    altYazi = if (kayitlarSayi >= 2) "tüm kayıtlar" else "2. fişten sonra",
                    modifier = Modifier.weight(1f)
                )
            }

            // Canlı / son depo durumu kartı
            val gosterilen = girilenDepoDurumu ?: sonDepoDurumu
            if (gosterilen != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Depo durumu (tahmini)", style = MaterialTheme.typography.labelMedium)
                        if (gosterilen.guvenilir) {
                            Text(
                                "≈ ${gosterilen.kalanLitre.sayi(1)} · ≈ ${"%.0f".format(TR_YEREL, gosterilen.kalanMenzil)} km gider",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Depo ${depoLitre.sayi(0)} L · son dolumdan beri ${gosterilen.gidilenKm.km()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (gosterilen.kalanMenzil < 80) {
                                Text(
                                    "⚠ Depo bitmek üzere — yakın zamanda dolum yapın.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            Text(
                                "2. fişten sonra burada kalan LPG tahminin görünür (depo ${depoLitre.sayi(0)} L).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = analizAc, modifier = Modifier.weight(1f)) { Text("Analiz & Grafikler") }
                OutlinedButton(onClick = gecmisAc, modifier = Modifier.weight(1f)) { Text("Geçmiş Fişler") }
            }

            Card {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Nasıl çalışır?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "1) Pompada ana km göstergesini gir.\n" +
                            "2) Fişi kamerayla çek; litre, tutar, tarih ve istasyon otomatik okunur (cihaz üzerinde, internetsiz).\n" +
                            "3) Kaydet. İkinci fişten itibaren 100 km'de kaç litre yaktığın, km başı maliyetin, aylık tüketim ve harcama grafiklerin çıkar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tüketim, iki dolum arasındaki mesafeye göre hesaplanır. En doğru sonuç için depoyu her seferinde tam doldur (${depoLitre.sayi(0)} L).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.size(20.dp))
        }
    }
}
