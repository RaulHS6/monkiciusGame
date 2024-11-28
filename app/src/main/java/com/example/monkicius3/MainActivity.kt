package com.example.monkicius3

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.monkicius3.ui.theme.Monkicius3Theme
import android.hardware.SensorManager
import android.hardware.SensorEventListener
import android.hardware.SensorEvent
import android.hardware.Sensor
import androidx.compose.runtime.*
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.util.DisplayMetrics
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Modo pantalla completa para versiones anteriores a Android 11 (API 21)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        setContent {
            Monkicius3Theme {
                MonkeyMover()
            }
        }
    }
}

@Composable
fun MonkeyMover() {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isGameWon by remember { mutableStateOf(false) }
    var monkeyRect by remember { mutableStateOf(Rect.Zero) }
    var mazeWalls by remember { mutableStateOf(listOf<Rect>()) }
    var goalRect by remember { mutableStateOf(Rect.Zero) }

    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels.toFloat()
    val screenHeight = displayMetrics.heightPixels.toFloat()
    val monkeySize = minOf(screenWidth, screenHeight) * 0.06f

    LaunchedEffect(screenWidth, screenHeight) {
        mazeWalls = createMazeWalls(screenWidth, screenHeight)
    }

    LaunchedEffect(screenWidth, screenHeight) {
        goalRect = Rect(
            left = screenWidth * 0.90f,
            top = screenHeight * 0.90f,
            right = screenWidth * 0.95f,
            bottom = screenHeight * 0.95f
        )
    }

    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (isGameWon) return@let

                    val newX = offsetX + (-it.values[0] * 10)
                    val newY = offsetY + (it.values[1] * 10)

                    val newMonkeyRect = Rect(
                        left = newX,
                        top = newY,
                        right = newX + monkeySize,
                        bottom = newY + monkeySize
                    )

                    if (mazeWalls.none { wall -> wall.overlaps(newMonkeyRect) }) {
                        offsetX = newX.coerceIn(0f, screenWidth - monkeySize)
                        offsetY = newY.coerceIn(0f, screenHeight - monkeySize)
                        monkeyRect = newMonkeyRect

                        if (goalRect.overlaps(newMonkeyRect)) {
                            isGameWon = true
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo del laberinto
        Image(
            painter = painterResource(id = R.drawable.maze),
            contentDescription = "Maze",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Mono
        Image(
            painter = painterResource(id = R.drawable.alex),
            contentDescription = "Monkey Alex",
            modifier = Modifier
                .graphicsLayer(translationX = offsetX, translationY = offsetY)
                .scale(0.5f)
                .onGloballyPositioned { coordinates ->
                    monkeyRect = Rect(
                        offsetX,
                        offsetY,
                        offsetX + monkeySize,
                        offsetY + monkeySize
                    )
                },
            contentScale = ContentScale.FillBounds
        )

        // Mensaje de victoria
        if (isGameWon) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "¡Ganaste!",
                    color = Color.White,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        textDecoration = TextDecoration.None
                    )
                )
            }
        }
    }
}

// Función actualizada para coincidir exactamente con maze.png
private fun createMazeWalls(screenWidth: Float, screenHeight: Float): List<Rect> {
    val wallThickness = minOf(screenWidth, screenHeight) * 0.03f // 3% para paredes más visibles
    
    return listOf(
        // Bordes externos
        Rect(0f, 0f, screenWidth, wallThickness), // Superior
        Rect(0f, 0f, wallThickness, screenHeight), // Izquierdo
        Rect(screenWidth - wallThickness, 0f, screenWidth, screenHeight), // Derecho
        Rect(0f, screenHeight - wallThickness, screenWidth, screenHeight), // Inferior

        // Paredes horizontales (de arriba a abajo)
        Rect(screenWidth * 0.2f, screenHeight * 0.15f, screenWidth * 0.5f, screenHeight * 0.15f + wallThickness),
        Rect(screenWidth * 0.6f, screenHeight * 0.15f, screenWidth * 0.8f, screenHeight * 0.15f + wallThickness),
        
        Rect(screenWidth * 0.3f, screenHeight * 0.3f, screenWidth * 0.7f, screenHeight * 0.3f + wallThickness),
        
        Rect(screenWidth * 0.15f, screenHeight * 0.45f, screenWidth * 0.4f, screenHeight * 0.45f + wallThickness),
        Rect(screenWidth * 0.5f, screenHeight * 0.45f, screenWidth * 0.85f, screenHeight * 0.45f + wallThickness),
        
        Rect(screenWidth * 0.3f, screenHeight * 0.6f, screenWidth * 0.6f, screenHeight * 0.6f + wallThickness),
        
        Rect(screenWidth * 0.15f, screenHeight * 0.75f, screenWidth * 0.4f, screenHeight * 0.75f + wallThickness),
        Rect(screenWidth * 0.5f, screenHeight * 0.75f, screenWidth * 0.85f, screenHeight * 0.75f + wallThickness),

        // Paredes verticales (de izquierda a derecha)
        Rect(screenWidth * 0.15f, screenHeight * 0.15f, screenWidth * 0.15f + wallThickness, screenHeight * 0.45f),
        
        Rect(screenWidth * 0.3f, screenHeight * 0.3f, screenWidth * 0.3f + wallThickness, screenHeight * 0.6f),
        
        Rect(screenWidth * 0.45f, screenHeight * 0.45f, screenWidth * 0.45f + wallThickness, screenHeight * 0.75f),
        
        Rect(screenWidth * 0.6f, screenHeight * 0.15f, screenWidth * 0.6f + wallThickness, screenHeight * 0.45f),
        
        Rect(screenWidth * 0.75f, screenHeight * 0.3f, screenWidth * 0.75f + wallThickness, screenHeight * 0.6f),
        
        Rect(screenWidth * 0.85f, screenHeight * 0.45f, screenWidth * 0.85f + wallThickness, screenHeight * 0.75f)
    )
}
