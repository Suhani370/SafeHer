package com.safeher.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.safeher.app.presentation.auth.AuthViewModel
import com.safeher.app.presentation.auth.ForgotPasswordScreen
import com.safeher.app.presentation.auth.LoginScreen
import com.safeher.app.presentation.auth.RegisterScreen
import com.safeher.app.presentation.contacts.ContactsScreen
import com.safeher.app.presentation.contacts.ContactsViewModel
import com.safeher.app.presentation.discreet.DiscreetModeScreen
import com.safeher.app.presentation.home.HomeScreen
import com.safeher.app.presentation.home.HomeViewModel
import com.safeher.app.presentation.incidents.IncidentDetailScreen
import com.safeher.app.presentation.incidents.IncidentsScreen
import com.safeher.app.presentation.incidents.IncidentsViewModel
import com.safeher.app.presentation.journey.JourneyMonitoringScreen
import com.safeher.app.presentation.journey.JourneyViewModel
import com.safeher.app.presentation.nearby.NearbyHelpScreen
import com.safeher.app.presentation.nearby.NearbyViewModel
import com.safeher.app.presentation.onboarding.OnboardingScreen
import com.safeher.app.presentation.profile.ProfileScreen
import com.safeher.app.presentation.profile.ProfileViewModel
import com.safeher.app.presentation.resources.SafetyEducationScreen
import com.safeher.app.presentation.safetytimer.SafetyTimerScreen
import com.safeher.app.presentation.safetytimer.SafetyTimerViewModel
import com.safeher.app.presentation.settings.SettingsScreen
import com.safeher.app.presentation.settings.SettingsViewModel
import com.safeher.app.presentation.sos.SosActiveScreen
import com.safeher.app.presentation.sos.SosViewModel

@Composable
fun SafeHerNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.SafetyTimer.route,
        Screen.NearbyHelp.route,
        Screen.Incidents.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                SafeHerBottomBar(navController = navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinishOnboarding = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Register.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                ForgotPasswordScreen(
                    viewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToSosActive = { incidentId ->
                        navController.navigate(Screen.SosActive.createRoute(incidentId))
                    },
                    onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                    onNavigateToSafetyTimer = { navController.navigate(Screen.SafetyTimer.route) },
                    onNavigateToJourney = { navController.navigate(Screen.Journey.route) },
                    onNavigateToLiveLocation = { navController.navigate(Screen.Home.route) },
                    onNavigateToDiscreetMode = { navController.navigate(Screen.DiscreetMode.route) },
                    onNavigateToNearbyHelp = { navController.navigate(Screen.NearbyHelp.route) }
                )
            }

            composable(
                route = Screen.SosActive.route,
                arguments = listOf(navArgument("incidentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""
                val sosViewModel: SosViewModel = hiltViewModel()
                SosActiveScreen(
                    incidentId = incidentId,
                    viewModel = sosViewModel,
                    onSosFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Contacts.route) {
                val contactsViewModel: ContactsViewModel = hiltViewModel()
                ContactsScreen(
                    viewModel = contactsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SafetyTimer.route) {
                val timerViewModel: SafetyTimerViewModel = hiltViewModel()
                SafetyTimerScreen(
                    viewModel = timerViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSosActive = { incidentId ->
                        navController.navigate(Screen.SosActive.createRoute(incidentId))
                    }
                )
            }

            composable(Screen.Journey.route) {
                val journeyViewModel: JourneyViewModel = hiltViewModel()
                JourneyMonitoringScreen(
                    viewModel = journeyViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.DiscreetMode.route) {
                DiscreetModeScreen(
                    onExitDiscreetMode = { navController.popBackStack() }
                )
            }

            composable(Screen.NearbyHelp.route) {
                val nearbyViewModel: NearbyViewModel = hiltViewModel()
                NearbyHelpScreen(
                    viewModel = nearbyViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Incidents.route) {
                val incidentsViewModel: IncidentsViewModel = hiltViewModel()
                IncidentsScreen(
                    viewModel = incidentsViewModel,
                    onNavigateToDetail = { incId ->
                        navController.navigate(Screen.IncidentDetail.createRoute(incId))
                    }
                )
            }

            composable(
                route = Screen.IncidentDetail.route,
                arguments = listOf(navArgument("incidentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""
                val incidentsViewModel: IncidentsViewModel = hiltViewModel()
                IncidentDetailScreen(
                    incidentId = incidentId,
                    viewModel = incidentsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToResources = { navController.navigate(Screen.Resources.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Resources.route) {
                SafetyEducationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
