package com.murat.lpganaliz.ekranlar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import coil.compose.AsyncImage
import com.murat.lpganaliz.AppViewModel
import com.murat.lpganaliz.ui.ddMMMMyyyy
import com.murat.lpganaliz.ui.clickableSade
import com.murat.lpganaliz.ui.litre
import com.murat.lpganaliz.ui.sayi
import com.murat.lpganaliz.ui.tl
import java.io.File

private val trYerel = java.util.Locale("tr", "TR")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KayitEkrani(
    vm: AppViewModel,
    anaKm: String,
    geri: () -> Unit,
    kaydedildi: () -> Unit
) {
    val durum by vm.fis.collectAsState()
    var km by remember { mutableStateOf(anaKm) }
    var tamDepo by remember { mutableStateOf(true) }
    var notlar by remember { mutableStateOf("") }
    var kameraAcik by remember { mutableStateOf(false) }
    var buyukResim by remember { mutableStateOf(false) }

    val litreDeger = com.murat.lpganaliz.ocr.FisAyristirici.sayiOku(durum.litre.replace(" ", ""))
    val tutarDeger = com.murat.lpganaliz.ocr.FisAyristirici.sayiOku(durum.tutar.replace(" ", ""))
    val birim = if (litreDeger != null && litreDeger > 0 && tutarDeger != null) tutarDeger / litreDeger else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fişi Kaydet", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = geri) { Icon(Icons.Default.ArrowBack, contentDescription = "Geri") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("1. Ana km", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = km,
                        onValueChange = { km = it.filter { c -> c.isDigit() }.take(7) },
                        label = { Text("Araç göstergesi (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("2. Fiş fotoğrafı", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (durum.fisYolu == null) {
                        Text(
                            "Fişi kamerayla çekin; litre, tutar, tarih ve istasyon otomatik okunur.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { kameraAcik = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(Modifier.size(6.dp))
                            Text("Fişi Çek ve Oku")
                        }
                        Text(
                            "Fotoğrafsız da kaydedebilirsiniz: değerleri elle yazın. Fotoğraf hiçbir yere gönderilmez, yalnızca bu telefonda saklanır.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        AsyncImage(
                            model = File(durum.fisYolu!!),
                            contentDescription = "Fiş fotoğrafı",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.4f)
                                .clickableSade { buyukResim = true }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { vm.fisOku(android.net.Uri.fromFile(File(durum.fisYolu!!))) },
                                modifier = Modifier.weight(1f)
                            ) { Text("Tekrar Oku") }
                            OutlinedButton(
                                onClick = { kameraAcik = true },
                                modifier = Modifier.weight(1f)
                            ) { Text("Yeniden Çek") }
                        }
                        OutlinedButton(
                            onClick = { vm.fisSifirla() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Fotoğrafı Kaldır") }
                    }

                    if (durum.okunuyor) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.size(10.dp))
                            Text("Fiş okunuyor…", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    durum.hata?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("3. Değerler", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = durum.litre,
                            onValueChange = { vm.fisLitre(it) },
                            label = { Text("Litre (L)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = durum.tutar,
                            onValueChange = { vm.fisTutar(it) },
                            label = { Text("Tutar (₺)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = durum.istasyon,
                        onValueChange = { vm.fisIstasyon(it) },
                        label = { Text("İstasyon (opsiyonel)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (birim != null) {
                        Text(
                            "Birim fiyat: ${"%.2f".format(trYerel, birim)} ₺/L",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        "Tarih: ${durum.tarih.ddMMMMyyyy()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = tamDepo, onCheckedChange = { tamDepo = it })
                        Spacer(Modifier.size(10.dp))
                        Column {
                            Text("Depoyu tam doldurdum", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Tüketim hesabı buna göre yapılır.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    OutlinedTextField(
                        value = notlar,
                        onValueChange = { notlar = it },
                        label = { Text("Not (opsiyonel)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Button(
                onClick = {
                    val basarili = vm.kaydet(km, tamDepo, durum.fisYolu, notlar)
                    if (basarili) kaydedildi()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Kaydet") }

            if (durum.hamMetin != null) {
                var acik by remember { mutableStateOf(false) }
                Card {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            if (acik) "Okunan metni gizle" else "Fişte okunan ham metni gör",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickableSade { acik = !acik }
                        )
                        if (acik) {
                            Spacer(Modifier.size(6.dp))
                            Text(
                                durum.hamMetin!!,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.size(20.dp))
        }
    }

    if (kameraAcik) {
        com.murat.lpganaliz.kamera.FisCekmeEkrani(
            onKapat = { kameraAcik = false },
            cekilenFis = { uri ->
                vm.fisGorselAyarla(uri, LocalContext.current)
                kameraAcik = false
            }
        )
    }

    if (buyukResim && durum.fisYolu != null) {
        Dialog(onDismissRequest = { buyukResim = false }) {
            AsyncImage(
                model = File(durum.fisYolu!!),
                contentDescription = "Fiş fotoğrafı",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
