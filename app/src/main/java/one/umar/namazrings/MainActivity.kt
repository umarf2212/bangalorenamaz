package one.umar.namazrings

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.app.Activity
import one.umar.namazrings.ui.NamazDashboardView
import one.umar.namazrings.widget.NamazWidgetProvider

class MainActivity : Activity() {
    private lateinit var dashboard: NamazDashboardView
    private val handler = Handler(Looper.getMainLooper())
    private val refresh = object : Runnable {
        override fun run() {
            dashboard.refresh()
            handler.postDelayed(this, REFRESH_INTERVAL_MS)
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        dashboard = NamazDashboardView(this)
        dashboard.onAddWidget = ::requestWidgetPin
        setContentView(dashboard)
    }

    override fun onResume() {
        super.onResume()
        val manager = AppWidgetManager.getInstance(this)
        dashboard.canPinWidget = manager.isRequestPinAppWidgetSupported
        dashboard.refresh()
        handler.post(refresh)
    }

    override fun onPause() {
        handler.removeCallbacks(refresh)
        super.onPause()
    }

    private fun requestWidgetPin() {
        val manager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, NamazWidgetProvider::class.java)
        val callbackIntent = Intent(this, MainActivity::class.java)
        val successCallback = PendingIntent.getActivity(
            this,
            1,
            callbackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        if (!manager.requestPinAppWidget(provider, null, successCallback)) {
            Toast.makeText(this, "Open the launcher widget picker and choose Namaz Rings.", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val REFRESH_INTERVAL_MS = 30_000L
    }
}
