package com.example.shiosabin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.shiosabin.BuildConfig.PREDICT_DATA_FETCH_NETWORK_ADDRESS
import com.example.shiosabin.BuildConfig.SENSOR_DATA_FETCH_NETWORK_ADDRESS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PredictionFragment : Fragment() {

    private lateinit var todaySaltLevelText: TextView
    private lateinit var backgroundIcon:ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // フラグメントのレイアウトをインフレート
        val view = inflater.inflate(R.layout.fragment_prediction, container, false)

        val hourSaltLevelArray = arrayOf(
            view.findViewById<TextView>(R.id.p_hour_salt_level_0),
            view.findViewById(R.id.p_hour_salt_level_1),
            view.findViewById(R.id.p_hour_salt_level_2),
            view.findViewById(R.id.p_hour_salt_level_3),
            view.findViewById(R.id.p_hour_salt_level_4),
            view.findViewById(R.id.p_hour_salt_level_5)
        )

        val daySaltlevelArray = arrayOf(
            arrayOf(
                view.findViewById<TextView>(R.id.p_day_max_salt_level_0),
                view.findViewById(R.id.p_day_min_salt_level_0)
            ),
            arrayOf(
                view.findViewById(R.id.p_day_max_salt_level_1),
                view.findViewById(R.id.p_day_min_salt_level_1)
            ),
            arrayOf(
                view.findViewById(R.id.p_day_max_salt_level_2),
                view.findViewById(R.id.p_day_min_salt_level_2)
            ),
            arrayOf(
                view.findViewById(R.id.p_day_max_salt_level_3),
                view.findViewById(R.id.p_day_min_salt_level_3)
            ),
            arrayOf(
                view.findViewById(R.id.p_day_max_salt_level_4),
                view.findViewById(R.id.p_day_min_salt_level_4)
            ),
            arrayOf(
                view.findViewById(R.id.p_day_max_salt_level_5),
                view.findViewById(R.id.p_day_min_salt_level_5)
            )
        )

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val result = DataHandler.fetchFromApi(PREDICT_DATA_FETCH_NETWORK_ADDRESS, requireContext())
            if (result.size >= 7) {
                withContext(Dispatchers.Main) {

                }
            } else {
                withContext(Dispatchers.Main) {

                }
            }
        }

        // カレンダーのインスタンスを取得
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN)

        // TextViewのリソースIDを取得
        val hourTextArray = arrayOf(
            view.findViewById(R.id.p_hour_time_0),
            view.findViewById(R.id.p_hour_time_1),
            view.findViewById(R.id.p_hour_time_2),
            view.findViewById(R.id.p_hour_time_3),
            view.findViewById(R.id.p_hour_time_4),
            view.findViewById<TextView>(R.id.p_hour_time_5)
        )

        val dayTextArray = arrayOf(
            view.findViewById(R.id.p_day_0),
            view.findViewById(R.id.p_day_1),
            view.findViewById(R.id.p_day_2),
            view.findViewById(R.id.p_day_3),
            view.findViewById(R.id.p_day_4),
            view.findViewById<TextView>(R.id.p_day_5)
        )

        todaySaltLevelText = view.findViewById(R.id.today_average_salt_level)
        backgroundIcon = view.findViewById(R.id.background_icon)
        when (todaySaltLevelText.text.toString()) {
            "1" -> backgroundIcon.setImageResource(R.drawable.level1_icon)
            "2" -> backgroundIcon.setImageResource(R.drawable.level2_icon)
            "3" -> backgroundIcon.setImageResource(R.drawable.level3_icon)
            "4" -> backgroundIcon.setImageResource(R.drawable.level4_icon)
            "5" -> backgroundIcon.setImageResource(R.drawable.level5_icon)
        }

        // 最初のTextViewのテキストを空に設定

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
}
