package com.example.tourcostingcalculator

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text

class MainActivity : AppCompatActivity() {

    private lateinit var btnAccommodation: Button
    private lateinit var btnTransportation: Button
    private lateinit var btnMeals: Button
    private lateinit var edtPax: EditText
    private lateinit var tvTotalCost: TextView
    private lateinit var tvCostPerPax: TextView
    private lateinit var tvProfit: TextView
    private lateinit var btnUpdate: Button
    private lateinit var tvSalesPrice: TextView

    private var totalCost = 0.0
    private var costPerPax = 0.0
    private var accommodationItems: List<AccommodationActivity.AccommodationItem> = emptyList()
    private var accommodationTotalCost: Double = 0.0
    private var transportationItems: List<TransportationActivity.TransportationItem> = emptyList()
    private var transportationTotalCost: Double = 0.0
    private var mealsItems: List<MealsActivity.MealItem> = emptyList()
    private var mealsTotalCost: Double = 0.0

    private val accommodationRequestCode = 1
    private val transportationRequestCode = 2
    private val mealsRequestCode = 3

    private lateinit var accommodationLauncher: ActivityResultLauncher<Intent>
    private lateinit var transportationLauncher: ActivityResultLauncher<Intent>
    private lateinit var mealsLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAccommodation = findViewById(R.id.btn_accommodation)
        btnTransportation = findViewById(R.id.btn_transportation)
        btnMeals = findViewById(R.id.btn_meals)
        edtPax = findViewById(R.id.edt_pax)
        tvTotalCost = findViewById(R.id.tv_total_cost)
        tvCostPerPax = findViewById(R.id.tv_cost_per_pax)
        tvProfit = findViewById(R.id.tv_profit)
        btnUpdate = findViewById(R.id.btn_update)
        tvSalesPrice = findViewById(R.id.tv_sales_price)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Use Activity Result API for Android 12 and above
            accommodationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    val items: List<AccommodationActivity.AccommodationItem> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        data?.getParcelableArrayListExtra("items", AccommodationActivity.AccommodationItem::class.java) ?: emptyList()
                    } else {
                        @Suppress("DEPRECATION")
                        data?.getParcelableArrayListExtra("items") ?: emptyList()
                    }
                    val totalCost = data?.getDoubleExtra("totalCost", 0.0) ?: 0.0
                    accommodationItems = items
                    accommodationTotalCost = totalCost
                    updateTotalCost()
                }
            }

            transportationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    val items: List<TransportationActivity.TransportationItem> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        data?.getParcelableArrayListExtra("items", TransportationActivity.TransportationItem::class.java) ?: emptyList()
                    } else {
                        @Suppress("DEPRECATION")
                        data?.getParcelableArrayListExtra("items") ?: emptyList()
                    }
                    val totalCost = data?.getDoubleExtra("totalCost", 0.0) ?: 0.0
                    transportationItems = items
                    transportationTotalCost = totalCost
                    updateTotalCost()
                }
            }

            mealsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    val items: List<MealsActivity.MealItem> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        data?.getParcelableArrayListExtra("items", MealsActivity.MealItem::class.java) ?: emptyList()
                    } else {
                        @Suppress("DEPRECATION")
                        data?.getParcelableArrayListExtra("items") ?: emptyList()
                    }
                    val totalCost = data?.getDoubleExtra("totalCost", 0.0) ?: 0.0
                    mealsItems = items
                    mealsTotalCost = totalCost
                    updateTotalCost()
                }
            }
        }

        btnAccommodation.setOnClickListener {
            val intent = Intent(this, AccommodationActivity::class.java)
            intent.putParcelableArrayListExtra("items", ArrayList(accommodationItems))
            intent.putExtra("totalCost", accommodationTotalCost)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                accommodationLauncher.launch(intent)
            } else {
                @Suppress("DEPRECATION")
                startActivityForResult(intent, accommodationRequestCode)
            }
        }

        btnTransportation.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                transportationLauncher.launch(Intent(this, TransportationActivity::class.java))
            } else {
                @Suppress("DEPRECATION")
                startActivityForResult(Intent(this, TransportationActivity::class.java), transportationRequestCode)
            }
        }

        btnMeals.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mealsLauncher.launch(Intent(this, MealsActivity::class.java))
            } else {
                @Suppress("DEPRECATION")
                startActivityForResult(Intent(this, MealsActivity::class.java), mealsRequestCode)
            }
        }
        btnUpdate.setOnClickListener {
            updateTotalCost()
            updateSalesPrice()
        }
        updateTotalCost()
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            if (resultCode == RESULT_OK) {
                when (requestCode) {
                    accommodationRequestCode -> {
                        val items = data?.getParcelableArrayListExtra<AccommodationActivity.AccommodationItem>("items") ?: emptyList()
                        val totalCost = data?.getDoubleExtra("totalCost", 0.0) ?: 0.0
                        accommodationItems = items
                        accommodationTotalCost = totalCost
                    }
                    transportationRequestCode -> {
                        val items = data?.getParcelableArrayListExtra<TransportationActivity.TransportationItem>("items") ?: emptyList()
                        val totalCost = data?.getDoubleExtra("totalCost", 0.0) ?: 0.0
                        transportationItems = items
                        transportationTotalCost = totalCost
                    }
                    mealsRequestCode -> {
                        val items = data?.getParcelableArrayListExtra<MealsActivity.MealItem>("items") ?: emptyList()
                        val totalCost = data?.getDoubleExtra("totalCost", 0.0) ?: 0.0
                        mealsItems = items
                        mealsTotalCost = totalCost
                    }
                }
                updateTotalCost()
            }
        }
    }

    private fun updateTotalCost() {
        totalCost = accommodationTotalCost + transportationTotalCost + mealsTotalCost
        tvTotalCost.text = getString(R.string.total_cost_template, totalCost)
        updateCostPerPax()
        updateSalesPrice()
        updateProfit()
    }

    private fun updateCostPerPax() {
        val paxString = edtPax.text.toString()
        val pax = paxString.toIntOrNull()
        costPerPax = if (pax != null && pax > 0) {
            totalCost / pax
        } else {
            0.0
        }
        tvCostPerPax.text = getString(R.string.cost_per_pax_template, costPerPax)
    }

    private fun updateSalesPrice() {
        val salesPrice10 = costPerPax * 1.10
        val salesPrice20 = costPerPax * 1.20
        val salesPrice30 = costPerPax * 1.30
        val salesPriceText = getString(R.string.sales_price_template, salesPrice10) + "\n" + getString(R.string.sales_price_template_2, salesPrice20) + "\n" + getString(R.string.sales_price_template_3, salesPrice30)
        tvSalesPrice.text = salesPriceText
    }

    private fun updateProfit() {
        val profit10 = totalCost * 0.10
        val profit20 = totalCost * 0.20
        val profit30 = totalCost * 0.30
        val profitText = getString(R.string.profit_per_pax_template, profit10) + "\n" + getString(R.string.profit_per_pax_template_2, profit20) + "\n" + getString(R.string.profit_per_pax_template_3, profit30)
        tvProfit.text = profitText
    }
}
