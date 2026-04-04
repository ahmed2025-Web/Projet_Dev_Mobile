
package com.example.dev_mobile.ui.navigation

import com.example.dev_mobile.session.UserSession

object MenuConfig {
    fun getMenuItems(): List<AppDestination> = when (UserSession.role) {
        "admin" -> listOf(
            AppDestination.Dashboard,
            AppDestination.Festivals,
            AppDestination.Reservants,
            AppDestination.Reservations,
            AppDestination.JeuxEditeurs,
            AppDestination.Zones,
            AppDestination.Administration,
            AppDestination.VuesPubliques
        )
        "super organisateur" -> listOf(
            AppDestination.Dashboard,
            AppDestination.Festivals,
            AppDestination.Reservants,
            AppDestination.Reservations,
            AppDestination.JeuxEditeurs,
            AppDestination.Zones,
            AppDestination.VuesPubliques
        )
        "organisateur" -> listOf(
            AppDestination.Dashboard,
            AppDestination.Festivals,
            AppDestination.JeuxEditeurs,
            AppDestination.Zones,
            AppDestination.VuesPubliques
        )
        "benevole", "visiteur" -> listOf(
            AppDestination.VuesPubliques
        )
        else -> emptyList()
    }
}