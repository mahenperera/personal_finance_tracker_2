package com.example.mad_lab_exam_3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ExpensesSummary : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_expenses_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(requireContext()).transactionDao()
            val expenses = dao.getByType("expense")

            val foodTotal = expenses.filter { it.category.equals("Food", true) }.sumOf { it.amount }
            val transportTotal = expenses.filter { it.category.equals("Transport", true) }.sumOf { it.amount }
            val billsTotal = expenses.filter { it.category.equals("Bills", true) }.sumOf { it.amount }
            val entertainmentTotal = expenses.filter { it.category.equals("Entertainment", true) }.sumOf { it.amount }
            val otherTotal = expenses.filter { it.category.equals("Other", true) }.sumOf { it.amount }

            val total = foodTotal + transportTotal + billsTotal + entertainmentTotal + otherTotal

            view.findViewById<TextView>(R.id.foodValue).text = "Rs. %.2f".format(foodTotal)
            view.findViewById<TextView>(R.id.TransportValue).text = "Rs. %.2f".format(transportTotal)
            view.findViewById<TextView>(R.id.BillsValue).text = "Rs. %.2f".format(billsTotal)
            view.findViewById<TextView>(R.id.EntertainmentValue).text = "Rs. %.2f".format(entertainmentTotal)
            view.findViewById<TextView>(R.id.OtherValue).text = "Rs. %.2f".format(otherTotal)
            view.findViewById<TextView>(R.id.TotalValue).text = "Rs. %.2f".format(total)
        }
    }
}
