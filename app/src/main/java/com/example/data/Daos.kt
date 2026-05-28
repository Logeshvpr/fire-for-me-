package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users ORDER BY username ASC")
    suspend fun getAllUsersDirect(): List<User>

    @Query("SELECT * FROM users WHERE username = :username AND role = :role LIMIT 1")
    suspend fun login(username: String, role: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Delete
    suspend fun deleteUser(user: User)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Int): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long
}

@Dao
interface SiteDao {
    @Query("SELECT * FROM sites ORDER BY siteName ASC")
    fun getAllSites(): Flow<List<Site>>

    @Query("SELECT * FROM sites WHERE customerId = :customerId")
    fun getSitesForCustomer(customerId: Int): Flow<List<Site>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: Site): Long
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY timestamp DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance ORDER BY timestamp DESC")
    suspend fun getAllAttendanceDirect(): List<Attendance>

    @Query("SELECT * FROM attendance WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestAttendanceForUser(userId: Int): Flow<Attendance?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long
}

@Dao
interface SiteSurveyDao {
    @Query("SELECT * FROM site_surveys ORDER BY createdAt DESC")
    fun getAllSurveys(): Flow<List<SiteSurvey>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurvey(survey: SiteSurvey): Long

    @Query("UPDATE site_surveys SET cleanDashboardMessage = :message, technicalRiskFlagged = :risk WHERE id = :id")
    suspend fun updateCustomerFacingMessage(id: Int, message: String, risk: Boolean)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY createdAt DESC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE customerName = :customerName ORDER BY createdAt DESC")
    fun getAppointmentsForCustomer(customerName: String): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Query("UPDATE appointments SET status = :status, assignedEngineer = :assignedEngineer WHERE id = :id")
    suspend fun updateAppointmentStatusAndEngineer(id: Int, status: String, assignedEngineer: String)

    @Update
    suspend fun updateAppointment(appointment: Appointment)
}

@Dao
interface SolarModelConfigDao {
    @Query("SELECT * FROM solar_models WHERE username = :username ORDER BY createdAt DESC")
    fun getModelsForUser(username: String): Flow<List<SolarModelConfig>>

    @Query("SELECT * FROM solar_models ORDER BY createdAt DESC")
    fun getAllModels(): Flow<List<SolarModelConfig>>

    @Query("SELECT * FROM solar_models WHERE id = :id LIMIT 1")
    suspend fun getModelById(id: Int): SolarModelConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(config: SolarModelConfig): Long

    @Delete
    suspend fun deleteModel(config: SolarModelConfig)
}
