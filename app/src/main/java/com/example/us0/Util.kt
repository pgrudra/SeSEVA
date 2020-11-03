package com.example.us0

import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.us0.data.AppAndCategory

fun formatApps(apps:List<AppAndCategory>,resources:Resources):Spanned{
    val sb=StringBuilder()
    sb.apply {
        append(resources.getString(R.string.installed_apps_list))
        for (it in apps) {
            append("<br>")
            append(resources.getString((R.string.installed_app_name)))
            append("${it.appName}<br>")
            append(resources.getString(R.string.installed_app_package_name))
            append("${it.packageName}<br>")
            append("<br>")
        }
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY)
    } else {
        HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

}
class TextItemViewHolder(val textView: TextView): RecyclerView.ViewHolder(textView)
