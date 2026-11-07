package com.remindme.app.utils

object AppConstants {
    val GENDER_LABELS = mapOf(
        "male" to "Male",
        "female" to "Female"
    )

    val RELATIONSHIP_LABELS = mapOf(
        "family" to Pair("Family", "Family"),
        "partner" to Pair("Partner", "Partner"),
        "friend" to Pair("Friend", "Friend"),
        "colleague" to Pair("Colleague", "Colleague"),
        "other" to Pair("Other", "Other")
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
