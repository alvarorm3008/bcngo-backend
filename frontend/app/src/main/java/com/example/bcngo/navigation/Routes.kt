package com.example.bcngo.navigation

sealed class Routes(val route: String) {
    object Welcome : Routes("welcome_screen")

    object LogIn : Routes("login_screen")

    object SignUp : Routes("signup_screen")

    object Home : Routes("home_screen")

    object CalendarItinerary : Routes("calendar_itinerary_screen")

    object ListIntPoints : Routes("list_int_points_screen")

    object OptionItinerary : Routes("option_itinerary_screen")

    object ListItineraries : Routes("list_itineraries_screen")

    object AuthomaticItinerary : Routes("category_selection_screen")

    object ManualItinerary : Routes("manual_itinerary_screen")

    object PreviewItinerary : Routes("preview_itinerary_screen")

    object ReviewForm : Routes("review_form_screen/{pointId}")

    object VirtualPassport : Routes("virtual_passport_screen")

    //pantalles perfil
    object EditProfile : Routes("edit_profile_screen")
    object ChangePassword : Routes("change-password")


    // pantalles admin
    object CreateInterestPoint : Routes("create_interest_point_screen")

    object HomeAdmin : Routes("home_admin_screen")

    object ListPointsAdmin : Routes("list_points_admin_screen")

    object ListUsersAdmin : Routes("list_users_admin_screen")

    object ListReviewAdmin : Routes("list_review_admin_screen")

    object ListMessagesAdmin : Routes("list_messages_admin_screen")

    object SearchEvents : Routes("search_events_screen")

    object DetailsPoint : Routes("details_point_screen/{id}")

    object FavouriteEvents : Routes("favourite_events_screen")

    object MyChats : Routes("my_chats_screen")



    object GroupChat : Routes("group_chat_screen/{chatId}/{eventName}") {
        fun createRoute(chatId: Int, eventName: String) = "group_chat_screen/$chatId/$eventName"
    }
}
