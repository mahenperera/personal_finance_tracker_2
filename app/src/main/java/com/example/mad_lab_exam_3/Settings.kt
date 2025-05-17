package com.example.mad_lab_exam_3

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class Settings : Fragment() {

    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val budgetInput = view.findViewById<TextInputEditText>(R.id.budgetEditText)
        val saveBtn = view.findViewById<Button>(R.id.saveSettingsBtn)
        val backupBtn = view.findViewById<Button>(R.id.btn_export_data)
        val restoreBtn = view.findViewById<Button>(R.id.btn_import_data)

        prefs = requireContext().getSharedPreferences("finance_tracker_prefs", Context.MODE_PRIVATE)

        val savedBudget = prefs.getFloat("monthly_budget", 0f)
        if (savedBudget > 0f) {
            budgetInput.setText(savedBudget.toString())
        }

        saveBtn.setOnClickListener {
            val budgetValue = budgetInput.text.toString().toFloatOrNull()
            if (budgetValue == null) {
                Toast.makeText(requireContext(), "Please enter a budget", Toast.LENGTH_SHORT).show()
            } else if (budgetValue < 0) {
                Toast.makeText(requireContext(), "Budget can not be negative", Toast.LENGTH_SHORT).show()
            } else {
                prefs.edit().putFloat("monthly_budget", budgetValue).apply()
                Toast.makeText(requireContext(), "Budget saved", Toast.LENGTH_SHORT).show()
            }
        }

        backupBtn.setOnClickListener {
            val budgetValue = prefs.getFloat("monthly_budget", 0f)
            val transactionsJson = prefs.getString("transactions", "[]")

            val json = JSONObject()
            json.put("monthly_budget", budgetValue)
            json.put("transactions", JSONArray(transactionsJson))

            val file = File(requireContext().filesDir, "transaction_backup.json")
            file.writeText(json.toString())

            Toast.makeText(requireContext(), "Backup Saved", Toast.LENGTH_SHORT).show()
        }

        restoreBtn.setOnClickListener {
            try {
                val file = File(requireContext().filesDir, "transaction_backup.json")
                if (file.exists()) {
                    val jsonString = file.readText()

                    val json = JSONObject(jsonString)
                    val restoredBudget = json.getDouble("monthly_budget").toFloat()
                    val restoredTransactions = json.getJSONArray("transactions").toString()

                    prefs.edit()
                        .putFloat("monthly_budget", restoredBudget)
                        .putString("transactions", restoredTransactions)
                        .apply()

                    budgetInput?.setText(restoredBudget.toString())

                    Toast.makeText(requireContext(), "Backup Restored", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No backup file found.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to restore backup: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        return view
    }
}
