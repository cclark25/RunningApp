package com.example.runningapp.presentation

import android.graphics.drawable.Icon
import android.os.BatteryManager
import android.os.Bundle
import android.os.Vibrator
import android.provider.Settings
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GestureDetectorCompat
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.foundation.CurvedScope
import androidx.wear.compose.foundation.curvedBox
import androidx.wear.compose.foundation.curvedComposable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.curvedText
import androidx.wear.input.WearableButtons
import com.example.runningapp.presentation.theme.RunningAppTheme
import kotlinx.coroutines.yield
import java.text.DecimalFormat
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.toKotlinDuration


class RefreshData(be: RunningAppBackend) {
    val duration: Duration = be.totalDuration.toKotlinDuration();
    val lapNum: Int = be.currentLapNumber;
    val lapDuration: Duration = be.lapDuration.toKotlinDuration();
    val heartRate: Float = be.heartRate;
    // TODO: this distance calculation is entering an infinite loop on laps and start/stops I think
    val distanceMeters: Double = be.getDistance(false);
    val lapMeters: Double = be.getDistance(true);
    val gpsState: Boolean = be.gpsStatus;


//    val distanceMeters: Double = 0.0;
}

open class MainActivity : ComponentActivity(),
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {
    var backend: RunningAppBackend? = null
    val self = this
    val ambientOn = mutableStateOf(false)

    val ambientCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback{
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
            self.ambientOn.value = true;
            println("Ambient Mode entered")
        }

        override fun onExitAmbient() {
            self.ambientOn.value = false;
            println("Ambient Mode exited")
        }

        override fun onUpdateAmbient() {
            // ... Called by the system in order to allow the app to periodically
            // update the display while in ambient mode. Typically the system will
            // call this every 60 seconds.
        }
    }

    private val ambientObserver = AmbientLifecycleObserver(this, this.ambientCallback)



    val refresher: Timer = Timer()
//    fixedRateTimer( daemon = true, period = 250, action = {
//        println("UI Refresher")
//        val be = self.backend;
////        if(be != null) {
////            self.refreshData.value = RefreshData(be);
////
////        }
//    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        val keyLabel =  WearableButtons.getButtonLabel(this, keyCode)
//        println("Long press: $keyCode - $keyLabel")

        return super.onKeyUp(keyCode, event)
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val keyLabel =  WearableButtons.getButtonLabel(this, keyCode)
//        println("Key Up: $keyCode - $keyLabel")
        if(keyCode == KeyEvent.KEYCODE_STEM_1){
            val running = this.backend?.startButton()

            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            var vibrationPattern = longArrayOf(0, 500, 50, 300)

            if(running == true){
                vibrationPattern = longArrayOf(0, 300, 50, 500)
            }

            val indexInPatternToRepeat = -1
            vibrator.vibrate(vibrationPattern, indexInPatternToRepeat)

//            this.backend.lapButton()
        }
        return super.onKeyUp(keyCode, event)

    }

    @Composable
    fun Time(dur: kotlin.time.Duration){
        Text(
                dur.toComponents {
                    hours, minutes, seconds, nanoseconds
                    -> "${
                        hours.toString().padStart(2, '0')
                    }:${
                        minutes.toString().padStart(2, '0')
                    }:${
                        seconds.toString().padStart(2, '0')
                    }"
                }
        )
    }

    @Composable
    fun LapNumber(lapNum: Int){
        Text("Lap $lapNum")
    }

    @Composable
    fun Distance(distanceMeters: Double){
        val df = DecimalFormat("##.##")

        Text("${df.format(distanceMeters / 1609.344)} mi")
    }

    @Composable
    fun GPSConnected(connected: Boolean){

//        Text(connected)
        if(connected){
            Text("GPS")
        }
        else {
            Text("---")
        }
    }

    @Composable
    fun HeartRate(rate: Float){
        Text("HR: $rate bpm")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)


        this.mDetector = GestureDetectorCompat(this, this)
        // Set the gesture detector as the double-tap
        // listener.
        this.mDetector.setOnDoubleTapListener(this)

        setTheme(android.R.style.Theme_DeviceDefault)

        this.refresher.run {  }
        val self = this


        setContent {
            RunningAppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color(
                                0,
                                if (this.ambientOn.value) {
                                    0
                                } else {
                                    0x44
                                }, 0
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Row() {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Time(self.refreshData?.value?.duration ?: Duration.ZERO)
                            Distance(self.refreshData?.value?.distanceMeters ?: 0.0)

                            LapNumber(self.refreshData?.value?.lapNum ?: 0)
                            Time(self.refreshData?.value?.lapDuration ?: Duration.ZERO)
                            Distance(distanceMeters = self.refreshData?.value?.lapMeters ?: 0.0)

                            HeartRate(self.refreshData?.value?.heartRate ?: 0.0.toFloat())
//                            GPSConnected(self.refreshData?.value?.gpsState ?: false)
                        }
                    }
                    val gpsIndicator =
                        @Composable{ GPSConnected(connected = self.refreshData.value?.gpsState ?: false) }
                    val batteryIndicator = @Composable {
                        val batteryManager = self.getSystemService(BATTERY_SERVICE) as BatteryManager

                        // Get the battery percentage and store it in a INT variable
                        val batLevel: Int = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                        Text(batLevel.toString() + "%")
                    }
                    TimeText(
                    startCurvedContent =
                    {
                        curvedComposable {gpsIndicator()}
                    },
                    startLinearContent = {
                        gpsIndicator()
                    },


                    endCurvedContent =
                    {
                        curvedComposable { batteryIndicator() }
                    },
                        endLinearContent = {
                            batteryIndicator()
                        }

                )
                }
            }
        }

        this.backend = RunningAppBackend(this)


//        this.backend?.startButton();
        this.backend?.onRefresh = Callback {

//            println("MainActivity callback called")
            val be = self.backend;
            if(be != null) {
                refreshData.value = RefreshData(be)
//                    this.self.refreshData.value = RefreshData(be);
            }
        }

        lifecycle.addObserver(this.ambientObserver)

    }

    var refreshData: MutableState<RefreshData?> = mutableStateOf(null)

    private lateinit var mDetector: GestureDetectorCompat


    override fun onDown(e: MotionEvent): Boolean {
//        println( "onDown: $e")
        return true
    }

    override fun onShowPress(e: MotionEvent) {
//        println("onShowPress: $e")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
//        println("onSingleTapUp: $e")
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
//        println("onScroll: $e1 $e2 ")
        return true;
    }

    override fun onLongPress(e: MotionEvent) {
//        println("onLongPress: $e")
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
//        println("onFling")
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
//            println("UI double tap")
//            if(
                this.backend?.lapButton();// == true
//        ) {
//
//                val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
//                val vibrationPattern = longArrayOf(0, 500)
//                val indexInPatternToRepeat = -1
//                vibrator.vibrate(vibrationPattern, indexInPatternToRepeat)
//            }
//            println("UI double tap done")

//            this.backend.lapButton()

        return true
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
//        println("onDoubleTapEvent: $event")
        return true
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
//        println("onSingleTapConfirmed: $event")
        return true
    }


}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
//    WearApp("Preview Android")
}