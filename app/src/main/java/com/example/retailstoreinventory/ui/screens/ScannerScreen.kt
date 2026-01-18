package com.example.retailstoreinventory.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.retailstoreinventory.ui.camera.BarcodeScanner
import kotlinx.coroutines.delay

private const val TAG = "ScannerScreen"

@Composable
fun ScannerScreen(onResult: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasCamPermission = it
        if (!it) {
            Log.w(TAG, "Camera permission denied")
        }
    }
    val scanner = remember { BarcodeScanner() }
    var scanStatus by remember { mutableStateOf("Ready to scan...") }
    var lastScannedBarcode by remember { mutableStateOf("") }
    var showScanFeedback by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "scan-animation")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan-line"
    )

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    BackHandler { onBack() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCamPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val selector = CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                .build()

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                                .build()

                            var isScanned = false
                            var frameCount = 0

                            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                frameCount++
                                if (!isScanned) {
                                    val result = scanner.processImage(imageProxy)

                                    if (result != null) {
                                        Log.d(TAG, "✓ Barcode detected: $result on frame $frameCount")
                                        isScanned = true
                                        lastScannedBarcode = result
                                        showScanFeedback = true

                                        // Trigger the result callback
                                        onResult(result)

                                        // Close the scanner after a short delay
                                        // This gives visual feedback of successful scan
                                    } else {
                                        // Log every 30 frames to avoid spam
                                        if (frameCount % 30 == 0) {
                                            Log.d(TAG, "Frame $frameCount: No barcode detected")
                                        }
                                    }
                                }
                            }

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                imageAnalysis
                            )

                            Log.d(TAG, "✓ Camera successfully bound to lifecycle")
                        } catch (e: Exception) {
                            Log.e(TAG, "✗ Camera binding error", e)
                            scanStatus = "Camera error: ${e.message}"
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scanner overlay
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Scanning frame
                    Box(
                        modifier = Modifier
                            .size(280.dp, 160.dp)
                            .border(2.dp, Color.Cyan, RoundedCornerShape(12.dp))
                    ) {
                        // Animated scan line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = (160 * scanLineY).dp)
                                .background(Color.Cyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Align barcode inside frame",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Barcode: $lastScannedBarcode",
                        color = Color.Yellow,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            // No permission
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Camera permission required",
                    color = Color.White
                )
            }
        }

        // Close button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .background(Color.Black.copy(0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White)
        }
    }
}