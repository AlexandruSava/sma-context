package ro.upt.sma.context.activity


import android.app.IntentService
import android.content.Intent
import com.google.android.gms.location.ActivityRecognitionResult

class ActivityRecognitionService : IntentService("ActivityRecognitionService") {

    override fun onHandleIntent(intent: Intent?) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            val detectedActivity = result.mostProbableActivity
            val broadcastIntent = Intent(INTENT_ACTION)
            broadcastIntent.putExtra(ACTIVITY_EXTRA, detectedActivity.type)
            sendBroadcast(broadcastIntent)
        }
    }

    companion object {
        const val INTENT_ACTION = "ro.upt.sma.context.activity"
        const val ACTIVITY_EXTRA = "activity"
    }

}
