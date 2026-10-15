package com.example.appmovil.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appmovil.data.local.empresa.EmpresaDao
import com.example.appmovil.data.local.empresa.EmpresaEntity
import com.example.appmovil.data.local.maquina.MaquinaDao
import com.example.appmovil.data.local.maquina.MaquinaEntity
import com.example.appmovil.data.local.medicion.MedicionDao
import com.example.appmovil.data.local.medicion.MedicionEntity
import com.example.appmovil.data.local.protocolo.ProtocoloDao
import com.example.appmovil.data.local.protocolo.ProtocoloEntity
import com.example.appmovil.data.local.user.UserDao
import com.example.appmovil.data.local.user.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Patrón Ej.8 AppDatabase: singleton + callback con seed + fallback destructivo.
// v4 CasoA: Empresa -> Maquina -> Medicion (se conserva protocolos/usuarios por compatibilidad).
@Database(
    entities = [UserEntity::class, ProtocoloEntity::class, EmpresaEntity::class, MaquinaEntity::class, MedicionEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun protocoloDao(): ProtocoloDao
    abstract fun empresaDao(): EmpresaDao
    abstract fun maquinaDao(): MaquinaDao
    abstract fun medicionDao(): MedicionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "induztek_database"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).userDao()
                                if (dao.count() == 0) {
                                    dao.insert(
                                        UserEntity(
                                            nombre = "Técnico Demo",
                                            email = "tecnico@induztek.cl",
                                            telefono = "+56911111111",
                                            passwordHash = "Induztek123!",
                                            direccion = "Base Plaza Norte"
                                        )
                                    )
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Compatibilidad con código anterior que usaba getDatabase()
        fun getDatabase(context: Context): AppDatabase = getInstance(context)
    }
}
