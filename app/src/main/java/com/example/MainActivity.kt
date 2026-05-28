package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy {
        AppRepository(
            database.userDao(),
            database.customerDao(),
            database.siteDao(),
            database.attendanceDao(),
            database.siteSurveyDao(),
            database.appointmentDao(),
            database.solarModelConfigDao()
        )
    }

    private val viewModel: AppViewModel by viewModels {
        AppViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    val allUsers by viewModel.allUsers.collectAsState()
                    val currentUser by viewModel.currentUser.collectAsState()

                    var locationPermissionGranted by remember {
                        mutableStateOf(
                            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                                    hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }

                    // Request permissions contract launcher
                    var pendingOnLocationFetched by remember { mutableStateOf<( (Double, Double) -> Unit )?>(null) }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                        locationPermissionGranted = fineGranted || coarseGranted

                        if (locationPermissionGranted) {
                            pendingOnLocationFetched?.let {
                                fetchActiveLocation(it)
                                pendingOnLocationFetched = null
                            }
                        } else {
                            Toast.makeText(this, "Location permissions denied.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    val requestLocationTrigger: ( (Double, Double) -> Unit ) -> Unit = { callback ->
                        if (locationPermissionGranted) {
                            fetchActiveLocation(callback)
                        } else {
                            pendingOnLocationFetched = callback
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }

                    Crossfade(targetState = currentUser, label = "ScreenTransition") { user ->
                        if (user == null) {
                            LoginScreen(
                                viewModel = viewModel,
                                allUsers = allUsers,
                                onLoginSuccess = { loggedInUser ->
                                    Toast.makeText(this, "Welcome ${loggedInUser.username}!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            when (user.role) {
                                "Admin" -> {
                                    AdminDashboard(
                                        viewModel = viewModel,
                                        onLogout = { viewModel.logout() }
                                    )
                                }
                                "Site Leader" -> {
                                    SiteLeaderDashboard(
                                        viewModel = viewModel,
                                        onLogout = { viewModel.logout() }
                                    )
                                }
                                "Customer" -> {
                                    CustomerDashboard(
                                        viewModel = viewModel,
                                        onLogout = { viewModel.logout() }
                                    )
                                }
                                else -> {
                                    StaffDashboard(
                                        viewModel = viewModel,
                                        onLogout = { viewModel.logout() },
                                        onRequestSystemLocation = requestLocationTrigger
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun fetchActiveLocation(onFetched: (Double, Double) -> Unit) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            val cts = CancellationTokenSource()
            
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cts.token
            ).addOnSuccessListener { loc ->
                if (loc != null) {
                    onFetched(loc.latitude, loc.longitude)
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            onFetched(lastLoc.latitude, lastLoc.longitude)
                        } else {
                            // Fallback standard default coordinates (AUSTIN, TX)
                            onFetched(30.2672, -97.7431)
                            Toast.makeText(this, "Using fallback coordinates (GPS is disabled on emulator).", Toast.LENGTH_LONG).show()
                        }
                    }.addOnFailureListener {
                        onFetched(30.2672, -97.7431)
                    }
                }
            }.addOnFailureListener {
                onFetched(30.2672, -97.7431)
                Toast.makeText(this, "Location fetch failed, using default Austin coordinates.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            onFetched(30.2672, -97.7431)
        }
    }
}
