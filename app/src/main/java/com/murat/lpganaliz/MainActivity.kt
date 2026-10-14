package com.murat.lpganaliz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.murat.lpganaliz.analiz.LpgAnaliz
import com.murat.lpganaliz.ekranlar.AnalizEkrani
import com.murat.lpganaliz.ekranlar.AnaEkran
import com.murat.lpganaliz.ekranlar.AyarlarEkrani
import com.murat.lpganaliz.ekranlar.GecmisEkrani
import com.murat.lpganaliz.ekranlar.KayitEkrani
import com.murat.lpganaliz.ui.LpgAnalizTema

class MainActivity : ComponentActivity() {

    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val arac by vm.arac.collectAsState()
            val baslik = buildString {
                append(arac?.marka ?: "Toyota")
                append(" ")
                append(arac?.model ?: "Auris")
                append(" ")
                append((arac?.yil ?: 2011).toString())
                append(" · ")
                append(arac?.motorHacmi ?: "1.6")
                append(" · ")
                append((arac?.beygir ?: 132).toString())
                append(" HP")
            }
            LpgAnalizTema {
                Uygulama(vm = vm, aracBasligi = baslik)
            }
        }
    }
}

private object Rotalar {
    const val ANA = "ana"
    const val KAYIT = "kayit"
    const val ANALIZ = "analiz"
    const val GECMIS = "gecmis"
    const val AYARLAR = "ayarlar"
}

@Composable
private fun Uygulama(vm: AppViewModel, aracBasligi: String) {
    val nav = rememberNavController()
    val kayitlar by vm.kayitlar.collectAsStateWithLifecycle()
    var anaKm by remember { mutableStateOf("") }

    val ozet = remember(kayitlar) { LpgAnaliz.genelOzet(kayitlar) }

    NavHost(navController = nav, startDestination = Rotalar.ANA) {
        composable(Rotalar.ANA) {
            AnaEkran(
                aracBasligi = aracBasligi,
                kayitlarSayi = kayitlar.size,
                sonKm = kayitlar.maxByOrNull { it.km }?.km,
                sonOrtalama = ozet?.ortalamaYuzKm?.takeIf { kayitlar.size >= 2 },
                fisEkraniAc = { nav.navigate(Rotalar.KAYIT) },
                analizAc = { nav.navigate(Rotalar.ANALIZ) },
                gecmisAc = { nav.navigate(Rotalar.GECMIS) },
                ayarlarAc = { nav.navigate(Rotalar.AYARLAR) }
            )
        }
        composable(Rotalar.KAYIT) {
            KayitEkrani(
                vm = vm,
                anaKm = anaKm,
                geri = { nav.popBackStack() },
                kaydedildi = {
                    anaKm = ""
                    nav.popBackStack(Rotalar.ANA, inclusive = false)
                }
            )
        }
        composable(Rotalar.ANALIZ) {
            AnalizEkrani(vm = vm, kayitlar = kayitlar, geri = { nav.popBackStack() })
        }
        composable(Rotalar.GECMIS) {
            GecmisEkrani(vm = vm, geri = { nav.popBackStack() })
        }
        composable(Rotalar.AYARLAR) {
            AyarlarEkrani(vm = vm, geri = { nav.popBackStack() })
        }
    }
}
