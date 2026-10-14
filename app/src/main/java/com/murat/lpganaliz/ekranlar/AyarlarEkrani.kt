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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.murat.lpganaliz.AppViewModel
import com.murat.lpganaliz.data.AracProfili

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyarlarEkrani(vm: AppViewModel, geri: () -> Unit) {
    val arac by vm.arac.collectAsStateWithLifecycle()
    var marka by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var yil by remember { mutableStateOf("") }
    var motor by remember { mutableStateOf("") }
    var beygir by remember { mutableStateOf("") }
    var depoLitre by remember { mutableStateOf("") }
    var baslangicKm by remember { mutableStateOf("") }
    var silmeOnay by remember { mutableStateOf(false) }

    LaunchedEffect(arac) {
        arac?.let {
            marka = it.marka
            model = it.model
            yil = it.yil.toString()
            motor = it.motorHacmi
            beygir = it.beygir.toString()
            depoLitre = it.depoLitre.toString()
            baslangicKm = it.baslangicKm.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Araç & Ayarlar", fontWeight = FontWeight.Bold) },
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
                    Text("Araç bilgisi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(marka, { marka = it }, label = { Text("Marka") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(model, { model = it }, label = { Text("Model") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            yil, { yil = it.filter { c -> c.isDigit() }.take(4) },
                            label = { Text("Model yılı") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true, modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            motor, { motor = it },
                            label = { Text("Motor") },
                            singleLine = true, modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        beygir, { beygir = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("Beygir (HP)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        depoLitre, { depoLitre = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(5) },
                        label = { Text("LPG depo hacmi (litre)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Depo hacmi menzil ve depo durumu tahminlerinde kullanılır (sizinki: 37 litre).",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        baslangicKm, { baslangicKm = it.filter { c -> c.isDigit() }.take(7) },
                        label = { Text("Başlangıç km (LPG takıldığı andaki km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Başlangıç km, LPG takıldığı andaki sayacı ifade eder. Geçmişe dönük toplam yol hesabında kullanılır.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            vm.aracKaydet(
                                AracProfili(
                                    marka = marka.ifBlank { "Toyota" },
                                    model = model.ifBlank { "Auris" },
                                    yil = yil.toIntOrNull() ?: 2011,
                                    motorHacmi = motor.ifBlank { "1.6" },
                                    beygir = beygir.toIntOrNull() ?: 132,
                                    baslangicKm = baslangicKm.toIntOrNull() ?: 0,
                                    depoLitre = com.murat.lpganaliz.ocr.FisAyristirici
                                        .sayiOku(depoLitre.replace(" ", ""))?.takeIf { it > 0 } ?: 37.0
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Kaydet") }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Veri", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Tüm fişler ve fotoğraflar yalnızca bu telefonda saklanır; hiçbir sunucuya gönderilmez. Uygulamayı kaldırırsanız veriler de silinir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { silmeOnay = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Tüm kayıtları sil") }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Hakkında", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("LPG Analiz 1.0", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Toyota Auris 2011 · 1.6 · 132 HP için hazırlanmış LPG tüketim ve maliyet analiz uygulaması. Fiş fotoğrafını cihaz üzerinde (offline) okur.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.size(20.dp))
        }
    }

    if (silmeOnay) {
        AlertDialog(
            onDismissRequest = { silmeOnay = false },
            title = { Text("Emin misiniz?") },
            text = { Text("Tüm fiş kayıtları silinecek. Bu işlem geri alınamaz.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.hepsiniSil()
                    silmeOnay = false
                }) { Text("Sil", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { silmeOnay = false }) { Text("Vazgeç") } }
        )
    }
}
