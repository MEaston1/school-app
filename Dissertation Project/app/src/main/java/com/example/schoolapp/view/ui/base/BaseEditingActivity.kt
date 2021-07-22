package com.example.schoolapp.view.ui.base

import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.EditText
import com.shagi.materialdatepicker.date.DatePickerFragmentDialog
import com.yarolegovich.lovelydialog.LovelyChoiceDialog
import com.example.schoolapp.R
import java.io.File

abstract class BaseEditingActivity : BaseActivity() {
    protected fun validate(vararg editTexts: EditText): Boolean {
        if ( editTexts[0].text == null ||  editTexts[0].text.toString().isEmpty()) {
            editTexts[0].error = "This field is Required Please!"
            return false
        }
        if (editTexts[1].text == null || editTexts[1].text.toString().isEmpty()) {
            editTexts[1].error = "This field is Required Please!"
            return false
        }
        if (editTexts[2].text == null || editTexts[2].text.toString().isEmpty()) {
            editTexts[2].error = "This field is Required Please!"
            return false
        }
        return true
    }

    protected fun clearEditTexts(vararg editTexts: EditText) {
        for (editText in editTexts) {
            editText.setText("")
        }
    }
    protected fun valOf(editText : EditText): String {
        return editText.text.toString()
    }

    protected fun selectDate(dateTxt: EditText) {
        dateTxt.setOnClickListener {
            val dialog =
                DatePickerFragmentDialog.newInstance { view: DatePickerFragmentDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val month: String
                    val monthOfYear1 = monthOfYear+1
                    month = if (monthOfYear1 < 10) {
                        "0$monthOfYear1"
                    } else {
                        monthOfYear1.toString()
                    }
                    val day: String = if (dayOfMonth < 10) {
                        "0$dayOfMonth"
                    } else {
                        dayOfMonth.toString()
                    }
                    dateTxt.setText("$year-$month-$day")
                }
            dialog.show(supportFragmentManager, "DATE_PICKER")
        }
    }

    protected fun selectCountry(countryTxt: EditText) {
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.countries)
        )
        LovelyChoiceDialog(this)
            .setTopColorRes(R.color.colorPrimary)
            .setTitle("Country Picker")
            .setTitleGravity(Gravity.CENTER_HORIZONTAL)
            .setIcon(R.drawable.m_star)
            .setMessage("Select the Country for this News.")
            .setMessageGravity(Gravity.CENTER_HORIZONTAL)
            .setItems(adapter) { _: Int, item: String? ->
                countryTxt.setText(item)
            }
            .show()
    }

}