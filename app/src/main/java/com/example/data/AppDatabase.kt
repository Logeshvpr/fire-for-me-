package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        Customer::class,
        Site::class,
        Attendance::class,
        SiteSurvey::class,
        Appointment::class,
        SolarModelConfig::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun customerDao(): CustomerDao
    abstract fun siteDao(): SiteDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun siteSurveyDao(): SiteSurveyDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun solarModelConfigDao(): SolarModelConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "survey_attendance_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
