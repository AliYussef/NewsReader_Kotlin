package nl.yussef.ali.repository

import nl.yussef.ali.api.RetrofitInstance
import nl.yussef.ali.model.User

class UserRepository {

    suspend fun login(user: User) = RetrofitInstance.api.login(user)

    suspend fun createUser(user: User) = RetrofitInstance.api.createUser(user)
}