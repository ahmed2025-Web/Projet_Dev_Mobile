package com.example.dev_mobile.ui.navigation

sealed class AppDestination(val route: String, val label: String, val icon: String) {
    object Dashboard      : AppDestination("dashboard",      "Dashboard",       "🏠")
    object Festivals      : AppDestination("festivals",      "Festivals",       "📅")
    object Reservants     : AppDestination("reservants",     "Réservants",      "👥")
    object Reservations   : AppDestination("reservations",   "Réservations",    "📋")
    object JeuxEditeurs   : AppDestination("jeux_editeurs",  "Jeux & Éditeurs", "🎮")
    object Zones          : AppDestination("zones",          "Zones",           "🗺️")
    object Facturation    : AppDestination("facturation",    "Facturation",     "💰")
    object Recapitulatif  : AppDestination("recapitulatif",  "Récapitulatif",   "📊")
    object Administration : AppDestination("administration", "Administration",  "⚙️")
    object VuesPubliques  : AppDestination("vues_publiques", "Vues publiques",  "👁️")
}