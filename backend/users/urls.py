"""
URLs para las vistas de la aplicación de usuarios.
Define las rutas relacionadas con el registro, autenticación, y gestión de perfiles.
"""

from django.urls import path
from . import views

urlpatterns = [
    path('register', views.RegisterApiView.as_view(), name='RegisterApiView'),
    path('login', views.LoginView.as_view(), name='LoginView'),
    path('logingoogle', views.LoginGoogleView.as_view(), name='LoginGoogleView'),
    path('is-google', views.IsGoogleView.as_view(), name='IsGoogleView'),
    path('profile', views.ProfileView.as_view(), name='ProfileView'),
    path('profile/edit', views.EditProfileView.as_view(), name='EditProfileView'),
    path('profile/delete', views.DeleteProfileView.as_view(), name='DeleteProfileView'),
    path('profile/change-password', views.ChangePasswordView.as_view(), name='ChangePasswordView'),
    path('is_admin', views.IsAdminView.as_view(), name='is_admin'),
    path('check-if-user-is-admin', views.CheckIfUserIsAdminView.as_view(), name='CheckIfUserIsAdminView'),
    path('list', views.UserListView.as_view(), name='UserListView'),
    path('make-admin', views.MakeAdminView.as_view(), name='MakeAdminView'),
    path('remove-admin', views.RemoveAdminView.as_view(), name='remove-admin'),
    path('block-user', views.BlockUserView.as_view(), name='BlockUserView'),
    path('list-blocked-users', views.ListBlockedUsersView.as_view(), name='ListBlockedUsersView'),
    path('unblock-user', views.UnblockUserView.as_view(), name='UnblockUserView'),
    path('check-user-blocked', views.CheckUserBlockedView.as_view(), name='CheckUserBlockedView'),
    path('check-user-blocked-by-email', views.CheckUserBlockedByEmailView.as_view(),
         name='CheckUserBlockedByEmailView'),
    path('password-reset', views.PasswordResetRequestView.as_view(), name='PasswordResetRequestView'),
    path('password-reset-confirm/<uid>/<token>',views.PasswordResetConfirmView.as_view(),
        name='password-reset-confirm'),
    path('password-reset-success', views.PasswordResetSuccessView.as_view(), name='password_reset_success'),
    path('delete-user-admin/<int:id>', views.DeleteUserAdmin.as_view(), name='DeleteUserAdmin'),
]
