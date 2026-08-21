package com.safeher.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalPolice
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object PermissionsGuide : Screen("permissions_guide")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")
    data object Home : Screen("home")
    data object SosActive : Screen("sos_active/{incidentId}") {
        fun createRoute(incidentId: String) = "sos_active/$incidentId"
    }
    data object Contacts : Screen("contacts")
    data object LiveLocation : Screen("live_location")
    data object SafetyTimer : Screen("safety_timer")
    data object Journey : Screen("journey")
    data object DiscreetMode : Screen("discreet_mode")
    data object NearbyHelp : Screen("nearby_help")
    data object Incidents : Screen("incidents")
    data object IncidentDetail : Screen("incident_detail/{incidentId}") {
        fun createRoute(incidentId: String) = "incident_detail/$incidentId"
    }
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Resources : Screen("resources")
}

data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Screen.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Safety", Screen.SafetyTimer.route, Icons.Filled.Shield, Icons.Outlined.Shield),
    BottomNavItem("Nearby", Screen.NearbyHelp.route, Icons.Filled.LocalPolice, Icons.Outlined.LocalPolice),
    BottomNavItem("Incidents", Screen.Incidents.route, Icons.Filled.History, Icons.Outlined.History),
    BottomNavItem("Profile", Screen.Profile.route, Icons.Filled.Person, Icons.Outlined.Person)
)
