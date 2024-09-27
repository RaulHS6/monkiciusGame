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

    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels.toFloat()
    val screenHeight = displayMetrics.heightPixels.toFloat()

    // Tamaño del mono reducido a la mitad
    val monkeySize = 100f // Suponiendo que el tamaño original era 200px

    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    // Ajuste de la posición basado en los valores del acelerómetro
                    val newX = offsetX + (-it.values[0] * 10)
                    val newY = offsetY + (it.values[1] * 10)

                    // Centrar el mono dentro de los límites de la pantalla
                    offsetX = newX.coerceIn(-(monkeySize / 2), screenWidth - (monkeySize / 2))
                    offsetY = newY.coerceIn(-(monkeySize / 2), screenHeight - (monkeySize / 2))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No se necesita manejar cambios en la precisión del sensor para este caso
            }
        }
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.alex),
            contentDescription = "Monkey Alex",
            modifier = Modifier
                .graphicsLayer(translationX = offsetX, translationY = offsetY)
                .scale(0.5f), // Escala al 50% del tamaño original
            contentScale = ContentScale.FillBounds
        )
    }
}
