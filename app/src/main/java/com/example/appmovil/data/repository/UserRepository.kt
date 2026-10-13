package com.example.appmovil.data.repository

import com.example.appmovil.data.local.user.UserDao
import com.example.appmovil.data.local.user.UserEntity

// Patrón Ej.8 UserRepository: retorna Result para que el ViewModel actualice el estado.
class UserRepository(private val userDao: UserDao) {

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val user = userDao.getByEmail(email)
        return if (user != null && user.passwordHash == password) {
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Credenciales inválidas"))
        }
    }

    suspend fun register(
        nombre: String,
        email: String,
        telefono: String,
        password: String
    ): Result<Long> {
        if (userDao.getByEmail(email) != null) {
            return Result.failure(IllegalStateException("El correo ya está registrado"))
        }
        val id = userDao.insert(
            UserEntity(
                nombre = nombre,
                email = email,
                telefono = telefono,
                passwordHash = password
            )
        )
        return Result.success(id)
    }

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getByEmail(email)

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
}
