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
import android.app.AlertDialog
import kotlinx.coroutines.delay

class LoginDialogFragment : DialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)

        // Viewの初期化
        val usernameEditText: EditText = view.findViewById(R.id.sensorIDEditText)
        val passwordEditText: EditText = view.findViewById(R.id.passwordEditText)
        val loginButton: Button = view.findViewById(R.id.loginButton)
        val registerButton: Button = view.findViewById(R.id.registerButton)

        // ログインボタンのクリックリスナーを設定
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val success = login(username, password)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            saveLoginState(true)
                            showSuccessDialog("ログイン成功: $username")
                            onLoginSuccessful()
                            dismiss()
                        } else {
                            showFailureDialog("ログイン失敗")
                        }
                    }
                }
            } else {
                showFailureDialog("ユーザー名とパスワードを入力してください")
            }
        }

        // 登録ボタンのクリックリスナーを設定
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val success = register(username, password)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            saveLoginState(true)
                            onLoginSuccessful()
                            showSuccessDialog("登録成功: $username")
                            dismiss()
                        } else {
                            showFailureDialog("登録失敗")
                        }
                    }
                }
            } else {
                showFailureDialog("ユーザー名とパスワードを入力してください")
            }
        }
    }

    private fun saveLoginState(isLoggedIn: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.5).toInt()
        )
        window?.setBackgroundDrawableResource(android.R.color.white)
    }

    private fun onLoginSuccessful() {
        val result = Bundle()
        result.putBoolean("loginSuccess", true)
        parentFragmentManager.setFragmentResult("loginRequestKey", result)

        SensorIDDialogFragment().show(parentFragmentManager, "SensorIDDialog")

        dismiss()
    }

    // ログイン処理
    private suspend fun login(username: String, password: String): Boolean {
        return sendRequest(LOGIN_NETWORK_ADDRESS, username, password)
    }

    // 登録処理
    private suspend fun register(username: String, password: String): Boolean {
        return sendRequest(REGISTER_NETWORK_ADDRESS, username, password)
    }

    // サーバーへリクエストを送信
    private suspend fun sendRequest(urlString: String, username: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonBody = JSONObject()
                jsonBody.put("name", username)
                jsonBody.put("password", password)

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonBody.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                connection.disconnect()

                responseCode == HttpURLConnection.HTTP_OK
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun showSuccessDialog(message: String) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("成功")
        alert.show()
    }

    // ログイン失敗時
    private fun showFailureDialog(message: String) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("失敗")
        alert.show()
    }
}
