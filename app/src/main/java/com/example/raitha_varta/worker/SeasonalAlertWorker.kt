package com.example.raitha_varta.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.raitha_varta.util.FarmingNotificationManager
import java.util.Calendar

class SeasonalAlertWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        
        // Dynamic advice based on season/month
        val (title, message) = when (month) {
            Calendar.MAY, Calendar.JUNE -> 
                Pair(
                    "🌦️ Kharif Season Alert: Time to Grow!", 
                    "Monsoon is arriving. Prepare your fields for Paddy, Maize, and Ragi. Ensure soil drainage is clear before heavy rains."
                )
            Calendar.JULY, Calendar.AUGUST -> 
                Pair(
                    "🌱 Kharif Mid-Season: Watch for Pests!", 
                    "High humidity expected. Watch out for stem borers in Paddy. Apply top dressing of Urea as per schedule."
                )
            Calendar.SEPTEMBER, Calendar.OCTOBER -> 
                Pair(
                    "🌾 Kharif Harvest & Rabi Prep", 
                    "Time to cut early Kharif crops. Clear fields and prepare for Rabi sowing (Wheat, Gram). Store grains in dry places."
                )
            Calendar.NOVEMBER, Calendar.DECEMBER -> 
                Pair(
                    "❄️ Rabi Season: Irrigation Alert", 
                    "Winter is here. Ensure light irrigation for Wheat to protect from frost. Monitor Tomato for early blight."
                )
            Calendar.JANUARY, Calendar.FEBRUARY -> 
                Pair(
                    "🚜 Rabi Harvest Soon", 
                    "Avoid heavy irrigation. Prepare cutting tools for upcoming Rabi harvest. Watch for sudden temperature spikes."
                )
            else -> // March, April
                Pair(
                    "☀️ Zaid Season (Summer)", 
                    "Hot weather approaching. Plant short-duration summer crops (Cucumber, Watermelon). Ensure heavy mulching to retain soil moisture."
                )
        }

        FarmingNotificationManager.showSeasonalNotification(applicationContext, title, message)

        return Result.success()
    }
}
