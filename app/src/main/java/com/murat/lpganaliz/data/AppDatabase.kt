package com.murat.lpganaliz.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface YakitDao {

    @Query("SELECT * FROM yakit_kayitlari ORDER BY km DESC")
    fun tumKayitlar(): Flow<List<YakitKaydi>>

    @Query("SELECT * FROM yakit_kayitlari ORDER BY km ASC")
    suspend fun tumKayitlarListe(): List<YakitKaydi>

    @Query("SELECT * FROM yakit_kayitlari WHERE id = :id")
    suspend fun kayit(id: Long): YakitKaydi?

    @Query("SELECT * FROM yakit_kayitlari WHERE km <= :km ORDER BY km DESC LIMIT 1")
    suspend fun sonKayit(km: Int): YakitKaydi?

    @Query("SELECT * FROM yakit_kayitlari ORDER BY km DESC LIMIT 1")
    suspend fun enSonKayit(): YakitKaydi?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun ekle(kayit: YakitKaydi): Long

    @Update
    suspend fun guncelle(kayit: YakitKaydi)

    @Query("DELETE FROM yakit_kayitlari WHERE id = :id")
    suspend fun sil(id: Long)

    @Query("DELETE FROM yakit_kayitlari")
    suspend fun hepsiniSil()

    @Query("SELECT * FROM arac WHERE id = 1")
    fun aracAkisi(): Flow<AracProfili?>

    @Query("SELECT * FROM arac WHERE id = 1")
    suspend fun arac(): AracProfili?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun aracKaydet(arac: AracProfili)
}

@Database(entities = [YakitKaydi::class, AracProfili::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): YakitDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /** v1 -> v2: arac tablosuna depoLitre (LPG depo hacmi) sütunu ekler. */
        private val GECIS_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE arac ADD COLUMN depoLitre REAL NOT NULL DEFAULT 37.0")
            }
        }

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "lpg_analiz.db"
            ).addMigrations(GECIS_1_2).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}
