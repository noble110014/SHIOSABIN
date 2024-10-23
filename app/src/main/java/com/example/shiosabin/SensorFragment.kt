package com.example.shiosabin

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cjh.WaveProgressBarlibrary.WaveProgressBar
import com.example.shiosabin.BuildConfig.SENSOR_DATA_FETCH_NETWORK_ADDRESS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer

class SensorFragment : Fragment() {

    private var outProgress: Int = 0 // 外部タンクの最終進捗値
    private var inProgress: Int = 0   // 内部タンクの最終進捗値
    private var currentOutProgress: Int = 0 // 現在の外部タンクの進捗
    private var currentInProgress: Int = 0  // 現在の内部タンクの進捗
    private lateinit var currentSalinityText:TextView
    private lateinit var salinityIcon:ImageView
    private lateinit var salinityIconBackGround:View
    private lateinit var salinityDescriptionText:TextView
    private lateinit var replaceTimeText:TextView
    private lateinit var disposeTimeText:TextView
    private var started: Boolean = true
    private var timer: Timer? = null // タイマーをフィールドとして宣言
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sensor, container, false)

        // WaveProgressBarを取得
        val outWaveProgressBar: WaveProgressBar = view.findViewById(R.id.s_out_tank)
        val inWaveProgressBar: WaveProgressBar = view.findViewById(R.id.s_in_tank)
        currentSalinityText = view.findViewById(R.id.s_salt_level)
        salinityIcon = view.findViewById(R.id.s_salt_icon)
        salinityIconBackGround = view.findViewById(R.id.s_icon_background)
        salinityDescriptionText = view.findViewById(R.id.s_salt_level_description)
        replaceTimeText = view.findViewById(R.id.s_replace_time)
        disposeTimeText = view.findViewById(R.id.s_dispose_time)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                context?.let { safeContext ->
                    val result = DataHandler.fetchFromSensorApi(safeContext)
                    Log.d("SensorFragment", "API Response: $result")

                    if (result.size >= 6) {
                        outProgress = result[3].toInt()
                        inProgress = result[4].toInt()
                        val sLevel = result[5]

                        val rTank = convertMinutesToHoursAndMinutes(120 * (1 - inProgress / 100f).toFloat())
                        val dTank = convertMinutesToHoursAndMinutes(120 * (1 - outProgress / 100f).toFloat())
                        withContext(Dispatchers.Main) {
                            setInformationToUI(sLevel.toString(), rTank, dTank)
                            startProgressUpdate(outWaveProgressBar, inWaveProgressBar)
                        }
                    } else {
                        Log.e("SensorFragment", "Invalid response size: ${result.size}")
                        withContext(Dispatchers.Main) {
                            setInformationToUI("0", "--:--", "--:--")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SensorFragment", "Error fetching data: ${e.message}")
                withContext(Dispatchers.Main) {
                    setInformationToUI("0", "--:--", "--:--")
                }
            }
        }

        return view
    }



    private fun startProgressUpdate(outWaveProgressBar: WaveProgressBar, inWaveProgressBar: WaveProgressBar) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            try {
                while (started) {
                    delay(50)

                    if (currentOutProgress < outProgress) {
                        currentOutProgress++
                    }

                    if (currentInProgress < inProgress) {
                        currentInProgress++
                    }

                    withContext(Dispatchers.Main) {
                        outWaveProgressBar.setProgress(currentOutProgress)
                        inWaveProgressBar.setProgress(currentInProgress)

                        if (currentOutProgress >= outProgress && currentInProgress >= inProgress) {
                            stopTimer()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SensorFragment", "Error during progress update: ${e.message}")
            }
        }
    }



    // タイマーを止める処理
    private fun stopTimer() {
        timer?.cancel()
        timer = null
        started = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // タイマーのキャンセルを忘れずに
        stopTimer()
    }

    private fun setInformationToUI(salinity: String, rTime: String, dTime: String) {
        try {
            currentSalinityText.text = salinity
            replaceTimeText.text = rTime
            disposeTimeText.text = dTime

            when (salinity) {
                "1" -> {
                    salinityIcon.setImageResource(R.drawable.level1_icon)
                    salinityDescriptionText.text = getString(R.string.s_salt_level1_description)
                    salinityIconBackGround.setBackgroundResource(R.drawable.level1_frame_style)
                }
                "2" -> {
                    salinityIcon.setImageResource(R.drawable.level2_icon)
                    salinityDescriptionText.text = getString(R.string.s_salt_level2_description)
                    salinityIconBackGround.setBackgroundResource(R.drawable.level2_frame_style)
                }
                // その他のケースも同様に
                "3" -> {
                    salinityIcon.setImageResource(R.drawable.level3_icon)
                    salinityDescriptionText.text = getString(R.string.s_salt_level3_description)
                    salinityIconBackGround.setBackgroundResource(R.drawable.level3_frame_style)
                }

                "4" -> {
                    salinityIcon.setImageResource(R.drawable.level4_icon)
                    salinityDescriptionText.text = getString(R.string.s_salt_level4_description)
                    salinityIconBackGround.setBackgroundResource(R.drawable.level4_frame_style)
                }

                "5" -> {
                    salinityIcon.setImageResource(R.drawable.level5_icon)
                    salinityDescriptionText.text = getString(R.string.s_salt_level5_description)
                    salinityIconBackGround.setBackgroundResource(R.drawable.level5_frame_style)
                }
                else ->
                    salinityIcon.setImageResource(R.drawable.level3_icon)
            }
        } catch (e: Exception) {
            Log.e("SensorFragment", "Error setting UI: ${e.message}")
        }
    }


    private fun convertMinutesToHoursAndMinutes(minutes: Float): String {
        val hours = (minutes / 60).toInt() // 時間を計算しIntに変換
        val remainingMinutes = (minutes % 60).toInt()// 分を計算しIntに変換
        return String.format("%02d:%02d", hours, remainingMinutes) // "時間:分"形式で返す
    }
}