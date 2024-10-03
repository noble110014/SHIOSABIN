package com.example.shiosabin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cjh.WaveProgressBarlibrary.WaveProgressBar
import java.util.Timer
import java.util.TimerTask

class SensorFragment : Fragment() {

    private var outProgress: Int = 80 // 外部タンクの最終進捗値
    private var inProgress: Int = 30   // 内部タンクの最終進捗値
    private var currentOutProgress: Int = 0 // 現在の外部タンクの進捗
    private var currentInProgress: Int = 0  // 現在の内部タンクの進捗
    private var started: Boolean = true
    private val handler = Handler(Looper.getMainLooper()) // メインスレッドのLooperを取得
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

        // Timerを使用して進捗を更新
        timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                if (started) {
                    // 外部タンクの進捗を更新
                    if (currentOutProgress < outProgress) {
                        currentOutProgress++
                    }

                    // 内部タンクの進捗を更新
                    if (currentInProgress < inProgress) {
                        currentInProgress++
                    }

                    // UIの更新はメインスレッドで行う
                    handler.post {
                        outWaveProgressBar.setProgress(currentOutProgress)
                        inWaveProgressBar.setProgress(currentInProgress)

                        // 外部と内部の進捗が両方とも完了したらタイマーを止める
                        if (currentOutProgress >= outProgress && currentInProgress >= inProgress) {
                            stopTimer()
                        }
                    }
                }
            }
        }

        timer?.schedule(timerTask, 0, 50) // 50ミリ秒ごとにタスクを実行

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
}
