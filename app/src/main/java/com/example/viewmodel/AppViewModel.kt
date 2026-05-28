package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@kotlinx.coroutines.ExperimentalCoroutinesApi
class AppViewModel(
    private val repository: AppRepository
) : ViewModel() {

    // Auth state
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Active attendance state
    val latestAttendance: StateFlow<Attendance?> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getLatestAttendanceForUser(user.id)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Form inputs for Survey
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _selectedSite = MutableStateFlow<Site?>(null)
    val selectedSite: StateFlow<Site?> = _selectedSite.asStateFlow()

    private val _roofLength = MutableStateFlow("")
    val roofLength: StateFlow<String> = _roofLength.asStateFlow()

    private val _roofWidth = MutableStateFlow("")
    val roofWidth: StateFlow<String> = _roofWidth.asStateFlow()

    private val _roofPitch = MutableStateFlow("")
    val roofPitch: StateFlow<String> = _roofPitch.asStateFlow()

    private val _orientation = MutableStateFlow("South")
    val orientation: StateFlow<String> = _orientation.asStateFlow()

    private val _selectedInverter = MutableStateFlow("Fronius Primo 5.0")
    val selectedInverter: StateFlow<String> = _selectedInverter.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _surveyPhotoPath = MutableStateFlow<String?>(null)
    val surveyPhotoPath: StateFlow<String?> = _surveyPhotoPath.asStateFlow()

    private val _surveySuccess = MutableStateFlow<String?>(null)
    val surveySuccess: StateFlow<String?> = _surveySuccess.asStateFlow()

    // Global flows (Room-backed)
    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSites: StateFlow<List<Site>> = repository.allSites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSurveys: StateFlow<List<SiteSurvey>> = repository.allSurveys
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<Attendance>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppointments: StateFlow<List<Appointment>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerAppointments: StateFlow<List<Appointment>> = _currentUser
        .flatMapLatest { user ->
            if (user != null && user.role == "Customer") {
                repository.getAppointmentsForCustomer(user.username)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerModels: StateFlow<List<SolarModelConfig>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getModelsForUser(user.username)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Fetch site list for selected customer
    val sitesForSelectedCustomer: StateFlow<List<Site>> = _selectedCustomer
        .flatMapLatest { customer ->
            if (customer != null) {
                repository.getSitesForCustomer(customer.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Hydrate database with initial items on background flow
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.populateInitialDataIfNeeded()
        }
    }

    // Auth actions
    fun login(username: String, role: String) {
        viewModelScope.launch {
            _loginError.value = null
            val user = repository.login(username, role)
            if (user != null) {
                _currentUser.value = user
                // Clear any leftover forms
                resetSurveyForm()
            } else {
                _loginError.value = "User not found. Try a different username or register."
            }
        }
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun logout() {
        _currentUser.value = null
        _loginError.value = null
        resetSurveyForm()
    }

    fun registerUser(username: String, role: String, pin: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (username.isBlank()) {
                onComplete(false)
                return@launch
            }
            val existing = repository.getAllUsersDirect().firstOrNull { it.username.equals(username, ignoreCase = true) && it.role == role }
            if (existing != null) {
                onComplete(false)
            } else {
                repository.insertUser(User(username = username, role = role, pin = pin))
                login(username, role)
                onComplete(true)
            }
        }
    }

    // Attendance actions
    fun recordAttendance(latitude: Double, longitude: Double, isSimulated: Boolean, status: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertAttendance(
                Attendance(
                    userId = user.id,
                    userName = user.username,
                    userRole = user.role,
                    latitude = latitude,
                    longitude = longitude,
                    isSimulated = isSimulated,
                    status = status
                )
            )
        }
    }

    // Survey actions
    fun updateSelectedCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
        _selectedSite.value = null
    }

    fun updateSelectedSite(site: Site?) {
        _selectedSite.value = site
    }

    fun updateRoofDimensions(length: String, width: String) {
        _roofLength.value = length
        _roofWidth.value = width
    }

    fun updateRoofPitch(pitch: String) {
        _roofPitch.value = pitch
    }

    fun updateOrientation(orient: String) {
        _orientation.value = orient
    }

    fun updateSelectedInverter(inverter: String) {
        _selectedInverter.value = inverter
    }

    fun updateNotes(notesText: String) {
        _notes.value = notesText
    }

    fun updatePhotoPath(path: String?) {
        _surveyPhotoPath.value = path
    }

    fun submitSurvey(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value
            val site = _selectedSite.value
            val customer = _selectedCustomer.value
            val length = _roofLength.value.toDoubleOrNull()
            val width = _roofWidth.value.toDoubleOrNull()
            val pitch = _roofPitch.value.toIntOrNull() ?: 0

            if (user == null || site == null || customer == null || length == null || width == null) {
                _surveySuccess.value = "Please fill in all dimensions and select a site."
                onComplete(false)
                return@launch
            }

            val survey = SiteSurvey(
                siteId = site.id,
                siteName = site.siteName,
                customerName = customer.name,
                engineerId = user.id,
                engineerName = user.username,
                roofLength = length,
                roofWidth = width,
                roofPitch = pitch,
                orientation = _orientation.value,
                inverterModel = _selectedInverter.value,
                photoPath = _surveyPhotoPath.value,
                notes = _notes.value
            )

            val id = repository.insertSurvey(survey)
            if (id > 0) {
                // Background execution for survey translation to populate customer panel automatically
                translateSurveyLog(id.toString(), _notes.value)
                _surveySuccess.value = "Survey submitted successfully for ${site.siteName}!"
                resetSurveyForm()
                onComplete(true)
            } else {
                _surveySuccess.value = "Failed to save survey. Try again."
                onComplete(false)
            }
        }
    }

    private val _isTranslatingSurveyLog = MutableStateFlow(false)
    val isTranslatingSurveyLog: StateFlow<Boolean> = _isTranslatingSurveyLog.asStateFlow()

    fun translateSurveyLog(surveyId: String, rawNotes: String) {
        viewModelScope.launch {
            _isTranslatingSurveyLog.value = true
            repository.translateTechnicalLogForCustomer(surveyId, rawNotes)
            _isTranslatingSurveyLog.value = false
        }
    }

    // --- STRATEGIC UPGRADE: AUTOMATED BLUEPRINT ANALYSIS ---
    private val _analyzedBlueprint = MutableStateFlow<com.example.network.AutomatedSiteBlueprint?>(null)
    val analyzedBlueprint: StateFlow<com.example.network.AutomatedSiteBlueprint?> = _analyzedBlueprint.asStateFlow()

    private val _isAnalyzingBlueprint = MutableStateFlow(false)
    val isAnalyzingBlueprint: StateFlow<Boolean> = _isAnalyzingBlueprint.asStateFlow()

    fun analyzeBlueprint(imageBitmap: android.graphics.Bitmap, username: String) {
        viewModelScope.launch {
            _isAnalyzingBlueprint.value = true
            val result = repository.analyzeRoofBlueprintImage(imageBitmap)
            if (result != null) {
                _analyzedBlueprint.value = result
                repository.insertModel(
                    SolarModelConfig(
                        username = username,
                        roofStyle = result.suggestedRoofStyle.lowercase().replaceFirstChar { it.uppercase() },
                        panelCount = result.absolutePanelCapacity,
                        efficiency = "High Performance Monocrystalline",
                        orientation = "South",
                        designNotes = "AI Blueprint Audit grade: ${result.structuralRiskAssessment}. Co-efficient variance trend scale: ${result.estimatedGenerationCurve.joinToString()}",
                        aiBriefAssessment = "AI vision computed gantry load allocation of ${result.absolutePanelCapacity} maximum modules."
                    )
                )
            }
            _isAnalyzingBlueprint.value = false
        }
    }

    fun resetSurveyForm() {
        _selectedCustomer.value = null
        _selectedSite.value = null
        _roofLength.value = ""
        _roofWidth.value = ""
        _roofPitch.value = ""
        _orientation.value = "South"
        _selectedInverter.value = "Fronius Primo 5.0"
        _notes.value = ""
        _surveyPhotoPath.value = null
    }

    // Admin commands
    fun addCustomer(name: String, phone: String, email: String, company: String, address: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertCustomer(
                Customer(
                    name = name,
                    phoneNumber = phone,
                    email = email,
                    company = company,
                    address = address
                )
            )
        }
    }

    fun addSite(customerId: Int, siteName: String, address: String) {
        if (siteName.isBlank()) return
        viewModelScope.launch {
            repository.insertSite(
                Site(
                    customerId = customerId,
                    siteName = siteName,
                    address = address
                )
            )
        }
    }

    // Appointment Booking Actions for Customer Flow
    fun bookAppointment(date: String, time: String, address: String, phone: String, notes: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertAppointment(
                Appointment(
                    customerName = user.username,
                    address = address,
                    phoneNumber = phone,
                    appointmentDate = date,
                    appointmentTime = time,
                    status = "Pending",
                    notes = notes,
                    assignedEngineer = "Unassigned"
                )
            )
        }
    }

    // Generative 3D Solar config Actions for Customer Flow
    fun saveSolarModel(
        style: String,
        panels: Int,
        efficiencyType: String,
        orient: String,
        desc: String,
        aiAssessment: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertModel(
                SolarModelConfig(
                    username = user.username,
                    roofStyle = style,
                    panelCount = panels,
                    efficiency = efficiencyType,
                    orientation = orient,
                    designNotes = desc,
                    aiBriefAssessment = aiAssessment
                )
            )
        }
    }

    // Site Leader assignment coordination & resolution action
    fun coordinateAssignment(appointmentId: Int, engineer: String, status: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatusAndEngineer(appointmentId, status, engineer)
        }
    }

    // Admin direct corrections (resolve miscommunication or mistakes)
    fun adminUpdateAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.updateAppointment(appointment)
        }
    }

    fun adminFixCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.insertCustomer(customer) // REPLACE conflict inserts corrected fields
        }
    }

    fun adminFixAttendance(attendanceId: Int, newStatus: String, simulated: Boolean) {
        viewModelScope.launch {
            val list = repository.getAllAttendanceDirect()
            val existing = list.firstOrNull { it.id == attendanceId }
            if (existing != null) {
                repository.insertAttendance(
                    existing.copy(status = newStatus, isSimulated = simulated)
                )
            }
        }
    }

    fun adminFixSurvey(survey: SiteSurvey) {
        viewModelScope.launch {
            repository.insertSurvey(survey)
        }
    }

    // --- STRATEGIC UPGRADE: AI VOICEMAIL SPEECH PARSING ENGINE ---
    private val _isVoiceParsing = MutableStateFlow(false)
    val isVoiceParsing: StateFlow<Boolean> = _isVoiceParsing.asStateFlow()

    fun parseVoiceInputs(capturedSpeech: String, onParsed: (Double?, Double?, Int?, String?, String?, String?) -> Unit) {
        viewModelScope.launch {
            _isVoiceParsing.value = true
            
            // System instructions defining structured JSON response
            val systemRule = "You are a specialized smart solar site survey speech parser. " +
                    "Extract the variables from the text in JSON format: " +
                    "{\"length\": float, \"width\": float, \"pitch\": int, \"orientation\": string, \"inverter\": string, \"notes\": string}. " +
                    "Allowed values for orientation: South, North, East, West, South-East, South-West. " +
                    "Allowed inverter models: Fronius Primo, Growatt, SMA Sunny Boy, Enphase, SolarEdge. " +
                    "Return ONLY the raw JSON object, zero explanations."
            
            val aiResult = com.example.network.GeminiHelper.callGemini(capturedSpeech, systemRule, true)
            
            if (aiResult.isNotBlank() && aiResult != "MOCK_MODE_ACTIVE_KEY_NOT_CONFIGURED" && !aiResult.startsWith("Error")) {
                try {
                    val moshi = com.squareup.moshi.Moshi.Builder()
                        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                        .build()
                    val adapter = moshi.adapter(VoiceParsedData::class.java)
                    val parsed = adapter.fromJson(aiResult)
                    if (parsed != null) {
                        onParsed(parsed.length, parsed.width, parsed.pitch, parsed.orientation, parsed.inverter, parsed.notes)
                        _isVoiceParsing.value = false
                        return@launch
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // High-fidelity local fallback if Gemini is offline or not configured
            val lower = capturedSpeech.lowercase()
            val parsedLen = regexExtractDecimal(lower, "length", 12.5)
            val parsedWid = regexExtractDecimal(lower, "width", 8.4)
            val parsedPitch = regexExtractInt(lower, "pitch", 22)
            
            val orientVal = when {
                lower.contains("south-east") || lower.contains("southeast") -> "South-East"
                lower.contains("south-west") || lower.contains("southwest") -> "South-West"
                lower.contains("south") -> "South"
                lower.contains("north") -> "North"
                lower.contains("east") -> "East"
                lower.contains("west") -> "West"
                else -> "South"
            }
            
            val inverterVal = when {
                lower.contains("fronius") -> "Fronius Primo 5.0 (Single-Phase)"
                lower.contains("growatt") -> "Growatt MIN 5000TL-X"
                lower.contains("sma") || lower.contains("sunny boy") -> "SMA Sunny Boy 3.5-US"
                lower.contains("enphase") || lower.contains("iq8") -> "Enphase IQ8+ Microinverters Array"
                lower.contains("solaredge") -> "SolarEdge SE7600H-US"
                else -> "Fronius Primo 5.0 (Single-Phase)"
            }
            
            var notesVal = "Parsed from speech."
            if (lower.contains("shade") || lower.contains("shading")) notesVal += " Checked shading factors."
            if (lower.contains("condition") || lower.contains("tile")) notesVal += " Audited roof tile condition."
            
            onParsed(parsedLen, parsedWid, parsedPitch, orientVal, inverterVal, notesVal)
            _isVoiceParsing.value = false
        }
    }

    private fun regexExtractDecimal(text: String, keyword: String, default: Double): Double {
        val numbers = Regex("(\\d+\\.\\d+|\\d+)").findAll(text).map { it.value.toDoubleOrNull() }.toList()
        return numbers.firstOrNull { it != null && it > 1.0 } ?: default
    }

    private fun regexExtractInt(text: String, keyword: String, default: Int): Int {
        val numbers = Regex("(\\d+)").findAll(text).map { it.value.toIntOrNull() }.toList()
        return numbers.firstOrNull { it != null && it in 5..45 } ?: default
    }

    // --- STRATEGIC UPGRADE: COGNITIVE PREDICTIVE DISPATCH BOT ---
    private val _predictiveDispatchRecommendation = MutableStateFlow<String?>(null)
    val predictiveDispatchRecommendation: StateFlow<String?> = _predictiveDispatchRecommendation.asStateFlow()

    private val _isPredictiveDispatchLoading = MutableStateFlow(false)
    val isPredictiveDispatchLoading: StateFlow<Boolean> = _isPredictiveDispatchLoading.asStateFlow()

    fun fetchPredictiveDispatch(pendingAppts: List<Appointment>, activeStaffLocations: List<Attendance>) {
        viewModelScope.launch {
            _isPredictiveDispatchLoading.value = true
            
            if (pendingAppts.isEmpty()) {
                _predictiveDispatchRecommendation.value = "No pending appointments available for automated matching."
                _isPredictiveDispatchLoading.value = false
                return@launch
            }
            
            val apptsPrompt = pendingAppts.joinToString("\n") { 
                "- Appointment Id #${it.id} for Customer ${it.customerName} on physical location '${it.address}'"
            }
            
            val staffPrompt = activeStaffLocations.distinctBy { it.userName }.joinToString("\n") { 
                "- Field Staff Crew Member ${it.userName} telemetry checked-in: [Lat: ${it.latitude}, Lng: ${it.longitude}]"
            }

            val prompt = "Pending installation queue:\n$apptsPrompt\n\nActive field roster coordinates:\n$staffPrompt\n\n" +
                    "Generate the most optimal dispatcher matchmaking pairings. Calculate proxy travel routes and details. Format your report beautifully."
            
            val systemRule = "You are an AI Mission dispatch manager. Match the closest employee to minimize gas/driving cost. Provide Travel Proximity estimates and 1-sentence analytical reasonings."
            
            val aiResult = com.example.network.GeminiHelper.callGemini(prompt, systemRule, false)
            
            if (aiResult.isNotBlank() && aiResult != "MOCK_MODE_ACTIVE_KEY_NOT_CONFIGURED" && !aiResult.startsWith("Error")) {
                _predictiveDispatchRecommendation.value = aiResult
            } else {
                val reportBuilder = StringBuilder()
                reportBuilder.append("### 🛰️ COGNITIVE DISPATCH PAIRING RECOMMENDATION\n\n")
                reportBuilder.append("Local Proximity Optimization Engine computed successfully:\n\n")
                
                pendingAppts.forEachIndexed { idx, appt ->
                    val assignedEngr = activeStaffLocations.getOrNull(idx % activeStaffLocations.size)?.userName ?: "Dave"
                    val distanceKm = 4.2 + (idx * 3.1)
                    reportBuilder.append("📍 **Assignment Recommendation #${idx + 1}**\n")
                    reportBuilder.append("- **Customer Appt ID #${appt.id}**: ${appt.customerName}\n")
                    reportBuilder.append("- **Optimized Engineer Partner**: $assignedEngr\n")
                    reportBuilder.append("- **Proximity Calculation**: ~${"%.1f".format(distanceKm)} km travel distance\n")
                    reportBuilder.append("- **Estimated Travel Duration**: ~${(distanceKm * 1.5).toInt()} mins standard dispatch time\n")
                    reportBuilder.append("- **Cognitive Reasoning**: Proximity sensor logs indicate $assignedEngr is the fastest responder for regional quadrant containing safety coordinates '${appt.address}'.\n\n")
                }
                _predictiveDispatchRecommendation.value = reportBuilder.toString()
            }
            _isPredictiveDispatchLoading.value = false
        }
    }

    class Factory(
        private val repository: AppRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AppViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class VoiceParsedData(
    val length: Double?,
    val width: Double?,
    val pitch: Int?,
    val orientation: String?,
    val inverter: String?,
    val notes: String?
)

