package com.example.dev_mobile.data.local.dao

import androidx.room.*
import com.example.dev_mobile.data.local.entity.FestivalEntity
import com.example.dev_mobile.data.local.entity.PendingOperationEntity
import com.example.dev_mobile.data.local.entity.ReservantEntity
import com.example.dev_mobile.data.local.entity.ReservationEntity
import kotlinx.coroutines.flow.Flow

// ── Festival DAO ──────────────────────────────────────────────────────────────
@Dao
interface FestivalDao {

    @Query("SELECT * FROM festivals ORDER BY est_courant DESC, cachedAt DESC")
    fun observeAll(): Flow<List<FestivalEntity>>

    @Query("SELECT * FROM festivals ORDER BY est_courant DESC, cachedAt DESC")
    suspend fun getAll(): List<FestivalEntity>

    @Query("SELECT * FROM festivals WHERE est_courant = 1 LIMIT 1")
    suspend fun getCourant(): FestivalEntity?

    @Query("SELECT * FROM festivals WHERE id = :id")
    suspend fun getById(id: Int): FestivalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(festivals: List<FestivalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(festival: FestivalEntity)

    @Query("DELETE FROM festivals")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM festivals")
    suspend fun count(): Int
}

// ── Réservant DAO ─────────────────────────────────────────────────────────────
@Dao
interface ReservantDao {

    @Query("SELECT * FROM reservants ORDER BY nom")
    fun observeAll(): Flow<List<ReservantEntity>>

    @Query("SELECT * FROM reservants ORDER BY nom")
    suspend fun getAll(): List<ReservantEntity>

    @Query("SELECT * FROM reservants WHERE id = :id")
    suspend fun getById(id: Int): ReservantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reservants: List<ReservantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reservant: ReservantEntity)

    @Query("DELETE FROM reservants")
    suspend fun deleteAll()

    @Query("DELETE FROM reservants WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM reservants")
    suspend fun count(): Int
}

// ── Réservation DAO ───────────────────────────────────────────────────────────
@Dao
interface ReservationDao {

    @Query("SELECT * FROM reservations WHERE festival_id = :festivalId ORDER BY created_at DESC")
    fun observeByFestival(festivalId: Int): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE festival_id = :festivalId ORDER BY created_at DESC")
    suspend fun getByFestival(festivalId: Int): List<ReservationEntity>

    @Query("SELECT * FROM reservations WHERE id = :id")
    suspend fun getById(id: Int): ReservationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reservations: List<ReservationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reservation: ReservationEntity)

    @Query("DELETE FROM reservations WHERE festival_id = :festivalId")
    suspend fun deleteByFestival(festivalId: Int)

    @Query("DELETE FROM reservations WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE reservations SET etat_contact = :etat WHERE id = :id")
    suspend fun updateEtatContact(id: Int, etat: String)

    @Query("UPDATE reservations SET etat_presence = :etat WHERE id = :id")
    suspend fun updateEtatPresence(id: Int, etat: String)

    @Query("SELECT COUNT(*) FROM reservations WHERE festival_id = :festivalId")
    suspend fun countByFestival(festivalId: Int): Int
}

// ── Pending Operations DAO ────────────────────────────────────────────────────
@Dao
interface PendingOperationDao {

    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<PendingOperationEntity>>

    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingOperationEntity>

    @Query("SELECT COUNT(*) FROM pending_operations")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pending_operations")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: PendingOperationEntity): Long

    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM pending_operations")
    suspend fun deleteAll()

    @Query("UPDATE pending_operations SET retryCount = retryCount + 1, lastError = :error WHERE id = :id")
    suspend fun incrementRetry(id: Int, error: String)
}