package com.example.foodsave.pages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.foodsave.camera.FoodImageManager
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onImageCaptured: (String) -> Unit,
    onBack: () -> Unit
) {

    // Camera setup
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Permission state
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Capture state
    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(null)
    }

    var isCapturing by remember {
        mutableStateOf(false)
    }

    var cameraError by remember {
        mutableStateOf<String?>(null)
    }

    // Permission launcher
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            hasPermission = granted

            cameraError = if (granted) {
                null
            } else {
                "Camera permission was denied."
            }
        }

    // Request permission
    LaunchedEffect(Unit) {

        if (!hasPermission) {

            permissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    // Release camera resources
    DisposableEffect(context) {

        onDispose {

            imageCapture = null

            try {

                val cameraProviderFuture =
                    ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener(
                    {
                        try {
                            cameraProviderFuture.get().unbindAll()
                        } catch (_: Exception) {
                            // Camera already released
                        }
                    },
                    ContextCompat.getMainExecutor(context)
                )

            } catch (_: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    // Screen layout
    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Capture Food Image")
                },

                navigationIcon = {

                    IconButton(onClick = onBack) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding),

            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            if (hasPermission) {

                // Camera preview
                AndroidView(

                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),

                    factory = { ctx ->

                        val previewView = PreviewView(ctx).apply {

                            scaleType =
                                PreviewView.ScaleType.FILL_CENTER
                        }

                        val cameraProviderFuture =
                            ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener(

                            {

                                try {

                                    val cameraProvider =
                                        cameraProviderFuture.get()

                                    // CameraX Preview setup
                                    val preview = Preview.Builder()
                                        .build()
                                        .also {

                                            it.surfaceProvider =
                                                previewView.surfaceProvider
                                        }

                                    // Image capture setup
                                    val capture =
                                        ImageCapture.Builder()
                                            .setCaptureMode(
                                                ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                                            )
                                            .build()

                                    cameraProvider.unbindAll()

                                    // Bind camera to lifecycle
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        capture
                                    )

                                    imageCapture = capture
                                    cameraError = null

                                } catch (e: Exception) {

                                    cameraError =
                                        "Unable to start camera. Please try again."

                                    Toast.makeText(
                                        ctx,
                                        "Unable to start camera.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },

                            ContextCompat.getMainExecutor(ctx)
                        )

                        previewView
                    }
                )

                // Camera error
                cameraError?.let { error ->

                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                // Capture button
                Button(

                    onClick = {

                        if (!isCapturing) {

                            isCapturing = true

                            captureFoodImage(

                                context = context,
                                imageCapture = imageCapture,

                                onSuccess = { savedPath ->

                                    isCapturing = false

                                    onImageCaptured(savedPath)
                                },

                                onError = { message ->

                                    isCapturing = false

                                    Toast.makeText(
                                        context,
                                        message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    },

                    enabled = imageCapture != null && !isCapturing,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(52.dp)

                ) {

                    // Capture progress
                    if (isCapturing) {

                        CircularProgressIndicator(

                            modifier = Modifier
                                .height(22.dp)
                                .width(22.dp),

                            strokeWidth = 2.dp
                        )

                        Spacer(
                            modifier = Modifier.width(10.dp)
                        )

                        Text("Capturing...")

                    } else {

                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text("Capture Image")
                    }
                }

            } else {

                // Camera permission screen
                Box(

                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),

                    contentAlignment = Alignment.Center

                ) {

                    Column(

                        horizontalAlignment = Alignment.CenterHorizontally,

                        verticalArrangement = Arrangement.Center

                    ) {

                        Text(
                            text = "Camera permission is required to capture food images.",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        // Grant camera permission
                        Button(

                            onClick = {

                                permissionLauncher.launch(
                                    Manifest.permission.CAMERA
                                )
                            }

                        ) {

                            Text("Grant Camera Permission")
                        }
                    }
                }
            }
        }
    }
}


// Capture food image
private fun captureFoodImage(
    context: Context,
    imageCapture: ImageCapture?,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {

    // Check camera availability
    val capture = imageCapture

    if (capture == null) {

        onError("Camera is not ready. Please try again.")
        return
    }

    // Create temporary image file
    val file = File(
        context.cacheDir,
        "food_${System.currentTimeMillis()}.jpg"
    )

    // Set output options
    val outputOptions =
        ImageCapture.OutputFileOptions.Builder(file).build()

    // Capture picture
    capture.takePicture(

        outputOptions,

        ContextCompat.getMainExecutor(context),

        object : ImageCapture.OnImageSavedCallback {

            // Handle captured image
            override fun onImageSaved(
                output: ImageCapture.OutputFileResults
            ) {

                var bitmap: Bitmap? = null

                try {

                    // Read captured image
                    bitmap = BitmapFactory.decodeFile(
                        file.absolutePath
                    ) ?: throw Exception(
                        "Unable to read captured image."
                    )

                    // Save permanent image
                    val savedPath = FoodImageManager.saveImage(
                        context = context,
                        bitmap = bitmap,
                        name = "food_${System.currentTimeMillis()}.jpg"
                    )

                    onSuccess(savedPath)

                } catch (e: Exception) {

                    // Handle image save errors
                    onError(
                        e.message ?: "Unable to save food image."
                    )

                } finally {

                    // Release bitmap and temporary file
                    bitmap?.recycle()
                    file.delete()
                }
            }

            // Handle capture errors
            override fun onError(
                exception: ImageCaptureException
            ) {

                file.delete()

                onError(
                    exception.message
                        ?: "Image capture failed. Please try again."
                )
            }
        }
    )
}


// Android Studio UI Preview
@OptIn(ExperimentalMaterial3Api::class)
@ComposePreview(
    showBackground = true,
    showSystemUi = true,
    name = "Camera Screen Preview"
)
@Composable
fun CameraScreenPreview() {

    MaterialTheme {

        Scaffold(

            topBar = {

                TopAppBar(

                    title = {
                        Text("Capture Food Image")
                    },

                    navigationIcon = {

                        IconButton(onClick = {}) {

                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }

        ) { padding ->

            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),

                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                // Camera preview placeholder
                Box(

                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),

                    contentAlignment = Alignment.Center

                ) {

                    Text(
                        text = "Camera Preview",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                // Capture button preview
                Button(

                    onClick = {},

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(52.dp)

                ) {

                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text("Capture Image")
                }
            }
        }
    }
}