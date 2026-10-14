package com.murat.lpganaliz.kamera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File

/**
 * Fiş fotoğrafı çekme ekranı (CameraX).
 * Çekilen görüntü uygulama önbelleğine kaydedilir ve [cekilenFis] ile geri verilir.
 */
@Composable
fun FisCekmeEkrani(
    onKapat: () -> Unit,
    cekilenFis: (Uri) -> Unit
) {
    val context = LocalContext.current
    val yasamDongusu = LocalLifecycleOwner.current

    var izinVar by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val izinIste = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { verildi ->
        izinVar = verildi
    }
    LaunchedEffect(Unit) { if (!izinVar) izinIste.launch(Manifest.permission.CAMERA) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (!izinVar) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(56.dp))
                Text(
                    "Fişi çekebilmek için kamera izni gerekiyor.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
                Button(onClick = { izinIste.launch(Manifest.permission.CAMERA) }) { Text("İzin Ver") }
                Button(onClick = onKapat, modifier = Modifier.padding(top = 8.dp)) { Text("Geri") }
            }
            return@Surface
        }

        val imageCaptureKutusu = remember { arrayOfNulls<ImageCapture>(1) }
        var lens by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
        val previewView = remember { PreviewView(context) }

        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        LaunchedEffect(lens) {
            val saglayici = ProcessCameraProvider.getInstance(context).get()
            val onizleme = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val yakalama = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            imageCaptureKutusu[0] = yakalama
                try {
                    saglayici.unbindAll()
                    saglayici.bindToLifecycle(
                        yasamDongusu,
                        CameraSelector.Builder().requireLensFacing(lens).build(),
                        onizleme,
                        yakalama
                    )
                } catch (e: Exception) {
                    // Kamera baglanamazsa kullanici elle girise donebilir.
                }
        }

        DisposableEffect(Unit) {
            onDispose {
                runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
            }
        }

        // Üst bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilledIconButton(onClick = onKapat) { Icon(Icons.Default.Close, contentDescription = "Kapat") }
            FilledIconButton(onClick = {
                lens = if (lens == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
                else CameraSelector.LENS_FACING_BACK
            }) { Icon(Icons.Default.Cameraswitch, contentDescription = "Kamera değiştir") }
        }

        // Alt bar: deklanşör
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 28.dp)) {
                Text(
                    "Fişi kadrajın tamamına alın",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                FilledIconButton(
                    onClick = {
                        val yakalama = imageCaptureKutusu[0] ?: return@FilledIconButton
                        val dosya = File(context.cacheDir, "fis_${System.currentTimeMillis()}.jpg")
                        val secenek = ImageCapture.OutputFileOptions.Builder(dosya).build()
                        yakalama.takePicture(
                            secenek,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(sonuc: ImageCapture.OutputFileResults) {
                                    cekilenFis(Uri.fromFile(dosya))
                                }

                                override fun onError(hata: ImageCaptureException) {
                                    // Sessizce yut: kullanıcı tekrar deneyebilir.
                                }
                            }
                        )
                    },
                    modifier = Modifier.size(76.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Fişi çek", modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}
