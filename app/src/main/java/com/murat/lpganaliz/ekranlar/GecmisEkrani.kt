package com.murat.lpganaliz.ekranlar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.murat.lpganaliz.AppViewModel
import com.murat.lpganaliz.data.YakitKaydi
import com.murat.lpganaliz.ui.clickableSade
import com.murat.lpganaliz.ui.km
import com.murat.lpganaliz.ui.litre
import com.murat.lpganaliz.ui.sayi
import com.murat.lpganaliz.ui.tarihKisa
import com.murat.lpganaliz.ui.tl
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GecmisEkrani(vm: AppViewModel, geri: () -> Unit) {
    val kayitlar by vm.kayitlar.collectAsStateWithLifecycle()
    var buyukResim by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Geçmiş Fişler", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = geri) { Icon(Icons.Default.ArrowBack, contentDescription = "Geri") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { pad ->
        if (kayitlar.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.size(12.dp))
                    Text("Henüz fiş kaydı yok.", style = MaterialTheme.typography.titleMedium)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(kayitlar, key = { it.id }) { kayit ->
                FisKarti(
                    kayit = kayit,
                    resimAc = { kayit.fisYolu?.let { buyukResim = it } },
                    sil = { vm.kaydiSil(kayit) },
                    tamDepoDegistir = { vm.kaydiGuncelle(kayit.copy(tamDepo = !kayit.tamDepo)) }
                )
            }
            item { Spacer(Modifier.size(20.dp)) }
        }
    }

    buyukResim?.let { yol ->
        androidx.compose.ui.window.Dialog(onDismissRequest = { buyukResim = null }) {
            AsyncImage(
                model = File(yol),
                contentDescription = "Fiş",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FisKarti(
    kayit: YakitKaydi,
    resimAc: () -> Unit,
    sil: () -> Unit,
    tamDepoDegistir: () -> Unit
) {
    Card {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (kayit.fisYolu != null && File(kayit.fisYolu).exists()) {
                AsyncImage(
                    model = File(kayit.fisYolu),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clickableSade(resimAc)
                )
                Spacer(Modifier.size(12.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${kayit.tarih.tarihKisa()} · ${kayit.km.km()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${kayit.litre.litre(2)} · ${kayit.tutar.tl(2)}" +
                        if (kayit.litre > 0) " (${(kayit.tutar / kayit.litre).sayi(2)} ₺/L)" else "",
                    style = MaterialTheme.typography.bodySmall
                )
                if (kayit.istasyon.isNotBlank()) {
                    Text(
                        kayit.istasyon,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!kayit.tamDepo) {
                    Text(
                        "Tam dolum değil — analizde dikkate alınır",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row {
                    IconButton(onClick = tamDepoDegistir) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = "Tam dolum işareti",
                            tint = if (kayit.tamDepo) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = sil) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
