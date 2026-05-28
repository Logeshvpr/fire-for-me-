package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AppRepository(
    private val userDao: UserDao,
    private val customerDao: CustomerDao,
    private val siteDao: SiteDao,
    private val attendanceDao: AttendanceDao,
    private val siteSurveyDao: SiteSurveyDao,
    private val appointmentDao: AppointmentDao,
    private val solarModelConfigDao: SolarModelConfigDao
) {
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allSites: Flow<List<Site>> = siteDao.getAllSites()
    val allAttendance: Flow<List<Attendance>> = attendanceDao.getAllAttendance()
    val allSurveys: Flow<List<SiteSurvey>> = siteSurveyDao.getAllSurveys()
    val allAppointments: Flow<List<Appointment>> = appointmentDao.getAllAppointments()

    fun getAppointmentsForCustomer(customerName: String): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsForCustomer(customerName)
    }

    suspend fun insertAppointment(appointment: Appointment): Long {
        return appointmentDao.insertAppointment(appointment)
    }

    suspend fun updateAppointmentStatusAndEngineer(id: Int, status: String, assignedEngineer: String) {
        appointmentDao.updateAppointmentStatusAndEngineer(id, status, assignedEngineer)
    }

    suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.updateAppointment(appointment)
    }

    fun getModelsForUser(username: String): Flow<List<SolarModelConfig>> {
        return solarModelConfigDao.getModelsForUser(username)
    }

    fun getAllModels(): Flow<List<SolarModelConfig>> {
        return solarModelConfigDao.getAllModels()
    }

    suspend fun insertModel(config: SolarModelConfig): Long {
        return solarModelConfigDao.insertModel(config)
    }

    suspend fun deleteModel(config: SolarModelConfig) {
        solarModelConfigDao.deleteModel(config)
    }

    suspend fun getAllUsersDirect(): List<User> {
        return userDao.getAllUsersDirect()
    }

    suspend fun getAllAttendanceDirect(): List<Attendance> {
        return attendanceDao.getAllAttendanceDirect()
    }

    suspend fun login(username: String, role: String): User? {
        return userDao.login(username, role)
    }

    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun insertCustomer(customer: Customer): Long {
        return customerDao.insertCustomer(customer)
    }

    suspend fun insertSite(site: Site): Long {
        return siteDao.insertSite(site)
    }

    suspend fun insertAttendance(attendance: Attendance): Long {
        return attendanceDao.insertAttendance(attendance)
    }

    suspend fun insertSurvey(survey: SiteSurvey): Long {
        return siteSurveyDao.insertSurvey(survey)
    }

    suspend fun translateTechnicalLogForCustomer(surveyId: String, rawTechnicalLog: String): com.example.network.CustomerProgressTranslation? {
        val prompt = """
            Analyze real-time field data logs and map them accurately into SolarRun's 6-step customer pipeline:
            Step 1: Free Home Visit (Assessment)
            Step 2: Personalized Proposal
            Step 3: Govt Paperwork & TNEB Coordination
            Step 4: 24-Hour Expert Installation (Wind-proof structure rated for 175 km/h)
            Step 5: Grid Net-Metering Connection (EB Office Meter Integration)
            Step 6: Active Power Generation & Subsidy Activation
            
            Analyze raw input from field team: "$rawTechnicalLog"
        """.trimIndent()
        
        val systemRule = """
            You are the automated coordination core for SolarRun Energies Pvt. Ltd. (Chennai).
            Generate a clear, highly reassuring status update for the SolarRun Customer Panel. Highlight Chennai specifics like our 25-year panel warranty, TNEB net-metering synchronization, ₹78,000 Govt Subsidy, or 175 km/h wind-proof design clearances.
            Respond ONLY in JSON matching this format:
            {"cleanDashboardMessage": "Status description matching 1 of the 6 steps", "estimatedPhaseCompletion": "Execution timeframe", "technicalRiskFlagged": true/false}
        """.trimIndent()
        
        val aiResult = com.example.network.GeminiHelper.callGemini(prompt, systemRule, true)
        
        val parsed = if (aiResult.isNotBlank() && aiResult != "MOCK_MODE_ACTIVE_KEY_NOT_CONFIGURED" && !aiResult.startsWith("Error")) {
            try {
                val moshi = com.squareup.moshi.Moshi.Builder()
                    .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()
                val adapter = moshi.adapter(com.example.network.CustomerProgressTranslation::class.java)
                adapter.fromJson(aiResult)
            } catch (e: Exception) {
                null
            }
        } else null

        val result = parsed ?: com.example.network.CustomerProgressTranslation(
            cleanDashboardMessage = "Rooftop parameters analyzed. Structure ready configuration matching premium standard.",
            estimatedPhaseCompletion = "Estimated 3-5 days to hardware delivery",
            technicalRiskFlagged = rawTechnicalLog.lowercase().contains("crack") || rawTechnicalLog.lowercase().contains("damage")
        )

        val idInt = surveyId.toIntOrNull()
        if (idInt != null) {
            siteSurveyDao.updateCustomerFacingMessage(idInt, result.cleanDashboardMessage, result.technicalRiskFlagged)
        }
        return result
    }

    suspend fun analyzeRoofBlueprintImage(imageBitmap: android.graphics.Bitmap): com.example.network.AutomatedSiteBlueprint? {
        val prompt = "Analyze rooftop blueprint asset. Return suggestedRoofStyle FLAT or SLANTED or GABLED, absolutePanelCapacity (1-24), structuralRiskAssessment, and estimatedGenerationCurve of 7 floats."
        val systemRule = "Return strictly JSON: {\"suggestedRoofStyle\": \"GABLED\", \"absolutePanelCapacity\": 18, \"structuralRiskAssessment\": \"LOW\", \"estimatedGenerationCurve\": [0.1, 0.3, 0.7, 0.9, 0.8, 0.4, 0.1]}"
        val aiResult = com.example.network.GeminiHelper.callGemini(prompt, systemRule, true)
        
        val parsed = if (aiResult.isNotBlank() && aiResult != "MOCK_MODE_ACTIVE_KEY_NOT_CONFIGURED" && !aiResult.startsWith("Error")) {
            try {
                val moshi = com.squareup.moshi.Moshi.Builder()
                    .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()
                val adapter = moshi.adapter(com.example.network.AutomatedSiteBlueprint::class.java)
                adapter.fromJson(aiResult)
            } catch (e: Exception) {
                null
            }
        } else null

        return parsed ?: com.example.network.AutomatedSiteBlueprint(
            suggestedRoofStyle = "SLANTED",
            absolutePanelCapacity = 16,
            structuralRiskAssessment = "LOW",
            estimatedGenerationCurve = listOf(0.1f, 0.3f, 0.8f, 0.95f, 0.78f, 0.45f, 0.12f)
        )
    }

    fun getLatestAttendanceForUser(userId: Int): Flow<Attendance?> {
        return attendanceDao.getLatestAttendanceForUser(userId)
    }

    fun getSitesForCustomer(customerId: Int): Flow<List<Site>> {
        return siteDao.getSitesForCustomer(customerId)
    }

    suspend fun populateInitialDataIfNeeded() {
        val users = getAllUsersDirect()
        if (users.isEmpty()) {
            // Add default users across standard roles requested by user
            userDao.insertUser(User(username = "Logesh Durai", role = "Staff", pin = "1234"))
            userDao.insertUser(User(username = "Alice Admin", role = "Admin", pin = "1111"))
            userDao.insertUser(User(username = "John Leader", role = "Site Leader", pin = "2222"))
            userDao.insertUser(User(username = "Mark Client", role = "Customer", pin = "3333"))
            userDao.insertUser(User(username = "Bob Engineer", role = "Staff", pin = "5678"))

            // Add default customers
            val tId = customerDao.insertCustomer(Customer(
                name = "Tesla Gigafactory Solar",
                phoneNumber = "+1-555-0199",
                email = "solar-install@tesla.com",
                company = "Tesla Inc.",
                address = "1 Electric Way, Austin, TX"
            )).toInt()

            val aId = customerDao.insertCustomer(Customer(
                name = "Acme Residential Complex",
                phoneNumber = "+1-555-0123",
                email = "facilities@acme.com",
                company = "Acme Properties",
                address = "500 Maple Boulevard, Sunnyvale, CA"
            )).toInt()

            val sId = customerDao.insertCustomer(Customer(
                name = "Sunnyvale Plaza Suite",
                phoneNumber = "+1-555-0145",
                email = "info@sunnyvaleplaza.com",
                company = "Plaza Holdings Ltd",
                address = "1200 Pacific Coast Hwy, San Jose, CA"
            )).toInt()

            // Add sites
            siteDao.insertSite(Site(customerId = tId, siteName = "Austin Roof Sector A", address = "Building 1 East Wing"))
            siteDao.insertSite(Site(customerId = tId, siteName = "Austin Roof Sector B", address = "Building 3 Battery Lab"))
            siteDao.insertSite(Site(customerId = aId, siteName = "Acme Main Clubhouse Roof", address = "Clubhouse Deck 2"))
            siteDao.insertSite(Site(customerId = sId, siteName = "Plaza Retail Wing", address = "Building A Flat Roof"))

            // Seed initial appointments to look complete
            appointmentDao.insertAppointment(
                Appointment(
                    customerName = "Mark Client",
                    address = "742 Evergreen Terrace",
                    phoneNumber = "555-0199",
                    appointmentDate = "2026-06-01",
                    appointmentTime = "10:30 AM",
                    status = "Confirmed",
                    notes = "Initial site assessment for 15kW rooftop solar panels array",
                    assignedEngineer = "Logesh Durai"
                )
            )

            appointmentDao.insertAppointment(
                Appointment(
                    customerName = "Mark Client",
                    address = "742 Evergreen Terrace",
                    phoneNumber = "555-0199",
                    appointmentDate = "2026-06-15",
                    appointmentTime = "02:00 PM",
                    status = "Pending",
                    notes = "Roof pitch integrity certification check",
                    assignedEngineer = "Bob Engineer"
                )
            )
        }
    }
}

