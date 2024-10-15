package com.example.shiosabin

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class SensorIDDialogFragment : DialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sensor_id_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("SensorPrefs", Context.MODE_PRIVATE)

        // Viewの初期化
        val sensorIDEditText: EditText = view.findViewById(R.id.sensorIDEditText)
        val saveButton: Button = view.findViewById(R.id.saveButton)
        val laterTextView: TextView = view.findViewById(R.id.laterTextView)

        // 保存ボタンのクリックリスナー
        saveButton.setOnClickListener {
            val sensorID = sensorIDEditText.text.toString()

            if (sensorID.isNotEmpty()) {
                val editor = sharedPreferences.edit()
                editor.putString("SENSOR_ID", sensorID)
                editor.apply()
                Toast.makeText(requireContext(), "センサーIDが保存されました", Toast.LENGTH_SHORT).show()
                val result = Bundle()
                parentFragmentManager.setFragmentResult("RegisterSensorIDKey", result)
                dismiss() // ダイアログを閉じる
            } else {
                Toast.makeText(requireContext(), "センサーIDを入力してください", Toast.LENGTH_SHORT).show()
            }
        }

        // 後で行うボタンのクリックリスナー
        laterTextView.setOnClickListener {
            dismiss() // ダイアログを閉じる
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE) // タイトルを非表示に
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.white)
    }
}
