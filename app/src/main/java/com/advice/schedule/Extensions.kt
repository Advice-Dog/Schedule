package com.advice.schedule

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.transition.Fade
import com.advice.core.firebase.FirebaseAction
import com.advice.core.firebase.FirebaseArticle
import com.advice.core.firebase.FirebaseConference
import com.advice.core.firebase.FirebaseEvent
import com.advice.schedule.models.firebase.*
import com.advice.schedule.models.local.*
import com.advice.schedule.utilities.Time
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.advice.core.local.Conference
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

fun Date.isToday(): Boolean {
    val current = Calendar.getInstance().now()

    val cal = Calendar.getInstance()
    cal.time = this

    return cal.get(Calendar.YEAR) == current.get(Calendar.YEAR)
            && cal.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR)
}

fun Date.isTomorrow(): Boolean {
    val cal1 = Calendar.getInstance().now()
    cal1.roll(Calendar.DAY_OF_YEAR, true)

    val cal2 = Calendar.getInstance()
    cal2.time = this

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(
        Calendar.DAY_OF_YEAR
    )
}

fun Date.getDateDifference(date: Date, timeUnit: TimeUnit): Long {
    return timeUnit.convert(date.time - this.time, TimeUnit.MILLISECONDS)
}


fun Calendar.now(): Calendar {
    this.time = Time.now()
    return this
}

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int) {
    supportFragmentManager.inTransaction { add(frameId, fragment) }
}


fun AppCompatActivity.replaceFragment(
    fragment: Fragment,
    frameId: Int,
    hasAnimation: Boolean = false,
    backStack: Boolean = true
) {
    supportFragmentManager.inTransaction {

        if (hasAnimation) {

            val fadeDuration = 300L


            fragment.apply {
                enterTransition = Fade().apply {
                    duration = fadeDuration
                }


                returnTransition = Fade().apply {
                    duration = fadeDuration
                }
            }
        }

        val transaction = replace(frameId, fragment)
        if (backStack) {
            transaction.addToBackStack(null)
        }
        return@inTransaction transaction
    }
}

fun FirebaseConference.toConference(): Conference? {
    return try {
        Conference(
            id,
            name,
            description,
            codeofconduct,
            supportdoc,
            code,
            maps,
            start_timestamp.toDate(),
            end_timestamp.toDate(),
            timezone
        )
    } catch (ex: Exception) {
        Timber.e("Could not map data to Conference: ${ex.message}")
        null
    }
}

fun FirebaseType.toType(): Type? {
    return try {
        val actions = ArrayList<Action>()
        discord_url?.let {
            if (it?.isNotBlank() == true) {
                actions.add(Action(Action.getLabel(it), it))
            }
        }

        subforum_url?.let {
            if (it?.isNotBlank() == true) {
                actions.add(Action(Action.getLabel(it), it))
            }
        }

        Type(
            id,
            name,
            conference,
            color,
            description,
            actions
        )
    } catch (ex: Exception) {
        Timber.e("Could not map data to Location: ${ex.message}")
        null
    }
}

fun FirebaseLocation.toLocation(): Location? {
    return try {
        Location(
            id,
            name,
            short_name,
            hotel,
            conference,
            default_status, hier_depth, hier_extent_left, hier_extent_right, parent_id, peer_sort_order, schedule
        )
    } catch (ex: Exception) {
        Timber.e("Could not map data to Location: ${ex.message}")
        null
    }
}

fun FirebaseEvent.toEvent(tags: List<FirebaseTagType>): Event? {
    try {
        val list = tags.flatMap { it.tags.sortedBy { it.sort_order } }

        val links = links.map { it.toAction() }
        val types = tag_ids.mapNotNull { id ->
            list.find { it.id == id }
        }.sortedBy { list.indexOf(it) }

        return Event(
            id,
            conference,
            title,
            android_description,
            begin_timestamp,
            end_timestamp,
            //todo:
            updated_timestamp.seconds.toString(),
            speakers.mapNotNull { it.toSpeaker() },
            types,
            location.toLocation()!!,
            links
        )
    } catch (ex: Exception) {
        Timber.e("Could not map data to Event: ${ex.message}")
        return null
    }
}

fun Timestamp.toDate(): Date {
    return Date(seconds * 1000)
}

private fun FirebaseAction.toAction() =
    Action(this.label, this.url)

fun FirebaseSpeaker.toSpeaker(): Speaker? {
    return try {
        Speaker(
            id,
            name,
            description,
            link,
            twitter,
            title
        )
    } catch (ex: Exception) {
        Timber.e("Could not map data to Speaker: ${ex.message}")
        null
    }
}

fun FirebaseVendor.toVendor(): Vendor? {
    return try {
        Vendor(
            id,
            name,
            description,
            link,
            partner
        )
    } catch (ex: Exception) {
        Timber.e("Could not map data to Vendor: ${ex.message}")
        null
    }
}

fun FirebaseArticle.toArticle(): Article? {
    return try {
        Article(
            id,
            name,
            text
        )
    } catch (ex: Exception) {
        Timber.e("Could not map data to Article: ${ex.message}")
        null
    }
}

fun FirebaseFAQ.toFAQ(isExpanded: Boolean = false): Pair<FAQQuestion, FAQAnswer>? {
    return try {
        FAQQuestion(id, question, isExpanded) to FAQAnswer(id, answer, isExpanded)
    } catch (ex: Exception) {
        Timber.e("Could not map data to FAQ: ${ex.message}")
        null
    }
}

fun FragmentActivity.showKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun FragmentActivity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = currentFocus ?: View(this)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun <T> List<Fragment>.get(clazz: Class<T>): T {
    return first { it::class.java == clazz } as T
}

fun <T> QuerySnapshot.toObjectsOrEmpty(@NonNull clazz: Class<T>): List<T> {
    return try {
        toObjects(clazz)
    } catch (ex: Exception) {
        Timber.e("Could not map data to objects: ${ex.message}")
        return emptyList()
    }
}

fun <T> DocumentSnapshot.toObjectOrNull(@NonNull clazz: Class<T>): T? {
    return try {
        toObject(clazz)
    } catch (ex: Exception) {
        Timber.e("Could not map data to objects: ${ex.message}")
        return null
    }
}

inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })
}

val Number.toPx
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

fun Context.getTintedDrawable(resource: Int, tint: Int): Drawable? {
    return ContextCompat.getDrawable(this, resource)?.mutate()?.apply {
        setTint(tint)
    }
}