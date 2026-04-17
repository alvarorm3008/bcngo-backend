package com.example.bcngo.navigation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bcngo.model.InterestPoint
import com.example.bcngo.network.ApiService.getTokenFromSharedPreferences
import com.example.bcngo.network.ApiService.isAdminApi
import com.example.bcngo.screens.*
import com.example.bcngo.screens.admin.CreateInterestPoint
import com.example.bcngo.screens.admin.HomeAdmin
import com.example.bcngo.screens.admin.ListPointsAdmin
import com.example.bcngo.screens.admin.ListReviewAdmin
import com.example.bcngo.screens.admin.ListUsersAdmin
import com.example.bcngo.screens.admin.ListMessagesAdmin
import com.example.bcngo.screens.calendarItinerary.CalendarItinerary
import com.example.bcngo.screens.login.LogIn
import com.example.bcngo.screens.manualItinerary.ManualItinerary
import com.example.bcngo.screens.previewItinerary.PreviewItinerary
import com.example.bcngo.screens.searchpoints.ListIntPoints
import com.example.bcngo.screens.signup.SignUp
import com.example.bcngo.utils.AuthManager
import com.example.bcngo.screens.calendarItinerary.InfoItineraryViewModel
import com.example.bcngo.screens.chats.GroupChat
import com.example.bcngo.screens.chats.MyChats
import com.example.bcngo.screens.detailsPoint.DetailsPoint
import com.example.bcngo.screens.home.Home
import com.example.bcngo.screens.home.PointsViewModel
import com.example.bcngo.screens.home.PointViewModel
import com.example.bcngo.screens.home.TransportViewModel
import com.example.bcngo.screens.login.isAdmin
import com.example.bcngo.screens.previewItinerary.PreviewItineraryViewModel
import com.example.bcngo.screens.manualItinerary.SelectedPointsViewModel
import com.example.bcngo.screens.profile.ChangePassword
import com.example.bcngo.screens.profile.EditProfile
import com.example.bcngo.screens.review.ReviewForm
import com.example.bcngo.screens.searchEvents.SearchEvents
import com.example.bcngo.screens.detailsPoint.DetailsPointViewModel
import com.example.bcngo.screens.passport.VirtualPassport
import com.example.bcngo.screens.searchEvents.FavouriteEvents
import com.example.bcngo.screens.searchEvents.InfoEvent

