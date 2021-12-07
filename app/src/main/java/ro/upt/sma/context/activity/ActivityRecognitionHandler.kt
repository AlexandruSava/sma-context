package ro.upt.sma.context.activity

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient

class ActivityRecognitionHandler(private val context: Context) {

    private val client: ActivityRecognitionClient = ActivityRecognition.getClient(context)

    @SuppressLint("UnspecifiedImmutableFlag")
    fun registerPendingIntent(): PendingIntent {

        // Create a pending intent for ActivityRecognitionService and register for updates to activity recognition client.

        val intent = Intent(context, ActivityRecognitionService::class.java)
        val pendingIntent = PendingIntent.getService(
            context,
            111,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        client.requestActivityUpdates(113, pendingIntent)

        return null!!
    }

    fun unregisterPendingIntent(pendingIntent: PendingIntent) {
        client.removeActivityUpdates(pendingIntent)
    }

}
