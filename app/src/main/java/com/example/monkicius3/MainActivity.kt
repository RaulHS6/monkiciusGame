package com.example.monkicius3

import android.os.Bundle
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
import android.view.WindowManager
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    // Actualiza la posición pero limita el movimiento a los bordes de la pantalla
                    val newX = offsetX + (-it.values[0] * 5)
                    val newY = offsetY + (it.values[1] * 5)

                    offsetX = newX.coerceIn(0f, screenWidth.value)
                    offsetY = newY.coerceIn(0f, screenHeight.value)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No necesitamos manejar cambios en la precisión del sensor para este caso
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
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        )
    }
}
