package com.co.component.tipcalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var etBillAmount: EditText
    private lateinit var seekBarTipPercentage: SeekBar
    private lateinit var tvTipPercentageLabel: TextView
    private lateinit var tvTipAmount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var switchRound: Switch
    private lateinit var etSplitBill: EditText
    private lateinit var llSplitFieldsContainer: LinearLayout
    private lateinit var tvTotalSplitAmountLabel: TextView
    private lateinit var tvTotalSplitAmount: TextView
    private lateinit var tvRemainingAmountLabel: TextView
    private lateinit var tvRemainingAmount: TextView
    private lateinit var spinnerCurrency: Spinner

    private var totalAmount: Double = 0.0
    private var numberOfPeople: Int = 1
    private var currencySymbol: String = "₹"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etBillAmount = findViewById(R.id.etBillAmount)
        seekBarTipPercentage = findViewById(R.id.sb_tip_percentage)
        tvTipPercentageLabel = findViewById(R.id.tv_tip_percentage_label)
        tvTipAmount = findViewById(R.id.tv_tip_amount)
        tvTotalAmount = findViewById(R.id.tv_total_amount)
        switchRound = findViewById(R.id.switchRound)
        etSplitBill = findViewById(R.id.et_split_bill)
        llSplitFieldsContainer = findViewById(R.id.ll_split_fields_container)
        tvTotalSplitAmountLabel = findViewById(R.id.tv_total_split_amount_label)
        tvTotalSplitAmount = findViewById(R.id.tv_total_split_amount)
        tvRemainingAmountLabel = findViewById(R.id.tv_remaining_amount_label)
        tvRemainingAmount = findViewById(R.id.tv_remaining_amount)
        spinnerCurrency = findViewById(R.id.sp_currency)

        // Initialize currency formatter based on default locale (Indian Rupee)
        //currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

        // Setup listeners and initial state
        setupCurrencySpinner()
        setupBillAmountTextWatcher()
        setupTipPercentageSeekBar()
        setupRoundSwitchListener()
        setupSplitBillTextWatcher()
    }

    private fun setupCurrencySpinner() {
        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currencySymbol = when (position) {
                    0 -> "₹" // Indian Rupee
                    1 -> "$"  // US Dollar
                    2 -> "€"  // Euro
                    else -> "₹" // Default to Indian Rupee
                }

                updateCurrencySigns()
                calculateTipAndTotal()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun updateCurrencySigns() {
        tvTipAmount.text = "${currencySymbol} 0.00"
        tvTotalAmount.text = "$currencySymbol 0.00"
        tvTotalSplitAmountLabel.text = "Total Amount Split:"
        tvTotalSplitAmount.text = "$currencySymbol 0.00"
        tvRemainingAmountLabel.text = "Remaining Amount to Split:"
        tvRemainingAmount.text = "$currencySymbol 0.00"
    }

    private fun setupBillAmountTextWatcher() {
        etBillAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val isEnabled = s?.isNotEmpty() ?: false
                switchRound.isEnabled = isEnabled
                etSplitBill.isEnabled = true
                calculateTipAndTotal()
            }
        })
    }

    private fun setupTipPercentageSeekBar() {
        seekBarTipPercentage.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvTipPercentageLabel.text = "Tip Percentage: $progress%"
                calculateTipAndTotal()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupRoundSwitchListener() {
        switchRound.setOnCheckedChangeListener { _, isChecked ->
            resetSplitBillUI()
            calculateTipAndTotal()
            updateSplitBillVisibility(isChecked)


        }
    }

    private fun setupSplitBillTextWatcher() {
        etSplitBill.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString() ?: ""
                numberOfPeople = if (input.isNotEmpty()) input.toInt() else 1
                calculateTipAndTotal()
                updateSplitFields(numberOfPeople)
            }
        })
    }

    private fun calculateTipAndTotal() {
        val billAmountString = etBillAmount.text.toString()
        if (billAmountString.isEmpty()) {
            resetUI()
            return
        }

        val billAmount = billAmountString.toDouble()
        val tipPercentage = seekBarTipPercentage.progress.toDouble() / 100.0
        val tipAmount = billAmount * tipPercentage
        totalAmount = billAmount + tipAmount

        val formattedTipAmount = "$tipAmount"
        val formattedTotalAmount = "$totalAmount"

        tvTipAmount.text = "$currencySymbol $formattedTipAmount"
        tvTotalAmount.text = "$currencySymbol $formattedTotalAmount"

        if (switchRound.isChecked) {
            updateSplitFields(numberOfPeople)
        }
        if(!switchRound.isChecked){
            updateSplitFields(numberOfPeople)
        }
    }

    private fun updateSplitFields(numberOfPeople: Int) {
        llSplitFieldsContainer.removeAllViews()
        if (numberOfPeople > 1) {
            if (switchRound.isChecked) {
                val splitAmount = totalAmount / numberOfPeople
                for (i in 1..numberOfPeople) {
                    val editText = EditText(this)
                    editText.hint = "Amount for Person $i"
                    editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    editText.setText("$splitAmount")
                    editText.isEnabled = false
                    llSplitFieldsContainer.addView(editText)
                }
                llSplitFieldsContainer.visibility = View.VISIBLE
                tvTotalSplitAmountLabel.visibility = View.VISIBLE
                tvTotalSplitAmount.visibility = View.VISIBLE
                tvRemainingAmountLabel.visibility = View.GONE
                tvRemainingAmount.visibility = View.GONE
                tvTotalSplitAmount.text = "$currencySymbol $totalAmount"
            } else {
                for (i in 1..numberOfPeople) {
                    val editText = EditText(this)
                    editText.hint = "Amount for Person $i"
                    editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    editText.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                        override fun afterTextChanged(s: Editable?) {
                            updateSplitAmounts()
                            tvRemainingAmount.text = totalAmount.toString()
                        }
                    })
                    llSplitFieldsContainer.addView(editText)
                }
                llSplitFieldsContainer.visibility = View.VISIBLE
                tvTotalSplitAmountLabel.visibility = View.VISIBLE
                tvTotalSplitAmount.visibility = View.VISIBLE
                tvRemainingAmountLabel.visibility = View.GONE
                tvRemainingAmount.visibility = View.GONE
            }
        } else {
            llSplitFieldsContainer.visibility = View.GONE
            tvTotalSplitAmountLabel.visibility = View.GONE
            tvTotalSplitAmount.visibility = View.GONE
            tvRemainingAmountLabel.visibility = View.GONE
            tvRemainingAmount.visibility = View.GONE
        }
    }

    private fun updateSplitAmounts() {
        var totalSplitAmount = 0.0

        for (i in 0 until llSplitFieldsContainer.childCount) {
            val editText = llSplitFieldsContainer.getChildAt(i) as EditText
            val splitAmount = editText.text.toString().toDoubleOrNull() ?: 0.0

            // Prevent entering amounts greater than the remaining amount
            val remainingAmount = totalAmount - totalSplitAmount
            if (splitAmount > remainingAmount) {
                editText.error = "Amount cannot exceed remaining amount (${remainingAmount})"
                editText.setText("")
                return
            }

            totalSplitAmount += splitAmount
        }

        val remainingAmount = totalAmount - totalSplitAmount
        val maxAvailableAmount = if (remainingAmount >= 0) remainingAmount else 0.0

        val formattedTotalSplitAmount = "$totalSplitAmount"
        tvTotalSplitAmount.text = currencySymbol+"$formattedTotalSplitAmount"

        val formattedRemainingAmount = "$maxAvailableAmount"
        tvRemainingAmount.text = "$formattedRemainingAmount"
    }

    private fun resetSplitBillUI() {
        llSplitFieldsContainer.removeAllViews()
        tvTotalSplitAmount.text = "$currencySymbol 0.00"
        tvRemainingAmount.text = "$currencySymbol 0.00"
    }

    private fun resetUI() {
        tvTipAmount.text = "$currencySymbol 0.00"
        tvTotalAmount.text = "$currencySymbol 0.00"
        resetSplitBillUI()
    }

    private fun updateSplitBillVisibility(isChecked: Boolean) {
        if (isChecked) {
            etSplitBill.isEnabled = true
            etSplitBill.visibility = View.VISIBLE
            llSplitFieldsContainer.visibility = View.VISIBLE
            tvTotalSplitAmountLabel.visibility = View.GONE
            tvTotalSplitAmount.visibility = View.GONE
            tvRemainingAmountLabel.visibility = View.GONE
            tvRemainingAmount.visibility = View.GONE
        } else {
            etSplitBill.isEnabled = true
            etSplitBill.visibility = View.VISIBLE
            llSplitFieldsContainer.visibility = View.VISIBLE
            tvTotalSplitAmountLabel.visibility = View.VISIBLE
            tvTotalSplitAmount.visibility = View.VISIBLE
            tvRemainingAmountLabel.visibility = View.GONE
            tvRemainingAmount.visibility = View.GONE
            updateSplitFields(numberOfPeople)
        }
    }
}
