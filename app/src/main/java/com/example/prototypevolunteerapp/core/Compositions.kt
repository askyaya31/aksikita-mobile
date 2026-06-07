package com.example.prototypevolunteerapp.core

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

val LocalBackStack = compositionLocalOf<NavBackStack<NavKey>> {
    error("error: LocalBackStack not provided")
}