@Composable
fun Navigation(
    context: Context,
    navController: NavHostController = rememberNavController(),
) {
    val authManager: AuthManager = AuthManager(context)

    val pointViewModel: PointViewModel = viewModel()

    val selectedPointsViewModel: SelectedPointsViewModel = viewModel()
    val infoItineraryViewModel: InfoItineraryViewModel = viewModel()
    val previewItineraryViewModel: PreviewItineraryViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val pointsViewModel: PointsViewModel = viewModel()
    val detailsPointViewModel: DetailsPointViewModel = viewModel()
    val transportViewModel: TransportViewModel = viewModel()
    pointsViewModel.getAllPointsFromApi()



    // Determina la pantalla inicial
    val startDestination = remember { mutableStateOf<String>(Routes.Welcome.route) }


    // Lógica para determinar la pantalla inicial
    LaunchedEffect(Unit) {
        if (isLogInUser(context)) {
            val isAdminUser = isAdmin(context) // Llamada a la API
            startDestination.value = if (isAdminUser) {
                Routes.HomeAdmin.route
            } else {
                Routes.Home.route
            }
        } else {
            startDestination.value = Routes.Welcome.route
        }
    }


    NavHost(
        navController = navController,
        // Empieza la aplicación en la pantalla de login si no hay usuario logueado, sino en la pantalla de Home

        startDestination = startDestination.value,

        ) {
        composable(Routes.Welcome.route) {
            Welcome(navController = navController)
        }
        composable(Routes.LogIn.route) {
            LogIn(
                navController = navController,
            )
        }
        composable(Routes.SignUp.route) {
            SignUp(
                navController = navController,
                auth = authManager,
            )
        }
        composable(Routes.Home.route) {
            Home(
                navController = navController,
                pointViewModel = pointViewModel,
            )
        }
        composable(Routes.CalendarItinerary.route) {
            CalendarItinerary(
                navController = navController,
                infoItineraryViewModel = infoItineraryViewModel,
            )
        }
        composable(Routes.ListIntPoints.route) {
            ListIntPoints(
                navController = navController,
            )
        }
        composable(Routes.OptionItinerary.route) {
            OptionItinerary(
                navController = navController,
                auth = authManager,
            )
        }
        composable(Routes.ManualItinerary.route) {
            ManualItinerary(
                navController = navController,
                selectedPointsViewModel = selectedPointsViewModel,
                infoItineraryViewModel = infoItineraryViewModel,
                previewItineraryViewModel = previewItineraryViewModel,
            )
        }
        composable(Routes.ListItineraries.route) {
            ListItineraries(
                navController = navController,
            )
        }
        composable(Routes.PreviewItinerary.route) {
            PreviewItinerary(
                navController = navController,
                auth = authManager,
                selectedPointsViewModel = selectedPointsViewModel,
                previewItineraryViewModel = previewItineraryViewModel,
            )
        }
        composable(Routes.AuthomaticItinerary.route) {
            AuthomaticItinerary(
                navController = navController,
                infoItineraryViewModel = infoItineraryViewModel,
                previewItineraryViewModel = previewItineraryViewModel,
                )
        }
        composable(Routes.EditProfile.route) {
            EditProfile(
                navController = navController,
            )
        }
        composable(Routes.ChangePassword.route) {
            ChangePassword(
                navController = navController,
            )
        }

        // Pantallas de administrador
        composable(Routes.HomeAdmin.route) {
            HomeAdmin(
                navController = navController,
                auth = authManager,
            )
        }
        composable(Routes.ListPointsAdmin.route) {
            ListPointsAdmin(
                navController = navController,
                auth = authManager,
            )
        }
        composable(Routes.ListUsersAdmin.route) {
            ListUsersAdmin(
                navController = navController,
            )
        }
        composable(Routes.ListReviewAdmin.route) {
            ListReviewAdmin(
                navController = navController,
                )
        }
        composable(Routes.ListMessagesAdmin.route) {
            ListMessagesAdmin(
                navController = navController,
            )
        }
        composable(Routes.CreateInterestPoint.route) {
            CreateInterestPoint(
                navController = navController,
                auth = authManager,
            )
        }

        composable(Routes.VirtualPassport.route) {
            VirtualPassport(
                navController = navController,
            )
        }

        composable(Routes.SearchEvents.route) {
            SearchEvents(
                navController = navController,
            )
        }
        composable(
            route = "infoEvent/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId") ?: -1
            InfoEvent(navController, eventId)
        }
        composable(Routes.FavouriteEvents.route) {
            FavouriteEvents(
                navController = navController,
            )
        }
        composable(
            route = Routes.ReviewForm.route,
            arguments = listOf(navArgument("pointId") { type = NavType.IntType })
        ) { backStackEntry ->
            val pointId = backStackEntry.arguments?.getInt("pointId") ?: -1

            ReviewForm(
                navController = navController,
                pointId = pointId
            )
        }
        composable(
            route = Routes.DetailsPoint.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: -1
            DetailsPoint(
                navController = navController,
                detailsPointViewModel = detailsPointViewModel,
                pointId = id
            )

        }
        composable(Routes.MyChats.route) {
            MyChats(
                navController = navController,
            )
        }
        composable(
            route = "group_chat_screen/{chatId}/{eventName}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.IntType },
                navArgument("eventName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getInt("chatId") ?: -1
            val eventName = backStackEntry.arguments?.getString("eventName") ?: ""
            GroupChat(
                navController = navController,
                chatId = chatId,
                event_name = eventName,
            )
        }

    }
}

fun isLogInUser(context: Context): Boolean {
    val token = getTokenFromSharedPreferences(context)
    return !token.isNullOrEmpty()
}

suspend fun isAdmin(context: Context): Boolean {
    return try {
        isAdminApi(context) // Llamada a la API
    } catch (e: Exception) {
        Log.e("Navigation", "Error checking admin status: ${e.localizedMessage}")
        false
    }
}
