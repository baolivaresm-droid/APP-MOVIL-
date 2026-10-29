package com.example.appmovil.repository.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appmovil.repository.models.Asignacion
import com.example.appmovil.repository.models.Empresa
import com.example.appmovil.repository.models.Informe
import com.example.appmovil.repository.models.Maquina
import com.example.appmovil.repository.models.Medicion
import com.example.appmovil.repository.models.Sucursal
import com.example.appmovil.repository.models.Usuario

// Usuario (rol + estado) -> Empresa -> Sucursal -> Maquina -> Medicion,
// más Asignacion (sucursal por técnico) e Informe (protocolos emitidos).
@Database(
    entities = [
        Usuario::class,
        Empresa::class,
        Sucursal::class,
        Maquina::class,
        Medicion::class,
        Asignacion::class,
        Informe::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun empresaDao(): EmpresaDao
    abstract fun sucursalDao(): SucursalDao
    abstract fun maquinaDao(): MaquinaDao
    abstract fun medicionDao(): MedicionDao
    abstract fun asignacionDao(): AsignacionDao
    abstract fun informeDao(): InformeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "induztek_database"
                )
                    // Proyecto académico: al cambiar la versión se recrea la BD
                    .fallbackToDestructiveMigration()
                    .addCallback(seedCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Datos de demostración. Se ejecutan en cada apertura con INSERT OR IGNORE,
        // así también se cargan después de una migración destructiva.
        // El administrador no se registra desde la app: solo personal autorizado.
        private val seedCallback = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL(
                    "INSERT OR IGNORE INTO usuarios " +
                        "(id, nombre, email, telefono, passwordHash, direccion, fotoUri, rol, estado) VALUES " +
                        "(1, 'Administrador Induztek', 'admin@induztek.cl', '+56900000000', " +
                        "'Induztek2026!', 'Casa matriz - Plaza Norte', '', 'ADMIN', 'APROBADO'), " +
                        "(2, 'Técnico Demo', 'tecnico@induztek.cl', '+56911111111', " +
                        "'Induztek123!', 'Base Plaza Norte', '', 'TECNICO', 'APROBADO')"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO empresas (id, nombre, rut) VALUES " +
                        "(1, 'Jumbo', '76.123.456-7'), (2, 'Líder', '77.765.432-1')"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO sucursales " +
                        "(id, empresaId, nombre, direccion, comuna, telefono) VALUES " +
                        "(1, 1, 'Jumbo La Florida', 'Av. José Pedro Alt 800', 'La Florida', '+56920000001'), " +
                        "(2, 1, 'Jumbo Las Condes', 'Av. Apoquindo 4500', 'Las Condes', '+56920000002'), " +
                        "(3, 2, 'Líder Quilpué', 'Av. Los Presidentes 1201', 'Quilpué', '+56920000003')"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO maquinas " +
                        "(id, sucursalId, categoria, codigo, descripcion, estado, creadoPor) VALUES " +
                        "(1, 1, 'Transformadores', 'TRA-001', 'Transformador de distribución 500 kVA', 'APROBADO', 2), " +
                        "(2, 1, 'Relés', 'REL-001', 'Relé de protección marca ABB', 'APROBADO', 2), " +
                        "(3, 2, 'Celdas', 'CEL-001', 'Celda de media tensión', 'APROBADO', 2), " +
                        "(4, 3, 'Baterías', 'BAT-001', 'Batería de respaldo 48V', 'APROBADO', 2)"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO asignaciones (usuarioId, sucursalId) VALUES " +
                        "(2, 1), (2, 2), (2, 3)"
                )
            }
        }
    }
}
