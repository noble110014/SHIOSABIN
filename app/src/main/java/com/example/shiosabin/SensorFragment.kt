package com.example.shiosabin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cjh.WaveProgressBarlibrary.WaveProgressBar
import com.example.shiosabin.BuildConfig.DATA_FETCH_NETWORK_ADDRESS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

class SensorFragment : Fragment() {

    private var outProgress: Float = 0F // 外部タンクの最終進捗値
    private var inProgress: Float = 0F   // 内部タンクの最終進捗値
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

        CoroutineScope(Dispatchers.IO).launch {
            val result = SensorDataHandler.fetchFromApi(DATA_FETCH_NETWORK_ADDRESS,requireContext())

            // レスポンスのサイズ確認
            if (result.size > 5) {
                outProgress = result[3].toFloat()
                inProgress = result[4].toFloat()
                val sLevel = result[5]
                val rTank = convertMinutesToHoursAndMinutes(outProgress / 100 * 120)
                val dTank = convertMinutesToHoursAndMinutes(120 - inProgress / 100 * 120)
                withContext(Dispatchers.Main) {
                    setInformationToUI(sLevel, rTank, dTank)
                }
            } else {
                // エラーハンドリング：期待するサイズでない場合の処理
                withContext(Dispatchers.Main) {
                    setInformationToUI("0", "--:--", "--:--")
                }
            }
        }

        // Timerを使用して進捗を更新
        startProgressUpdate(outWaveProgressBar,inWaveProgressBar)

        return view
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
        currentSalinityText.text = salinity // 塩分濃度を設定
        replaceTimeText.text = rTime // 交換時間を設定
        disposeTimeText.text = dTime // 廃棄時間を設定

        // 塩分濃度に応じてアイコンと説明文を設定
        when (salinity) {
            "1" -> {
                salinityIcon.setImageResource(R.drawable.level1_icon) // アイコンを設定
                salinityDescriptionText.text = getString(R.string.s_salt_level1_description) // 説明文を設定
                salinityIconBackGround.setBackgroundColor(R.drawable.level1_frame_style)
            }
            "2" -> {
                salinityIcon.setImageResource(R.drawable.level2_icon) // アイコンを設定
                salinityDescriptionText.text = getString(R.string.s_salt_level2_description) // 説明文を設定
                salinityIconBackGround.setBackgroundColor(R.drawable.level2_frame_style)
            }
            "3" -> {
                salinityIcon.setImageResource(R.drawable.level3_icon) // アイコンを設定
                salinityDescriptionText.text = getString(R.string.s_salt_level3_description) // 説明文を設定
                salinityIconBackGround.setBackgroundColor(R.drawable.level3_frame_style)
            }
            "4" -> {
                salinityIcon.setImageResource(R.drawable.level4_icon) // アイコンを設定
                salinityDescriptionText.text = getString(R.string.s_salt_level4_description) // 説明文を設定
                salinityIconBackGround.setBackgroundColor(R.drawable.level4_frame_style)
            }
            "5" -> {
                salinityIcon.setImageResource(R.drawable.level5_icon) // アイコンを設定
                salinityDescriptionText.text = getString(R.string.s_salt_level5_description) // 説明文を設定
                salinityIconBackGround.setBackgroundColor(R.drawable.level5_frame_style)
            }
            else -> {
                salinityIcon.setImageResource(R.drawable.lader_icon)
                salinityDescriptionText.text = "-"
            }
        }
    }

    private fun convertMinutesToHoursAndMinutes(minutes: Float): String {
        val hours = (minutes / 60).toInt() // 時間を計算しIntに変換
        val remainingMinutes = (minutes % 60).toInt() // 分を計算しIntに変換
        return String.format("%d:%02d", hours, remainingMinutes) // "時間:分"形式で返す
    }

    // Timerの代わりにCoroutineで定期処理を行う
    private fun startProgressUpdate(outWaveProgressBar: WaveProgressBar, inWaveProgressBar: WaveProgressBar) {
        CoroutineScope(Dispatchers.Default).launch {
            while (started) {
                delay(50) // 50ミリ秒ごとに処理

                // 外部タンクの進捗を更新
                if (currentOutProgress < outProgress) {
                    currentOutProgress++
                }

                // 内部タンクの進捗を更新
                if (currentInProgress < inProgress) {
                    currentInProgress++
                }

                // UIの更新はメインスレッドで行う
                withContext(Dispatchers.Main) {
                    outWaveProgressBar.setProgress(currentOutProgress)
                    inWaveProgressBar.setProgress(currentInProgress)

                    // 進捗が完了したら停止
                    if (currentOutProgress >= outProgress && currentInProgress >= inProgress) {
                        stopTimer()
                    }
                }
            }
        }
    }



}