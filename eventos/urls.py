"""
Módulo de URLs para la aplicación de eventos.
Define las rutas para acceder a los eventos culturales.
"""
from django.urls import path
from . import views

urlpatterns = [
    path('all', views.CulturalEventListView.as_view(), name='events'),
    path('current', views.CurrentEventsView.as_view(), name='current_events'),
    path('day/<str:day>', views.EventsByDayView.as_view(), name='events_by_day'),
    path('month', views.EventsOfMonthView.as_view(), name='events_of_month'),
    path('week', views.EventsOfWeekView.as_view(), name='events_of_week'),
    path('<int:id>', views.EventDetailView.as_view(), name='event_detail'),
    path('favorites', views.ListFavoriteEventsView.as_view(), name='favorite_events'),
    path('favorites/<int:id>', views.FavoriteEventView.as_view(), name='favorite_event'),
    path('chats/<int:chat_id>/is_participant', views.IsUserInGroupView.as_view(), name='is_participant'),
    path('chats/<int:event_id>/join', views.JoinEventChatView.as_view(), name='join_chat'),
    path('chats', views.UserChatsView.as_view(), name='user_chats'),
    path('chats/<int:chat_id>/messages', views.GetMessagesView.as_view(), name='chat_messages'),
    path('chats/<int:chat_id>/messages/create', views.SendMessageView.as_view(), name='create_message'),
    path('chats/<int:chat_id>/leave', views.LeaveEventChatView.as_view(), name='leave_chat'),
    path('chats/messages/<int:message_id>', views.DeleteMessageView.as_view(), name='delete_message'),
    path('chats/messages/<int:message_id>/report', views.ReportMessageView.as_view(), name='report_message'),
    path('messages/reported', views.ListReportedMessagesView.as_view(), name='reported_messages'),
    path('messages/<int:message_id>/delete-admin', views.DeleteAnyMessageView.as_view(), name='delete_message_admin'),
]
