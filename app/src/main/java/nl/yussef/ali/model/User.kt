package nl.yussef.ali.model

import java.io.Serializable

data class User(
    val username: String,
    val password: String
) : Serializable

data class UserResponse(
    val username: String?,
    val AuthToken: String?
)

data class UserResponseRegister(
    val Success: Boolean?,
    val Message: String?
)