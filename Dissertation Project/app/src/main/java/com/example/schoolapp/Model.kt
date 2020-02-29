package com.example.schoolapp

data class Feature(var title: String, val featurePhoto: Int)

object Supplier{
    val features = listOf<features>(
        Feature("Messages", R.drawable.letter),
        Feature("Absence", R.drawable.absence),
        Feature("Newsletter", R.drawable.medical_form),
        Feature("Consent Forms", R.drawable.consent),
        Feature("Calendar", R.drawable.calendar),
        Feature("Medical Forms", R.drawable.medical_forms),
        Feature("Information", R.drawable.consent),
        Feature("Logout", R.drawable.ic_launcher_background)
    )
}