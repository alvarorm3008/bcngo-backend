package com.example.bcngo.exceptions

// Excepciones usuario
class UserNotFoundException(message: String) : Exception(message)

class InvalidEmailException(message: String) : Exception(message)

class EmailAlreadyExistsException(message: String) : Exception(message)

class InvalidUsernameException(message: String) : Exception(message)

class InvalidPasswordException(message: String) : Exception(message)

class AccountBlockedException(message: String) : Exception(message)

class UsernameAlreadyExistsException(message: String) : Exception(message)
