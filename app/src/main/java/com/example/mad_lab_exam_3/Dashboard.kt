package com.example.mad_lab_exam_3

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import sendBudgetNotification
import java.util.Calendar

class Dashboard : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = AppDatabase.getDatabase(requireContext()).transactionDao()

        lifecycleScope.launch {

            val allTransactions = dao.getAll()

            val recentTransactions = allTransactions.takeLast(3).reversed()

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = TransactionAdapter(
                recentTransactions,
                onEdit = { transaction: Transaction -> showEditTransactionDialog(transaction, dao) },
                onDelete = { transaction: Transaction -> confirmDeleteTransaction(transaction, dao) }
            )

            val fab = view.findViewById<FloatingActionButton>(R.id.floatingActionButton)
            fab.setOnClickListener {
                showAddTransactionDialog(dao)
            }

            val totalIncome = allTransactions.filter { it.type == "income" }.sumOf { it.amount }
            val totalExpense = allTransactions.filter { it.type == "expense" }.sumOf { it.amount }

            val prefs = requireContext().getSharedPreferences("finance_tracker_prefs", Context.MODE_PRIVATE)
            val budget = prefs.getFloat("monthly_budget", 0f)
            val remaining = budget - totalExpense

            view.findViewById<TextView>(R.id.totalIncomeValue)?.text = "Rs. %.2f".format(totalIncome)
            view.findViewById<TextView>(R.id.totalExpensesValue)?.text = "Rs. %.2f".format(totalExpense)
            view.findViewById<TextView>(R.id.remainingBudgetValue)?.text = "Rs. %.2f".format(remaining)

            if (remaining < 0) {
                Toast.makeText(requireContext(), "Warning: Budget exceeded!", Toast.LENGTH_LONG).show()
            } else if (remaining <= 1000) {
                Toast.makeText(requireContext(), "Warning: You have only Rs. 1000 budget left!", Toast.LENGTH_LONG).show()
            }

            val summaryLink = view.findViewById<TextView>(R.id.summaryLink)
            summaryLink.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, ExpensesSummary())
                    .addToBackStack(null)
                    .commit()
            }
        }

    }

    private fun showAddTransactionDialog(dao: TransactionDao) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_transaction, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)

        val etCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.etCategory)

        val categories = listOf("Food", "Transport", "Bills", "Entertainment", "Other")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(categoryAdapter)

        val etDate = dialogView.findViewById<EditText>(R.id.etDate)
        val rgType = dialogView.findViewById<RadioGroup>(R.id.rgType)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = etCategory.text.toString()
            val date = etDate.text.toString()

            if (title.isBlank()) {
                Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount == null) {
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (category.isBlank()) {
                Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (date.isBlank()) {
                Toast.makeText(requireContext(), "Please choose a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (rgType.checkedRadioButtonId == -1) {
                Toast.makeText(requireContext(), "Please select a transaction type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val type = if (rgType.checkedRadioButtonId == R.id.rbIncome) "income" else "expense"

            val transaction = Transaction(
                title = title,
                amount = amount,
                category = category,
                date = date,
                type = type
            )

            lifecycleScope.launch {
                dao.insert(transaction)

                if (type == "expense") {
                    val prefs = requireContext().getSharedPreferences("finance_tracker_prefs", Context.MODE_PRIVATE)
                    val budget = prefs.getFloat("monthly_budget", 0f)

                    val allExpenses = dao.getByType("expense")
                    val totalExpenses = allExpenses.sumOf { it.amount }

                    if (budget > 0) {
                        val percent = (totalExpenses / budget) * 100
                        if (percent >= 100) {
                            sendBudgetNotification(requireContext(), "You've exceeded your monthly budget!")
                        } else if (percent >= 80) {
                            sendBudgetNotification(requireContext(), "You're nearing your monthly budget.")
                        }
                    }
                }

                Toast.makeText(requireContext(), "Transaction added", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()

                refreshTransactionList(dao)
            }
        }

        alertDialog.show()
    }

    private fun confirmDeleteTransaction(transaction: Transaction, dao: TransactionDao) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    dao.delete(transaction)
                    Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                    refreshTransactionList(dao)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun refreshTransactionList(dao: TransactionDao) {
        lifecycleScope.launch {
            val updated = dao.getAll()
            setupRecyclerView(updated, dao)
        }
    }

    private fun setupRecyclerView(transactions: List<Transaction>, dao: TransactionDao) {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = TransactionAdapter(
            transactions,
            onEdit = { showEditTransactionDialog(it, dao) },
            onDelete = { confirmDeleteTransaction(it, dao) }
        )
    }

    private fun showEditTransactionDialog(transaction: Transaction, dao: TransactionDao) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_transaction, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val etDate = dialogView.findViewById<EditText>(R.id.etDate)
        val rgType = dialogView.findViewById<RadioGroup>(R.id.rgType)
        val rbIncome = dialogView.findViewById<RadioButton>(R.id.rbIncome)
        val rbExpense = dialogView.findViewById<RadioButton>(R.id.rbExpense)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        val etCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.etCategory)
        val categories = listOf("Food", "Transport", "Bills", "Entertainment", "Other")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(categoryAdapter)

        etTitle.setText(transaction.title)
        etAmount.setText(transaction.amount.toString())
        etCategory.setText(transaction.category, false)
        etDate.setText(transaction.date)
        if (transaction.type == "income") rbIncome.isChecked = true else rbExpense.isChecked = true

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = etCategory.text.toString()
            val date = etDate.text.toString()

            if (title.isBlank()) {
                Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount == null) {
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (category.isBlank()) {
                Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (date.isBlank()) {
                Toast.makeText(requireContext(), "Please choose a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (rgType.checkedRadioButtonId == -1) {
                Toast.makeText(
                    requireContext(),
                    "Please select a transaction type",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val type = if (rgType.checkedRadioButtonId == R.id.rbIncome) "income" else "expense"

            val updatedTransaction = Transaction(
                id = transaction.id,
                title = title,
                amount = amount,
                category = category,
                date = date,
                type = type
            )

            viewLifecycleOwner.lifecycleScope.launch {
                dao.update(updatedTransaction)
                Toast.makeText(requireContext(), "Transaction Updated", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
                refreshTransactionList(dao)
            }
        }

        alertDialog.show()
    }
}