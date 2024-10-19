package com.example.shiosabin

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.shiosabin.BuildConfig.MAP_DATA_FETCH_NETWORK_ADDRESS
import com.example.shiosabin.BuildConfig.PREDICT_DATA_FETCH_NETWORK_ADDRESS
import com.example.shiosabin.BuildConfig.SENSOR_DATA_FETCH_NETWORK_ADDRESS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

object DataHandler {
    suspend fun fetchFromSensorApi(context: Context): List<String> {
        // SharedPreferencesを取得
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("SensorPrefs", Context.MODE_PRIVATE)
        // センサーIDを読み取る
        val sensorID = sharedPreferences.getString("SENSOR_ID", "")

        if (sensorID.isNullOrEmpty()) {
            return listOf("Error: Sensor ID is missing")
        }

        val urlString = SENSOR_DATA_FETCH_NETWORK_ADDRESS + sensorID

        return try {
            withContext(Dispatchers.IO) {
                val url = URL(urlString)
                val con = url.openConnection() as HttpURLConnection
                try {
                    con.connectTimeout = 30_000
                    con.readTimeout = 30_000
                    con.requestMethod = "GET"
                    con.connect()

                    if (con.responseCode != HttpURLConnection.HTTP_OK) {
                        return@withContext listOf("Error: Response Code ${con.responseCode}")
                    }

                    val str = con.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    if (str.isEmpty() || !str.startsWith("[")) {
                        throw Exception("Invalid response format: $str")
                    }
                    val jsonArray = JSONArray(str)

                    // JSONArray の最初の配列を取得し、数値だけ取り出す
                    val firstArray = jsonArray.getJSONArray(0)

                    // 数値のみをフィルタしてリストに格納
                    val resultList = mutableListOf<String>()
                    for (i in 0 until firstArray.length()) {
                        val item = firstArray.get(i)
                        resultList.add(item.toString())

                    }
                    resultList

                } finally {
                    con.disconnect()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            listOf("Error: ${e.message}")
        }
    }


    suspend fun fetchFromMapApi(context: Context): Array<MutableList<Pair<Int, String>>> {
        // SharedPreferencesを取得
        val urlString = MAP_DATA_FETCH_NETWORK_ADDRESS
        return try {
            withContext(Dispatchers.IO) {
                val url = URL(urlString)
                val con = url.openConnection() as HttpURLConnection
                con.connectTimeout = 30_000
                con.readTimeout = 30_000
                con.requestMethod = "GET"
                con.connect()

                if (con.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Error: Response Code ${con.responseCode}")
                }

                val str = con.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                if (str.isEmpty() || !str.startsWith("[")) {
                    throw Exception("Invalid response format: $str")
                }

                val jsonArray = JSONArray(str)

                // 結果を格納するためのリストを用意
                val resultList = Array(jsonArray.length()) { mutableListOf<Pair<Int, String>>() }

                // API結果をループして処理
                for (i in 0 until jsonArray.length()) {
                    val groupArray = jsonArray.getJSONArray(i)
                    for (j in 0 until groupArray.length()) {
                        val data = groupArray.getJSONArray(j)
                        val number = data.getInt(0)  // Numberを取得
                        val point = data.getString(1)  // POINT(座標)を取得

                        // 各グループの配列に (Number, Point) のペアを追加
                        resultList[i].add(Pair(number, point))
                    }
                }

                resultList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            arrayOf(mutableListOf<Pair<Int, String>>(Pair(-1, "Error: ${e.message}")))
        }
    }

    suspend fun fetchFromPredictionApi(context: Context, additionalURL: String): List<List<String>> {
        // SharedPreferencesを取得
        val urlString = PREDICT_DATA_FETCH_NETWORK_ADDRESS

        return try {
            withContext(Dispatchers.IO) {
                val url = URL(urlString + additionalURL)
                val con = url.openConnection() as HttpURLConnection
                con.connectTimeout = 30_000
                con.readTimeout = 30_000
                con.requestMethod = "GET"
                con.connect()

                if (con.responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext listOf(listOf("Error: Response Code ${con.responseCode}"))
                }

                val str = con.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                if (str.isEmpty() || !str.startsWith("[")) {
                    throw Exception("Invalid response format: $str")
                }

                val jsonArray = JSONArray(str)

                // JSONArray の内容を List<List<String>> に変換
                val resultList = mutableListOf<List<String>>()
                for (i in 0 until jsonArray.length()) {
                    val innerArray = jsonArray.getJSONArray(i)
                    val innerList = mutableListOf<String>()
                    for (j in 0 until innerArray.length()) {
                        innerList.add(innerArray.get(j).toString())
                    }
                    resultList.add(innerList)
                }
                resultList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            listOf(listOf("Error: ${e.message}"))
        }
    }


}
