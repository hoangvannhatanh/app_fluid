package com.lusa.fluidwallpaper.extensions

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.*
import android.os.*
import android.preference.*
import android.text.*
import android.view.*
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.FontRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import com.lusa.fluidwallpaper.R
import java.io.*
import java.text.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

fun String?.formatStringNumber(format: String): String {
    return try {
        this?.run {
            DecimalFormat(format).format(this.toInt())
        } ?: ""
    } catch (ex: NumberFormatException) {
        this ?: ""
    }
}

fun TextView.setColorText(color: Int) {
    this.setTextColor(ContextCompat.getColor(context, color))
}

fun TextView.setStyleText(@FontRes font: Int) {
    this.typeface = ResourcesCompat.getFont(context, font)
}

fun TextView.setTextColorGradient(colorGradient1: String, colorGradient2: String) {
    val paint = this.paint
    val width = paint.measureText(this.text.toString())
    val textShader: Shader = LinearGradient(
        0f, 0f, width, this.textSize, intArrayOf(
            Color.parseColor(colorGradient1),
            Color.parseColor(colorGradient2)
        ), null, Shader.TileMode.REPEAT
    )
    this.paint.shader = textShader
}

fun View.setBackGroundDrawable(drawableRes: Int) {
    this.background = ContextCompat.getDrawable(context, drawableRes)
}

fun Context.showActivity(activity: Class<*>, bundle: Bundle? = null) {
    val intent = Intent(this, activity)
    intent.putExtras(bundle ?: Bundle())
    this.startActivity(intent)
}

fun Activity.showActivity(activity: Class<*>, bundle: Bundle? = null) {
    val intent = Intent(this, activity)
    intent.putExtras(bundle ?: Bundle())
    this.startActivity(intent)
}

fun showActivityAnimationAbove21(context: Activity, intent: Intent, view: View, elementName: String) {
    val options = ActivityOptions.makeSceneTransitionAnimation(
        context,
        view,
        elementName
    )
    context.startActivity(intent, options.toBundle())
}

fun showActivityAnimationNormal(context: Activity, intent: Intent, view: View) {
    val options = ActivityOptionsCompat.makeScaleUpAnimation(
        view,
        view.left,
        view.top,
        view.width,
        view.height
    )
    context.startActivity(intent, options.toBundle())
}

fun showActivityAnimationNormal(context: Activity, view: View, activity: Class<*>, bundle: Bundle? = null) {
    val intent = Intent(context, activity)
    intent.putExtras(bundle ?: Bundle())

    val options = ActivityOptionsCompat.makeScaleUpAnimation(
        view,
        view.left,
        view.top,
        view.width,
        view.height
    )
    context.startActivity(intent, options.toBundle())
}

fun launchActivityAnimationNormal(intent: Intent, view: View, launcher: ActivityResultLauncher<Intent>) {
    val options = ActivityOptionsCompat.makeScaleUpAnimation(
        view,
        view.left,
        view.top,
        view.width,
        view.height
    )
    launcher.launch(intent, options)
}

fun Activity.finishWithAnimation() {
    if (Build.VERSION.SDK_INT >= 34) {
        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.scale_in, R.anim.scale_out)
        finish()
    } else {
        finish()
        overridePendingTransition(R.anim.scale_in, R.anim.scale_out)
    }
}

fun Class<*>.isMyServiceRunning(context: Context): Boolean {
    val manager = context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (this.name == service.service.className) {
            return true
        }
    }
    return false
}

fun View.hide() {
    try {
        this.run {
            this.visibility = View.GONE
        }
    } catch (e: java.lang.Exception) {
        this.run {
            this.visibility = View.GONE
        }
    }
}

fun View.show() {
    try {
        this.run {
            this.visibility = View.VISIBLE
        }
    } catch (e: java.lang.Exception) {
        this.run {
            this.visibility = View.GONE
        }
    }
}

