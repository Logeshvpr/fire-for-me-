package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val role: String, // "Admin" or "Field Staff"
    val pin: String = "1234",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val company: String,
    val address: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sites")
data class Site(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val siteName: String,
    val address: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val userName: String,
    val userRole: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val isSimulated: Boolean = false,
    val status: String = "Checked In" // "Checked In" or "Checked Out"
)

@Entity(tableName = "site_surveys")
data class SiteSurvey(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val siteId: Int, // Refers to Site
    val siteName: String,
    val customerName: String,
    val engineerId: Int,
    val engineerName: String,
    val roofLength: Double,
    val roofWidth: Double,
    val roofPitch: Int, // degrees
    val orientation: String, // N, S, E, W, etc.
    val inverterModel: String,
    val photoPath: String? = null,
    val notes: String = "",
    val cleanDashboardMessage: String? = null,
    val technicalRiskFlagged: Boolean? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val address: String,
    val phoneNumber: String,
    val appointmentDate: String,
    val appointmentTime: String,
    val status: String = "Pending", // "Pending", "Confirmed", "Completed", "Cancelled"
    val notes: String = "",
    val assignedEngineer: String = "Unassigned",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "solar_models")
data class SolarModelConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val roofStyle: String, // "Gabled", "Flat", "Slanted", "Hip roof"
    val panelCount: Int,
    val efficiency: String, // "High Performance Monocrystalline", "Standard Polycrystalline", "Premium Thin-Film"
    val orientation: String,
    val designNotes: String = "",
    val aiBriefAssessment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
