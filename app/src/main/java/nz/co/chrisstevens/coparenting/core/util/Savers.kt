package nz.co.chrisstevens.coparenting.core.util

import androidx.compose.runtime.saveable.Saver

/** Lets any enum survive rotation via rememberSaveable without a bespoke Saver each time. */
inline fun <reified T : Enum<T>> enumSaver(): Saver<T, String> =
    Saver(save = { it.name }, restore = { enumValueOf<T>(it) })
