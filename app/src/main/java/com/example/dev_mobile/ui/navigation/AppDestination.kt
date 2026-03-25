package com.example.dev_mobile.ui.navigation

sealed class AppDestination(val route: String, val label: String, val icon: String) {
    object Dashboard      : AppDestination("dashboard",      "Dashboard",       "🏠")
    object Festivals      : AppDestination("festivals",      "Festivals",       "📅")
    object Reservants     : AppDestination("reservants",     "Reservants",      "👥")
    object Reservations   : AppDestination("reservations",   "Reservations",    "📋")
    object JeuxEditeurs   : AppDestination("jeux_editeurs",  "Jeux & Editeurs", "🎮")
    object Zones          : AppDestination("zones",          "Zones",           "🗺️")
    object Facturation    : AppDestination("facturation",    "Facturation",     "💰")
    object Recapitulatif  : AppDestination("recapitulatif",  "Recapitulatif",   "📊")
    object Administration : AppDestination("administration", "Administration",  "⚙️")
    object VuesPubliques  : AppDestination("vues_publiques", "Vues publiques",  "👁️")
}