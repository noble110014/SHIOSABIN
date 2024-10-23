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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        val hourSaltLevelArray = arrayOf(
            view.findViewById<TextView>(R.id.p_hour_salt_level_0),
            view.findViewById(R.id.p_hour_salt_level_1),
            view.findViewById(R.id.p_hour_salt_level_2)
        )

        val daySaltlevelArray = arrayOf(
            view.findViewById<TextView>(R.id.p_day_salt_level_0),
            view.findViewById(R.id.p_day_salt_level_1),
            view.findViewById(R.id.p_day_salt_level_2),
            view.findViewById(R.id.p_day_salt_level_3),
            view.findViewById(R.id.p_day_salt_level_4),
            view.findViewById(R.id.p_day_salt_level_5)
        )

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val dayResult = DataHandler.fetchFromPredictionApi(requireContext(),"day")
            val hourResult = DataHandler.fetchFromPredictionApi(requireContext(),"hour")
            Log.d("PredictionFragment",dayResult.toString())
            withContext(Dispatchers.Main) {
                // 取得結果を UI に反映させる
                hourSaltLevelArray[0].text = hourResult[1][3]
                hourSaltLevelArray[2].text = hourResult[0][3]

                for (i in 0 until daySaltlevelArray.size - 1)
                {
                    val index = daySaltlevelArray.size - i - 2
                    daySaltlevelArray[i].text = dayResult[index][3]
                }
            }
        }

        // カレンダーのインスタンスを取得
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN)

        // TextViewのリソースIDを取得
        val hourTextArray = arrayOf(
            view.findViewById<TextView>(R.id.p_hour_time_0),
            view.findViewById(R.id.p_hour_time_1),
            view.findViewById(R.id.p_hour_time_2),

        )

        val dayTextArray = arrayOf(
            view.findViewById(R.id.p_day_0),
            view.findViewById(R.id.p_day_1),
            view.findViewById(R.id.p_day_2),
            view.findViewById(R.id.p_day_3),
            view.findViewById(R.id.p_day_4),
            view.findViewById<TextView>(R.id.p_day_5)
        )

        val timeScaleArray = arrayOf(
            view.findViewById(R.id.p_hour_timescale_0),
            view.findViewById(R.id.p_hour_timescale_1),
            view.findViewById<TextView>(R.id.p_hour_timescale_2)
        )

        todaySaltLevelText = view.findViewById(R.id.today_average_salt_level)

        // 最初のTextViewのテキストを空に設定

        val currentHour = (calendar.get(Calendar.HOUR_OF_DAY)) % 24
        hourTextArray[1].text = String.format("%02d:00", currentHour)

        if((currentHour >= 18)  || (currentHour <= 4))
        {
            hourTextArray[0].text = "17:00"
            hourTextArray[2].text = "5:00"
            timeScaleArray[0].text = "昨日"
            timeScaleArray[2].text = "明日"
        }else if(currentHour in 6..14)
        {
            hourTextArray[0].text = "5:00"
            hourTextArray[2].text = "17:00"
            timeScaleArray[0].text = "今日"
            timeScaleArray[2].text = "今日"
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
