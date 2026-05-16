package com.smartlogix.app.ui.screen.componentes

import android.Manifest
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Composable para escanear códigos de barras usando CameraX y ML Kit
 *
 * Soporta:
 * - Códigos 1D: EAN-13, UPC-A, Code-128, etc.
 * - Códigos 2D: QR Code, Data Matrix, etc.
 *
 * @param onBarcodeScanned Callback cuando se detecta un código
 * @param onClose Callback para cerrar el escáner
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BarcodeScanner(
    onBarcodeScanned: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estado del permiso de cámara
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    // Estado de escaneo
    var isScanning by remember { mutableStateOf(true) }
    var lastScannedCode by remember { mutableStateOf<String?>(null) }

    // Solicitar permiso al iniciar si no está otorgado
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            // Permiso otorgado - Mostrar cámara
            cameraPermissionState.status.isGranted -> {
                // Vista de la cámara
                CameraPreview(
                    onBarcodeScanned = { barcode ->
                        if (isScanning && barcode != lastScannedCode) {
                            lastScannedCode = barcode
                            isScanning = false
                            onBarcodeScanned(barcode)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay con instrucciones y botón cerrar
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .safeDrawingPadding()
                ) {
                    // Botón cerrar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledTonalButton(
                            onClick = onClose,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cerrar")
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Instrucciones
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp), // Add bottom padding for safety
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📷 Escanear Código",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Apunta la cámara hacia el código de barras o QR",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            if (!isScanning) {
                                Spacer(modifier = Modifier.height(16.dp))
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }

            // Permiso denegado - Mostrar explicación
            cameraPermissionState.status.shouldShowRationale -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📷 Permiso de Cámara Requerido",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Para escanear códigos de barras, necesitamos acceso a tu cámara.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PrimaryButton(
                        text = "Otorgar Permiso",
                        onClick = { cameraPermissionState.launchPermissionRequest() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SecondaryButton(
                        text = "Cancelar",
                        onClick = onClose
                    )
                }
            }

            // Permiso denegado permanentemente
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "⚠️ Permiso Denegado",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "El permiso de cámara fue denegado. Por favor, habilítalo en la configuración de la aplicación.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    SecondaryButton(
                        text = "Cerrar",
                        onClick = onClose
                    )
                }
            }
        }
    }
}

/**
 * Vista previa de la cámara con análisis de códigos de barras
 */
@Composable
private fun CameraPreview(
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Configurar CameraX
    LaunchedEffect(previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Image Analysis para ML Kit
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Analizador de códigos de barras con ML Kit
            val barcodeScanner = BarcodeScanning.getClient()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(barcodeScanner, imageProxy, onBarcodeScanned)
            }

            // Selector de cámara (trasera)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Desvincular casos de uso anteriores
                cameraProvider.unbindAll()

                // Vincular casos de uso a la cámara
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraPreview", "Error al iniciar cámara", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Liberar recursos al salir
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

/**
 * Procesa la imagen para detectar códigos de barras con ML Kit
 */
@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onBarcodeScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image

    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    when (barcode.valueType) {
                        Barcode.TYPE_TEXT,
                        Barcode.TYPE_PRODUCT,
                        Barcode.TYPE_ISBN,
                        Barcode.TYPE_URL -> {
                            barcode.rawValue?.let { value ->
                                onBarcodeScanned(value.trim())
                            }
                        }
                        else -> {
                            barcode.rawValue?.let { value ->
                                onBarcodeScanned(value.trim())
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("BarcodeScanning", "Error al escanear", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

