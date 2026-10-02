package com.example.appmovil.repository

import com.example.appmovil.models.AppDao
import com.example.appmovil.models.UserEntity

class UserRepository(private val appDao: AppDao) {

    // Registrar un nuevo usuario
    suspend fun registerUser(user: UserEntity): Long {
        return appDao.insertUser(user)
    }

    // Buscar usuario por correo para el inicio de sesión
    suspend fun getUserByEmail(email: String): UserEntity? {
        return appDao.getUserByEmail(email)
    }

    // Actualizar datos del usuario (para el perfil)
    suspend fun updateUser(user: UserEntity) {
        appDao.updateUser(user)
    }
}