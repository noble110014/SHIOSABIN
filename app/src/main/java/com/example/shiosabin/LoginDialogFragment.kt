package com.example.shiosabin

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.shiosabin.BuildConfig.LOGIN_NETWORK_ADDRESS
import com.example.shiosabin.BuildConfig.NETWORK_ADDRESS
import com.example.shiosabin.BuildConfig.REGISTER_NETWORK_ADDRESS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LoginDialogFragment : DialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // レイアウトファイルをDialogFragmentに関連付ける
        return inflater.inflate(R.layout.fragment_login_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)

        // Viewの初期化
        val usernameEditText: EditText = view.findViewById(R.id.sensorIDEditText)
        val passwordEditText: EditText = view.findViewById(R.id.passwordEditText)
        val loginButton: Button = view.findViewById(R.id.loginButton)

        // ログインボタンのクリックリスナーを設定
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // シンプルなログイン処理（ここでは単にトーストを表示）
            if (username.isNotEmpty() && password.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val success = login(username, password)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            saveLoginState(true)
                            Toast.makeText(requireContext(), "ログイン成功: $username", Toast.LENGTH_SHORT).show()
                            onLoginSuccessful()
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), "ログイン失敗", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "ユーザー名とパスワードを入力してください", Toast.LENGTH_SHORT).show()
            }
        }

        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            loginButton.text = "ログイン"
        } else {
            loginButton.text = "登録"
        }
    }

    private fun saveLoginState(isLoggedIn: Boolean) {
        // ログイン状態を保存する
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE) // タイトルを非表示にする
        return dialog
    }

    override fun onStart() {
        super.onStart()
        // ダイアログのウィンドウサイズを調整して、マージンをつける
        val window = dialog?.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),  // 横幅を画面の90%に設定
            (resources.displayMetrics.heightPixels * 0.5).toInt()  // 高さを画面の80%に設定
        )
        window?.setBackgroundDrawableResource(android.R.color.white) // 背景色を白に設定
    }

    private fun onLoginSuccessful() {
        // ログイン成功時の結果を返す
        val result = Bundle()
        result.putBoolean("loginSuccess", true)
        parentFragmentManager.setFragmentResult("loginRequestKey", result)
        dismiss()
    }

    private suspend fun login(username: String, password: String): Boolean {
        val urlString: String

        // ログイン状態を確認して、ログイン用と登録用のURLを切り替える
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            urlString = LOGIN_NETWORK_ADDRESS
        } else {
            urlString = REGISTER_NETWORK_ADDRESS
        }

        return withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                // POSTするデータをJSON形式で作成
                val jsonBody = JSONObject()
                jsonBody.put("name", username)
                jsonBody.put("password", password)

                // データをサーバーに送信
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonBody.toString())
                    writer.flush()
                }

                // サーバーからのレスポンスコードを確認
                val responseCode = connection.responseCode
                connection.disconnect()

                // レスポンスが200 (HTTP_OK) の場合は成功
                responseCode == HttpURLConnection.HTTP_OK
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

}
