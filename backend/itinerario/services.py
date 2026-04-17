"""
Este módulo contiene la lógica para seleccionar puntos de interés 
basados en categorías y criterios de rareza para un itinerario.
"""

import random
from puntosdeinteres.models import PointOfInterest

def select_points_for_itinerary(categories, days_of_stay, rarity):
    """
    Selecciona puntos de interés para un itinerario según las categorías, rareza y días de estancia.

    :param categories: Lista de categorías de puntos de interés.
    :param days_of_stay: Número de días de estancia.
    :param rarity: Rareza ("atypical", "typical", "very typical").
    :return: Lista de puntos seleccionados.
    """
    if days_of_stay <= 0:
        raise ValueError("El número de días de estancia debe ser mayor a 0.")

    total_points_needed = days_of_stay * 6

    # Obtener puntos esenciales y no esenciales según las categorías seleccionadas
    essential_points = list(PointOfInterest.objects.filter(
        category__in=categories, esencial=True))
    non_essential_points = list(PointOfInterest.objects.filter(
        category__in=categories, esencial=False))

    # Validar que haya puntos disponibles
    if not essential_points and not non_essential_points:
        raise ValueError("No hay puntos de interés disponibles para las categorías seleccionadas ni esenciales.")

    selected_points = []

    if rarity == "Atypical":
        # Selección completamente aleatoria dentro de las categorías seleccionadas
        all_points = essential_points + non_essential_points
        if len(all_points) < total_points_needed:
            selected_points = all_points  # Usar todos si no hay suficientes
        else:
            selected_points = random.sample(all_points, total_points_needed)

    elif rarity == "Typical":
        # 50% esenciales, 50% no esenciales
        essential_needed = total_points_needed // 2

        # Limitar los esenciales al 50% de los disponibles
        max_essential_to_select = len(essential_points) // 2
        essential_needed = min(essential_needed, max_essential_to_select)
        non_essential_needed = total_points_needed - essential_needed

        # Selección de esenciales
        selected_essential = (
            random.sample(essential_points, essential_needed)
            if len(essential_points) >= essential_needed
            else essential_points[:essential_needed]  # Seleccionar hasta el límite
        )

        essential_deficit = essential_needed - len(selected_essential)

        # Selección de no esenciales
        if essential_deficit > 0:
            # Si faltan esenciales, completar con no esenciales
            non_essential_needed += essential_deficit

        selected_non_essential = (
            random.sample(non_essential_points, non_essential_needed)
            if len(non_essential_points) >= non_essential_needed
            else non_essential_points  # Seleccionar todos los no esenciales disponibles
        )

        selected_points = selected_essential + selected_non_essential

    elif rarity == "Very typical":
        # Selección 100% esenciales
        if len(essential_points) < total_points_needed:
            # Si no hay suficientes esenciales, tomar todos los disponibles
            selected_points = essential_points
        else:
            # Seleccionar solo los necesarios de los esenciales
            selected_points = random.sample(essential_points, total_points_needed)

    else:
        raise ValueError("El valor de rareza debe ser 'Atypical', 'Typical' o 'Very typical'.")

    return selected_points
