package com.example.co_parenting_calendar.feature.activity.domain

/**
 * Just an identifier - what image it maps to is a UI concern (see ActivityIcons.kt in the ui
 * package), so this enum has no dependency on Compose/Android.
 */
enum class ActivityIconType {
    SCHOOL,
    FOOTBALL,
    RUGBY,
    BASKETBALL,
    SWIMMING,
    MUSIC,
    DANCE,
    BIRTHDAY,
    DOCTOR,
    DENTIST,
    HOLIDAY,
    TRAVEL,
    HOME,
    FAMILY,
    OTHER
}
