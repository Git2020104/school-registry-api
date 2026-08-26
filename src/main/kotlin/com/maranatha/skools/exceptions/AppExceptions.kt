package com.maranatha.skools.exceptions

class UserAlreadyExistsException(val email: String) : RuntimeException("User with email '$email' already exists.")

class UserNotFoundException(val id: Int) : RuntimeException("User with ID '$id' was not found.")
