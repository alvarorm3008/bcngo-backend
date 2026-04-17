package com.example.bcngo.viewmodels

import com.example.bcngo.screens.manualItinerary.SelectedPointsViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SelectedPointsViewModelTest {

    private lateinit var viewModel: SelectedPointsViewModel

    @Before
    fun setUp() {
        // Inicializamos el ViewModel antes de cada prueba
        viewModel = SelectedPointsViewModel()
    }

    @Test
    fun `addPoint adds a new point if not already selected`() {
        // Act
        viewModel.addPoint(1)

        // Assert
        assertEquals(listOf(1), viewModel.selectedPoints)
    }

    @Test
    fun `addPoint does not add a duplicate point`() {
        // Arrange
        viewModel.addPoint(1)

        // Act
        viewModel.addPoint(1)

        // Assert
        assertEquals(listOf(1), viewModel.selectedPoints)
    }

    @Test
    fun `removePoint removes a point if it exists`() {
        // Arrange
        viewModel.addPoint(1)
        viewModel.addPoint(2)

        // Act
        viewModel.removePoint(1)

        // Assert
        assertEquals(listOf(2), viewModel.selectedPoints)
    }

    @Test
    fun `removePoint does nothing if the point does not exist`() {
        // Arrange
        viewModel.addPoint(1)

        // Act
        viewModel.removePoint(2)

        // Assert
        assertEquals(listOf(1), viewModel.selectedPoints)
    }

    @Test
    fun `clearPoints removes all points`() {
        // Arrange
        viewModel.addPoint(1)
        viewModel.addPoint(2)

        // Act
        viewModel.clearPoints()

        // Assert
        assertEquals(emptyList<Int>(), viewModel.selectedPoints)
    }
}
