package com.example.appmovil.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- USUARIOS ---
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- CARRITO ---
    @Query("SELECT * FROM carrito")
    fun getCartItems(): Flow<List<CartItemEntity>> // Flow permite actualizar la UI en tiempo real

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Query("DELETE FROM carrito WHERE id = :id")
    suspend fun deleteCartItem(id: Long)

    @Query("DELETE FROM carrito")
    suspend fun clearCart()
}