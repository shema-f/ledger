package com.example.myapplication.ui.inventory

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.myapplication.util.localizedString
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerModal(
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var manualBarcode by remember { mutableStateOf("") }
    var isFlashEnabled by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = localizedString("Scan Barcode"),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row {
                        if (hasCameraPermission) {
                            IconButton(onClick = {
                                isFlashEnabled = !isFlashEnabled
                                cameraControl?.enableTorch(isFlashEnabled)
                            }) {
                                Icon(
                                    imageVector = if (isFlashEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                                    contentDescription = "Toggle Torch",
                                    tint = if (isFlashEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close Scanner"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Camera Viewport Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx).apply {
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                val executor = ContextCompat.getMainExecutor(ctx)

                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()

                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val analysisExecutor = Executors.newSingleThreadExecutor()
                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build().apply {
                                            setAnalyzer(analysisExecutor, BarcodeAnalyzer { barcode ->
                                                ContextCompat.getMainExecutor(ctx).execute {
                                                    onBarcodeScanned(barcode)
                                                }
                                            })
                                        }

                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                    try {
                                        cameraProvider.unbindAll()
                                        val camera = cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview,
                                            imageAnalysis
                                        )
                                        cameraControl = camera.cameraControl
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }, executor)

                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Target Frame Overlay
                        ScannerOverlay()

                    } else {
                        // Camera Permission Request Banner inside Camera View Box
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = localizedString("Camera permission is required to scan barcodes."),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                Text(localizedString("Grant Camera Permission"))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manual / Simulated Barcode Input Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = localizedString("Or enter barcode manually / simulate scan:"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = manualBarcode,
                                onValueChange = { manualBarcode = it },
                                placeholder = { Text("e.g. 123456789012") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (manualBarcode.isNotBlank()) {
                                            onBarcodeScanned(manualBarcode.trim())
                                        }
                                    }
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (manualBarcode.isNotBlank()) {
                                        onBarcodeScanned(manualBarcode.trim())
                                    }
                                },
                                enabled = manualBarcode.isNotBlank()
                            ) {
                                Text(localizedString("Scan"))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerOverlay() {
    val cornerColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val boxWidth = size.width * 0.75f
        val boxHeight = size.width * 0.5f
        val left = (size.width - boxWidth) / 2f
        val top = (size.height - boxHeight) / 2f
        val cornerLength = 30.dp.toPx()
        val strokeWidth = 4.dp.toPx()

        // Top-Left Corner
        drawPath(
            path = Path().apply {
                moveTo(left, top + cornerLength)
                lineTo(left, top)
                lineTo(left + cornerLength, top)
            },
            color = cornerColor,
            style = Stroke(width = strokeWidth)
        )
        // Top-Right Corner
        drawPath(
            path = Path().apply {
                moveTo(left + boxWidth - cornerLength, top)
                lineTo(left + boxWidth, top)
                lineTo(left + boxWidth, top + cornerLength)
            },
            color = cornerColor,
            style = Stroke(width = strokeWidth)
        )
        // Bottom-Left Corner
        drawPath(
            path = Path().apply {
                moveTo(left, top + boxHeight - cornerLength)
                lineTo(left, top + boxHeight)
                lineTo(left + cornerLength, top + boxHeight)
            },
            color = cornerColor,
            style = Stroke(width = strokeWidth)
        )
        // Bottom-Right Corner
        drawPath(
            path = Path().apply {
                moveTo(left + boxWidth - cornerLength, top + boxHeight)
                lineTo(left + boxWidth, top + boxHeight)
                lineTo(left + boxWidth, top + boxHeight - cornerLength)
            },
            color = cornerColor,
            style = Stroke(width = strokeWidth)
        )
    }
}

private class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(
                BarcodeFormat.EAN_13,
                BarcodeFormat.EAN_8,
                BarcodeFormat.UPC_A,
                BarcodeFormat.UPC_E,
                BarcodeFormat.CODE_128,
                BarcodeFormat.CODE_39,
                BarcodeFormat.QR_CODE
            ),
            DecodeHintType.TRY_HARDER to true
        )
        setHints(hints)
    }

    @Volatile
    private var isDetected = false

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        if (isDetected) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null && mediaImage.format == ImageFormat.YUV_420_888) {
            val buffer = mediaImage.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val width = mediaImage.width
            val height = mediaImage.height

            if (bytes.size >= width * height) {
                val source = PlanarYUVLuminanceSource(
                    bytes, width, height, 0, 0, width, height, false
                )
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

                try {
                    val result = reader.decodeWithState(binaryBitmap)
                    val barcodeText = result.text
                    if (!barcodeText.isNullOrBlank()) {
                        isDetected = true
                        onBarcodeDetected(barcodeText)
                    }
                } catch (e: Exception) {
                    // Frame does not contain valid barcode
                } finally {
                    reader.reset()
                }
            }
        }
        imageProxy.close()
    }
}
