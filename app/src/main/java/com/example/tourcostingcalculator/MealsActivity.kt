package com.example.tourcostingcalculator

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.parcelize.Parcelize

class MealsActivity : AppCompatActivity() {

    private lateinit var edtDescription: EditText
    private lateinit var edtUnit: EditText
    private lateinit var edtMeals: EditText
    private lateinit var edtPricePerUnit: EditText
    private lateinit var btnAddItem: Button
    private lateinit var btnReturn: Button
    private lateinit var itemsContainer: LinearLayout
    private lateinit var tvTotalCost: TextView

    private val items = mutableListOf<MealItem>()
    private var totalCost = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meals)

        edtDescription = findViewById(R.id.edt_description)
        edtUnit = findViewById(R.id.edt_unit)
        edtMeals = findViewById(R.id.edt_meals)
        edtPricePerUnit = findViewById(R.id.edt_price_per_unit)
        btnAddItem = findViewById(R.id.btn_add_item)
        btnReturn = findViewById(R.id.btn_return)
        itemsContainer = findViewById(R.id.items_container)
        tvTotalCost = findViewById(R.id.tv_total_cost)

        btnAddItem.setOnClickListener { addItem() }
        btnReturn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun addItem() {
        val description = edtDescription.text.toString()
        val unitString = edtUnit.text.toString()
        val mealsString = edtMeals.text.toString()
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

        val meals = mealsString.toIntOrNull()
        if (meals == null) {
            edtMeals.error = "Please enter a valid number"
            isValid = false
        } else {
            edtMeals.error = null // Clear any previous error
        }

        val pricePerUnit = pricePerUnitString.toDoubleOrNull()
        if (pricePerUnit == null) {
            edtPricePerUnit.error = "Please enter a valid number"
            isValid = false
        } else {
            edtPricePerUnit.error = null // Clear any previous error
        }

        if (!isValid) {
            return // Don't proceed if there are errors
        }

        val cost = unit!! * meals!! * pricePerUnit!!
        totalCost += cost
        items.add(MealItem(description, unit, meals, pricePerUnit, cost))

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("items", ArrayList(items))
        startActivity(intent)

        updateUI()
        clearInputs()
    }

    private fun updateUI() {
        itemsContainer.removeAllViews()
        for ((index, item) in items.withIndex()) {
            val itemView = layoutInflater.inflate(R.layout.item_meals, itemsContainer, false)

            val tvItemDescription: TextView = itemView.findViewById(R.id.tv_item_description)
            val btnDelete: Button = itemView.findViewById(R.id.btn_delete_item)
            val description = item.description
            val unit = item.unit
            val meals = item.meals
            val pricePerUnit = item.pricePerUnit
            val cost = item.cost

            val formattedDescription = getString(
                R.string.item_description_template,
                description,
                unit,
                meals,
                pricePerUnit,
                cost
            )
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
        edtMeals.text.clear()
        edtPricePerUnit.text.clear()
    }

    @Parcelize
    data class MealItem(
        val description: String,
        val unit: Int,
        val meals: Int,
        val pricePerUnit: Double,
        val cost: Double
    ) : Parcelable

}