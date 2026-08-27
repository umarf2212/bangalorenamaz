package one.umar.namazrings.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import one.umar.namazrings.MainActivity
import one.umar.namazrings.R
import one.umar.namazrings.data.SnapshotProvider

class NamazWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    companion object {
        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val options = manager.getAppWidgetOptions(widgetId)
            val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 300)
            val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 140)
            val bitmap = WidgetRenderer.render(
                snapshot = SnapshotProvider.get(context),
                widthDp = widthDp,
                heightDp = heightDp,
                displayDensity = context.resources.displayMetrics.density,
            )
            val views = RemoteViews(context.packageName, R.layout.namaz_widget)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val openApp = Intent(context, MainActivity::class.java)
            val pendingOpen = PendingIntent.getActivity(
                context,
                0,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingOpen)
            manager.updateAppWidget(widgetId, views)
        }
    }
}