fun View.visible() {
    try {
        this.run {
            this.visibility = View.VISIBLE
        }
    } catch (e: java.lang.Exception) {
        this.run {
            this.visibility = View.GONE
        }
    }
}

fun View.invisible() {
    try {
        this.run {
            this.visibility = View.INVISIBLE
        }
    } catch (e: java.lang.Exception) {
        this.run {
            this.visibility = View.GONE
        }
    }
}

fun setPref(c: Context, pref: String, value: String) {
    val e = PreferenceManager.getDefaultSharedPreferences(c).edit()
    e.putString(pref, value)
    e.apply()

}

fun getPref(c: Context, pref: String, value: String): String? {
    return PreferenceManager.getDefaultSharedPreferences(c).getString(
        pref,
        value
    )
}

fun setPref(c: Context, pref: String, value: Boolean) {
    val e = PreferenceManager.getDefaultSharedPreferences(c).edit()
    e.putBoolean(pref, value)
    e.apply()

}

fun getPref(c: Context, pref: String, value: Boolean): Boolean {
    return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(
        pref, value
    )
}

fun setPref(c: Context, pref: String, value: Int) {
    val e = PreferenceManager.getDefaultSharedPreferences(c).edit()
    e.putInt(pref, value)
    e.apply()

}

fun getPref(c: Context, pref: String, value: Int): Int {
    return PreferenceManager.getDefaultSharedPreferences(c).getInt(
        pref, value
    )
}

fun setPref(c: Context, pref: String, value: Long) {
    val e = PreferenceManager.getDefaultSharedPreferences(c).edit()
    e.putLong(pref, value)
    e.apply()

}

fun getPref(c: Context, pref: String, value: Long): Long {
    return PreferenceManager.getDefaultSharedPreferences(c).getLong(
        pref, value
    )
}


fun <T : View> T.onClickDelay(block: T.() -> Unit) {

    onClick(300, block)
}

private var lastClick = 0L
fun <T : View> T.onClick(delayBetweenClick: Long = 0, block: T.() -> Unit) {
    setOnClickListener {
        when {
            delayBetweenClick <= 0 -> {
                block()
            }

            System.currentTimeMillis() - lastClick > delayBetweenClick -> {
                lastClick = System.currentTimeMillis()
                block()
            }

            else -> {

            }
        }
    }
}

fun Activity.hideKeyboard() {
    val inputMethodManager =
        this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
}

fun Activity.showKeyboard(editText: View) {
    val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Đối với Android 6.0 (API 23) trở lên
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
//        return false
    } else {
        // Đối với Android 5.0-5.1 (API 21-22)
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo
        @Suppress("DEPRECATION")
        return networkInfo != null && networkInfo.isConnected
//        return false
    }
}
fun isMPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

fun isQPlus() = Build.VERSION.SDK_INT > Build.VERSION_CODES.Q

fun is32Minus() = Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2

fun is32Plus() = Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2

fun isOPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

//fun showCustomToast(message: String, activity: Activity) {
//        val toast = Toast(activity)
//        val layout = activity.layoutInflater.inflate (R.layout.layout_custom_toast, activity.findViewById(R.id.toast_container))
//        if (message.isNotEmpty()){
//            val textView = layout.findViewById<TextView>(R.id.toast_text)
//            textView.text = message
//     }
//        toast.apply {
//            setGravity(Gravity.BOTTOM, 0, 50)
//            duration = Toast.LENGTH_SHORT
//            view = layout
//            show()
//    }
//}
//
//fun firebaseAnalyticsEvent(context: Context, key: String, data: String) {
//     if (isNetworkAvailable(context)) {
//         val bundle = Bundle()
//         bundle.putString(key, data)
//         val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
//         analytics.logEvent(key, bundle)
//     }
//}

fun isCameraAvailable(context: Context): Boolean {
    val packageManager = context.packageManager
    return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
}

fun isSingleCLick(): Boolean {
    return isClickEvent(300)
}

