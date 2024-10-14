package com.example.shiosabin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PredictionFragment : Fragment() {

    private lateinit var todaySaltLevelText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // フラグメントのレイアウトをインフレート
        val view = inflater.inflate(R.layout.fragment_prediction, container, false)

        // カレンダーのインスタンスを取得
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN)

        // TextViewのリソースIDを取得
        val hourTextArray = arrayOf(
            view.findViewById<TextView>(R.id.p_hour_time_0),
            view.findViewById<TextView>(R.id.p_hour_time_1),
            view.findViewById<TextView>(R.id.p_hour_time_2),
            view.findViewById<TextView>(R.id.p_hour_time_3),
            view.findViewById<TextView>(R.id.p_hour_time_4),
            view.findViewById<TextView>(R.id.p_hour_time_5)
        )

        val dayTextArray = arrayOf(
            view.findViewById<TextView>(R.id.p_day_0),
            view.findViewById<TextView>(R.id.p_day_1),
            view.findViewById<TextView>(R.id.p_day_2),
            view.findViewById<TextView>(R.id.p_day_3),
            view.findViewById<TextView>(R.id.p_day_4),
            view.findViewById<TextView>(R.id.p_day_5)
        )

        // 最初のTextViewのテキストを空に設定
        val ct = getString(R.string.p_current_time)
        hourTextArray[0].text = ct

        for (i in 0..5) {
            val hour = (calendar.get(Calendar.HOUR_OF_DAY) + i) % 24
            hourTextArray[i].text = String.format("%02d時", hour)
        }

        for (i in 0..5) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val dayText = "$dayOfMonth${getWeek(dayOfWeek)}"
            dayTextArray[i].text = dayText
        }

        return view
    }

    fun getWeek(day: Int): String {
        return when (day) {
            Calendar.SUNDAY -> "(日)"
            Calendar.MONDAY -> "(月)"
            Calendar.TUESDAY -> "(火)"
            Calendar.WEDNESDAY -> "(水)"
            Calendar.THURSDAY -> "(木)"
            Calendar.FRIDAY -> "(金)"
            Calendar.SATURDAY -> "(土)"
            else -> ""
        }
    }

    public fun SetTodaySaltLevel(level:String)
    {
        todaySaltLevelText.text = level
    }
}
