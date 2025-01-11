package com.example.tourcostingcalculator

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.text
import kotlinx.parcelize.Parcelize

class AccommodationActivity : AppCompatActivity() {

    private lateinit var edtDescription: EditText
    private lateinit var edtUnit: EditText
    private lateinit var edtDay: EditText
    private lateinit var edtPricePerUnit: EditText
    private lateinit var btnAddItem: Button
    private lateinit var btnReturn: Button
    private lateinit var itemsContainer: LinearLayout
    private lateinit var tvTotalCost: TextView

    private val items = mutableListOf<AccommodationItem>()
    private var totalCost = 0.0

    companion object {
        private const val KEY_ACCOMMODATION_ITEMS = "accommodation_items"
        private const val KEY_TOTAL_COST = "total_cost"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accommodation)

        edtDescription = findViewById(R.id.edt_description)
        edtUnit = findViewById(R.id.edt_unit)
        edtDay = findViewById(R.id.edt_day)
        edtPricePerUnit = findViewById(R.id.edt_price_per_unit)
        btnAddItem = findViewById(R.id.btn_add_item)
        btnReturn = findViewById(R.id.btn_return)
        itemsContainer = findViewById(R.id.items_container)
        tvTotalCost = findViewById(R.id.tv_total_cost)

        // Restore state if available
        if (savedInstanceState != null) {
            val restoredItems: List<AccommodationItem> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getParcelableArrayList(KEY_ACCOMMODATION_ITEMS, AccommodationItem::class.java) ?: emptyList()
            } else {
                @Suppress("DEPRECATION")
                savedInstanceState.getParcelableArrayList(KEY_ACCOMMODATION_ITEMS) ?: emptyList()
            }
            items.addAll(restoredItems)
            totalCost = savedInstanceState.getDouble(KEY_TOTAL_COST, 0.0)
            updateUI()
        } else {
            // Get data from intent if available
            val intentItems: List<AccommodationItem> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra("items", AccommodationItem::class.java) ?: emptyList()
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra("items") ?: emptyList()
            }
            val intentTotalCost = intent.getDoubleExtra("totalCost", 0.0)
            items.addAll(intentItems)
            totalCost = intentTotalCost
            updateUI()
        }

        btnAddItem.setOnClickListener { addItem() }
        btnReturn.setOnClickListener { returnResult() }
        updateUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(KEY_ACCOMMODATION_ITEMS, ArrayList(items))
        outState.putDouble(KEY_TOTAL_COST, totalCost)
    }

    private fun addItem() {
        val description = edtDescription.text.toString()
        val unitString = edtUnit.text.toString()
        val dayString = edtDay.text.toString()
        val pricePerUnitString = edtPricePerUnit.text.toString()

        var isValid = true

        if (description.isEmpty()) {
            edtDescription.error = "Description cannot be empty"
            isValid = false
        }

        val unit = unitString.toIntOrNull()
        if (unit == null) {
            edtUnit.error = "Please enter a valid number"
            isValid = false
        } else {
            edtUnit.error = null // Clear any previous error
        }

        val day = dayString.toIntOrNull()
        if (day == null) {
            edtDay.error = "Please enter a valid number"
            isValid = false
        } else {
            edtDay.error = null // Clear any previous error
        }

        val pricePerUnit = pricePerUnitString.toDoubleOrNull()
        if (pricePerUnit == null) {
            edtPricePerUnit.error = "Please enter a valid number"
            isValid = false
        } else {
            edtPricePerUnit.error = null // Clear any previous error
        }

        if (!isValid) {
            return
        }

        val cost = unit!! * day!! * pricePerUnit!!
        totalCost += cost
        items.add(AccommodationItem(description, unit, day, pricePerUnit, cost))

        updateUI()
        clearInputs()
    }

    private fun updateUI() {
        itemsContainer.removeAllViews()
        for ((index, item) in items.withIndex()) {
            val itemView = layoutInflater.inflate(R.layout.item_accommodation, itemsContainer, false)

            val tvItemDescription: TextView = itemView.findViewById(R.id.tv_item_description)
            val btnDelete: Button = itemView.findViewById(R.id.btn_delete_item)
            val description = item.description
            val unit = item.unit
            val day = item.day
            val pricePerUnit = item.pricePerUnit
            val cost = item.cost

            val formattedDescription = getString(R.string.item_description_template, description, unit, day, pricePerUnit, cost)
            tvItemDescription.text = formattedDescription
            btnDelete.setOnClickListener { deleteItem(index) }

            itemsContainer.addView(itemView)
        }
        tvTotalCost.text = getString(R.string.total_cost_template, totalCost)
    }

    private fun deleteItem(index: Int) {
        totalCost -= items[index].cost
        items.removeAt(index)
        updateUI()
    }

    private fun clearInputs() {
        edtDescription.text.clear()
        edtUnit.text.clear()
        edtDay.text.clear()
        edtPricePerUnit.text.clear()
    }

    private fun returnResult() {
        val resultIntent = Intent()
        resultIntent.putParcelableArrayListExtra("items", ArrayList(items))
        resultIntent.putExtra("totalCost", totalCost)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    @Parcelize
    data class AccommodationItem(
        val description: String,
        val unit: Int,
        val day: Int,
        val pricePerUnit: Double,
        val cost: Double
    ) : Parcelable
}