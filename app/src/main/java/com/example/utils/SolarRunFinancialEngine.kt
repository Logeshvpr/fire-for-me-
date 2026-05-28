package com.example.utils

object SolarRunFinancialEngine {
    
    // Core data values from SolarRun's corporate engine
    const val MAX_GOVT_SUBSIDY = 78000.0
    const val AVERAGE_PAYBACK_YEARS = 3.9
    const val ROI_PERCENTAGE = 26.0

    /**
     * Calculates realistic corporate metrics matching the solarrun.in model.
     * @param panelCount The dynamic module count designed by the user on the 3D Canvas.
     */
    fun calculateSavingsProjection(panelCount: Int): SolarRunPricingSheet {
        // Assuming modern residential panels around 550W each (2 modules = 1.1 kW approx)
        val calculatedKw = (panelCount * 550) / 1000.0
        val baseCost = panelCount * 22000.0 // Local operational baseline estimate
        
        // SolarRun Subsidy Logic: Max subsidy tier applies up to 3kW+ allocations
        val appliedSubsidy = if (calculatedKw >= 3.0) MAX_GOVT_SUBSIDY else (calculatedKw * 25000.0)
        val netInvestment = (baseCost - appliedSubsidy).coerceAtLeast(15000.0)
        
        val annualSavings = netInvestment * (ROI_PERCENTAGE / 100.0)
        val lifetimeSavings = annualSavings * 25.0 // 25-Year Panel Warranty metric

        return SolarRunPricingSheet(
            systemSizeKw = calculatedKw,
            estimatedSubsidy = appliedSubsidy,
            netInvestmentCost = netInvestment,
            annualSavings = annualSavings,
            lifetimeSavings = lifetimeSavings
        )
    }
}

data class SolarRunPricingSheet(
    val systemSizeKw: Double,
    val estimatedSubsidy: Double,
    val netInvestmentCost: Double,
    val annualSavings: Double,
    val lifetimeSavings: Double
)
