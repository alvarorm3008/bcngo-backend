"""
Definición de las rutas URL para la aplicación de Puntos de Interés.

Incluye endpoints para listar, detallar y actualizar puntos de interés,
así como gestionar reseñas asociadas.
"""

from django.urls import path
from .views import (
    PointOfInterestApiView,
    PointOfInterestDetailApiView,
    ParksApiView,
    CreatePointOfInterestApiView,
    DeletePointOfInterestApiView,
    CreateReviewApiView,
    DeleteReviewApiView,
    ListReviewsByPointApiView,
    ReportReviewApiView,
    ListReportedReviewsApiView,
)

urlpatterns = [
    path(
        "",
        PointOfInterestApiView.as_view(),
        name="pointsofinterest-list",
    ),
    path(
        "<int:point_of_interest_id>/",
        PointOfInterestDetailApiView.as_view(),
        name="pointsofinterest-detail",
    ),
    path(
        "parks/",
        ParksApiView.as_view(),
        name="parks-list",
    ),
    path(
        "create/",
        CreatePointOfInterestApiView.as_view(),
        name="pointsofinterest-create",
    ),
    path(
        "delete/<int:point_of_interest_id>/",
        DeletePointOfInterestApiView.as_view(),
        name="pointsofinterest-delete",
    ),
    # Endpoints para reseñas
    path(
        "reviews/create/",
        CreateReviewApiView.as_view(),
        name="review-create",
    ),
    path(
        "reviews/delete/<int:review_id>/",
        DeleteReviewApiView.as_view(),
        name="review-delete",
    ),
    path(
        "reviews/<int:point_of_interest_id>/",
        ListReviewsByPointApiView.as_view(),
        name="reviews-list-by-point",
    ),
    path(
        "reviews/report/<int:review_id>/",
        ReportReviewApiView.as_view(),
        name="review-report",
    ),
    path(
        "reviews/reported/",
        ListReportedReviewsApiView.as_view(),
        name="reviews-reported-list",
    ),
]
