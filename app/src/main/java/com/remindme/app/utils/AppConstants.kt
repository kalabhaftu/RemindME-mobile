package com.remindme.app.utils

object AppConstants {
    val GENDER_LABELS = mapOf(
        "male" to "Male",
        "female" to "Female",
        "nonbinary" to "Non-binary",
        "unspecified" to "—"
    )

    val RELATIONSHIP_LABELS = mapOf(
        "family" to Pair("Family", "👨‍👩‍👧"),
        "partner" to Pair("Partner", "💑"),
        "friend" to Pair("Friends", "👫"),
        "colleague" to Pair("Colleague", "💼"),
        "other" to Pair("Other", "👤"),
        // Additional relationships
        "mother" to Pair("Mother", "👩‍👦"),
        "father" to Pair("Father", "👨‍👦"),
        "sister" to Pair("Sister", "👧"),
        "brother" to Pair("Brother", "👦"),
        "wife" to Pair("Wife", "💍"),
        "husband" to Pair("Husband", "💍"),
        "daughter" to Pair("Daughter", "👧"),
        "son" to Pair("Son", "👦")
    )

    val ZODIAC_GLYPHS = mapOf(
        "Aries" to "♈",
        "Taurus" to "♉",
        "Gemini" to "♊",
        "Cancer" to "♋",
        "Leo" to "♌",
        "Virgo" to "♍",
        "Libra" to "♎",
        "Scorpio" to "♏",
        "Sagittarius" to "♐",
        "Capricorn" to "♑",
        "Aquarius" to "♒",
        "Pisces" to "♓"
    )
}