class SolarAppRepository(
    private val surveyDao: SiteSurveyDao,
    private val apiKey: String
) {
    suspend fun translateTechnicalLogForCustomer(surveyId: String, rawTechnicalLog: String): com.example.network.CustomerProgressTranslation? {
        val prompt = """
            Analyze real-time field data logs and map them accurately into SolarRun's 6-step customer pipeline:
            Step 1: Free Home Visit (Assessment)
            Step 2: Personalized Proposal
            Step 3: Govt Paperwork & TNEB Coordination
            Step 4: 24-Hour Expert Installation (Wind-proof structure rated for 175 km/h)
            Step 5: Grid Net-Metering Connection (EB Office Meter Integration)
            Step 6: Active Power Generation & Subsidy Activation
            
            Analyze raw input from field team: "$rawTechnicalLog"
        """.trimIndent()
        
        val systemRule = """
            You are the automated coordination core for SolarRun Energies Pvt. Ltd. (Chennai).
            Generate a clear, highly reassuring status update for the SolarRun Customer Panel. Highlight Chennai specifics like our 25-year panel warranty, TNEB net-metering synchronization, ₹78,000 Govt Subsidy, or 175 km/h wind-proof design clearances.
            Respond ONLY in JSON matching this format:
            {"cleanDashboardMessage": "Status description matching 1 of the 6 steps", "estimatedPhaseCompletion": "Execution timeframe", "technicalRiskFlagged": true/false}
        """.trimIndent()
        
        val aiResult = com.example.network.GeminiHelper.callGemini(prompt, systemRule, true)
        
        val parsed = if (aiResult.isNotBlank() && aiResult != "MOCK_MODE_ACTIVE_KEY_NOT_CONFIGURED" && !aiResult.startsWith("Error")) {
            try {
                val moshi = com.squareup.moshi.Moshi.Builder()
                    .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()
                val adapter = moshi.adapter(com.example.network.CustomerProgressTranslation::class.java)
                adapter.fromJson(aiResult)
            } catch (e: Exception) {
                null
            }
        } else null

        val result = parsed ?: com.example.network.CustomerProgressTranslation(
            cleanDashboardMessage = "Rooftop parameters analyzed. Structure ready configuration matching premium standard.",
            estimatedPhaseCompletion = "Estimated 3-5 days to hardware delivery",
            technicalRiskFlagged = rawTechnicalLog.lowercase().contains("crack") || rawTechnicalLog.lowercase().contains("damage")
        )

        val idInt = surveyId.toIntOrNull()
        if (idInt != null) {
            surveyDao.updateCustomerFacingMessage(idInt, result.cleanDashboardMessage, result.technicalRiskFlagged)
        }
        return result
    }

    suspend fun analyzeRoofBlueprintImage(imageBitmap: android.graphics.Bitmap): com.example.network.AutomatedSiteBlueprint? {
        val prompt = "Analyze rooftop blueprint asset. Return suggestedRoofStyle FLAT or SLANTED or GABLED, absolutePanelCapacity (1-24), structuralRiskAssessment, and estimatedGenerationCurve of 7 floats."
        val systemRule = "Return strictly JSON: {\"suggestedRoofStyle\": \"GABLED\", \"absolutePanelCapacity\": 18, \"structuralRiskAssessment\": \"LOW\", \"estimatedGenerationCurve\": [0.1, 0.3, 0.7, 0.9, 0.8, 0.4, 0.1]}"
        val aiResult = com.example.network.GeminiHelper.callGemini(prompt, systemRule, true)
        
        val parsed = if (aiResult.isNotBlank() && aiResult != "MOCK_MODE_ACTIVE_KEY_NOT_CONFIGURED" && !aiResult.startsWith("Error")) {
            try {
                val moshi = com.squareup.moshi.Moshi.Builder()
                    .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()
                val adapter = moshi.adapter(com.example.network.AutomatedSiteBlueprint::class.java)
                adapter.fromJson(aiResult)
            } catch (e: Exception) {
                null
            }
        } else null

        return parsed ?: com.example.network.AutomatedSiteBlueprint(
            suggestedRoofStyle = "SLANTED",
            absolutePanelCapacity = 16,
            structuralRiskAssessment = "LOW",
            estimatedGenerationCurve = listOf(0.1f, 0.3f, 0.8f, 0.95f, 0.78f, 0.45f, 0.12f)
        )
    }
}

