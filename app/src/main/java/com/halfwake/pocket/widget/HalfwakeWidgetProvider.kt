package com.halfwake.pocket.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.widget.RemoteViews
import com.halfwake.pocket.DiaryStore
import com.halfwake.pocket.Mood
import com.halfwake.pocket.R

class HalfwakeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        ids.forEach { id -> updateWidget(context, mgr, id) }
    }

    companion object {
        private fun paletteFor(mood: String): Pair<Int, Int> = when (mood) {
            Mood.CRITICAL.id -> Color.parseColor("#3A1414") to Color.parseColor("#F2C9C9")
            Mood.BURNING.id -> Color.parseColor("#4A2211") to Color.parseColor("#F4C79C")
            Mood.GROGGY.id -> Color.parseColor("#E9E7E4") to Color.parseColor("#3A3833")
            Mood.FED.id -> Color.parseColor("#E8F0E2") to Color.parseColor("#2E4020")
            Mood.HAPPY.id -> Color.parseColor("#FFF3C4") to Color.parseColor("#4A3B00")
            Mood.SAD.id -> Color.parseColor("#E4E7EF") to Color.parseColor("#33384A")
            Mood.TIRED.id -> Color.parseColor("#EAE6E9") to Color.parseColor("#3A3540")
            Mood.BUSY.id -> Color.parseColor("#F7EDE0") to Color.parseColor("#2C2016")
            Mood.QUIET.id -> Color.parseColor("#1E2126") to Color.parseColor("#D9DDE2")
            Mood.RESTLESS.id -> Color.parseColor("#EFECE9") to Color.parseColor("#2A2A2E")
            else -> Color.parseColor("#F4F1EC") to Color.parseColor("#23211D") // content
        }

        fun updateWidget(context: Context, mgr: AppWidgetManager, id: Int) {
            val latest = DiaryStore.latest(context)
            val views = RemoteViews(context.packageName, R.layout.widget_halfwake)

            val mood = latest?.mood ?: "—"
            val line = latest?.line ?: "Not ticked yet. Open the app once to start."
            val (bg, fg) = paletteFor(mood)

            views.setInt(R.id.widget_root, "setBackgroundColor", bg)
            views.setTextColor(R.id.widget_mood, fg)
            views.setTextColor(R.id.widget_line, fg)
            views.setTextViewText(R.id.widget_mood, mood.uppercase())
            views.setTextViewText(R.id.widget_line, line)

            mgr.updateAppWidget(id, views)
        }

        /** Called by TickWorker right after a new diary entry is written. */
        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, HalfwakeWidgetProvider::class.java))
            ids.forEach { id -> updateWidget(context, mgr, id) }
        }
    }
}
