package com.example.remindme_mobile.utils

object AppConstants {
    val GENDER_LABELS = mapOf(
        "unspecified" to "Prefer not to say",
        "male" to "Male",
        "female" to "Female",
        "non_binary" to "Non-binary",
        "other" to "Other"
    )

    val RELATIONSHIP_LABELS = mapOf(
        "mother" to Pair("Mother", "👩‍👦"),
        "father" to Pair("Father", "👨‍👦"),
        "sister" to Pair("Sister", "👧"),
        "brother" to Pair("Brother", "👦"),
        "wife" to Pair("Wife", "💍"),
        "husband" to Pair("Husband", "💍"),
        "partner" to Pair("Partner", "❤️"),
        "daughter" to Pair("Daughter", "👧"),
        "son" to Pair("Son", "👦"),
        "friend" to Pair("Friend", "👋"),
        "colleague" to Pair("Colleague", "💼"),
        "other" to Pair("Other", "✨")
    )
}