private var mOldClickTime: Long = 0
fun isClickEvent(timeDelay: Long): Boolean {
    val time = System.currentTimeMillis()
    if (time - mOldClickTime < timeDelay) return false
    mOldClickTime = time
    return true
}

fun setTextColorInString(context: Context, textColor: String, textNoColor: String, color: Int): Spanned {
    return HtmlCompat.fromHtml("<font color='${ContextCompat.getColor(context, color)}'>$textColor</font> $textNoColor", HtmlCompat.FROM_HTML_MODE_LEGACY)
}

//fun isShowInter15s(context: Context): Boolean {
//    val timeOffResume15s = getPref(context, TURN_ON_OFF_INTER_15S, 0L) ?: 0L
//    val timeDelay = (RemoteConfig.interval_show_inter.toString() + "000").toLong()
//    return try {
//        Calendar.getInstance().timeInMillis > (timeOffResume15s + timeDelay)
//    } catch (e: Exception) {
//        return true
//    }
//}

inline fun <T> tryOrCatch(blockTry: () -> T, blockCatch: () -> T): T {
    return try {
        blockTry.invoke()
    } catch (e: Exception) {
        blockCatch.invoke()
    }
}
fun Activity.shakeView(view: ImageView?) {
    val shakeAnimator = ObjectAnimator.ofFloat(view, "translationX", 0f, 15f, -15f, 15f, -15f, 10f, -10f, 3f, -3f, 0f)
    shakeAnimator.duration = 300
    shakeAnimator.start()
}

fun Double.formatNumber(): String {
    val decimalFormat = DecimalFormat("#,###,###,###,###,###,###,###,###.##")
    return decimalFormat.format(this)
}

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return tryOrCatch(
        blockTry = {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        }
    ) {
        null
    }
}
fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
    val contextWrapper = ContextWrapper(context)
    val directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE)
    val file = File(directory, "avatar.jpg")
    tryOrCatch(
        blockTry = {
            val outputStream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        }
    ) {}
    return file.absolutePath
}
fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, nameImage: String): String {
    val contextWrapper = ContextWrapper(context)
    val directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE)
    val file = File(directory, "${nameImage}.jpg")
    tryOrCatch(
        blockTry = {
            val outputStream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        }
    ) {}
    return file.absolutePath
}
fun getBitmapFromInternalStorage(path: String): Bitmap? {
    return tryOrCatch(
        blockTry = {
            val file = File(path)
            BitmapFactory.decodeStream(FileInputStream(file))
        },
        blockCatch = {
            null
        }
    )
}
fun convertToCalendarFromDate(year: Int, month: Int, day: Int): Calendar? {
    val calendar = Calendar.getInstance()
    calendar[year, month] = day
    return calendar
}
fun Long.convertDateToString(format: String): String {
    val date = Date(this)
    val dateFormat = SimpleDateFormat(format, Locale.US)
    return dateFormat.format(date)
}

fun convertDateBasedOnAndroidVersion(inputDate: String): String? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
            val date = LocalDate.parse(inputDate, inputFormatter)
            date.format(outputFormatter)
        } catch (e: Exception) {
            null
        }
    } else {
        try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
            val date = inputFormat.parse(inputDate)
            outputFormat.format(date)
        } catch (e: Exception) {
            null
        }
    }
}

fun Activity.hideNavigation() {
    window?.decorView?.systemUiVisibility =
        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
}

fun animateSelection(views: List<View>, onEnd: (() -> Unit)? = null) {
    val interpolator = OvershootInterpolator(2f)
    val lastIndex = views.lastIndex
    views.forEachIndexed { index, v ->
        v.scaleX = 0.3f
        v.scaleY = 0.3f
        val animator = v.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(70)
            .setInterpolator(interpolator)
        if (index == lastIndex && onEnd != null) {
            animator.withEndAction { onEnd.invoke() }
        }
        animator.start()
    }
}