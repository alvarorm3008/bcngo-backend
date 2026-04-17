package com.example.bcngo.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.bcngo.screens.login.LogInViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LogInViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: LogInViewModel

    @Before
    fun setUp() {
        viewModel = LogInViewModel()
    }

    @Test
    fun `onUsernameEmailChange updates usernameemail LiveData`() {
        // Act
        viewModel.onUsernameEmailChange("test@example.com")

        // Assert
        assertEquals("test@example.com", viewModel.usernameemail.value)
    }

    @Test
    fun `onPasswordChange updates password LiveData`() {
        // Act
        viewModel.onPasswordChange("mypassword")

        // Assert
        assertEquals("mypassword", viewModel.password.value)
    }

    @Test
    fun `onSignClick updates navigateToSign LiveData to true`() {
        // Act
        viewModel.onSignClick()

        // Assert
        assertEquals(true, viewModel.navigateToSign.value)
    }

    @Test
    fun `onNavigationDone updates navigateToSign LiveData to false`() {
        // Arrange
        viewModel.onSignClick() // Set initial value to true

        // Act
        viewModel.onNavigationDone()

        // Assert
        assertEquals(false, viewModel.navigateToSign.value)
    }
}
