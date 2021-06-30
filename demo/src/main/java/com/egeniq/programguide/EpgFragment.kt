package com.egeniq.programguide

import android.annotation.SuppressLint
import android.text.Spanned
import android.text.SpannedString
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.egeniq.androidtvprogramguide.ProgramGuideFragment
import com.egeniq.androidtvprogramguide.R
import com.egeniq.androidtvprogramguide.entity.ProgramGuideChannel
import com.egeniq.androidtvprogramguide.entity.ProgramGuideSchedule
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.lang.Error
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class EpgFragment : ProgramGuideFragment<EpgFragment.SimpleProgram>() {

//    override fun onFling(m0: MotionEvent?, m1: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
////        super.onFling(m0, m1, velocityX, velocityY)
//
//        //TODO: https://developer.android.com/training/gestures/scroll
//
//        Log.d(TAG,"onFling velocity x: $velocityX")
//        Log.d(TAG," onFling velocity y: $velocityY")
//
//        return false
//    }

    // Feel free to change configuration values like this:
    //
    // override val DISPLAY_CURRENT_TIME_INDICATOR = false
    // override val DISPLAY_SHOW_PROGRESS = false

    companion object {
        private val TAG = EpgFragment::class.java.name
    }

    data class SimpleChannel(
        override val id: String,
        override val name: Spanned?,
        override val imageUrl: String?) : ProgramGuideChannel

    // You can put your own data in the program class
    data class SimpleProgram(
        val id: String,
        val description: String,
        val metadata: String
    )

    override fun onScheduleClicked(programGuideSchedule: ProgramGuideSchedule<SimpleProgram>) {
        val innerSchedule = programGuideSchedule.program
        if (innerSchedule == null) {
            // If this happens, then our data source gives partial info
            Log.w(TAG, "Unable to open schedule: $innerSchedule")
            return
        }
        if (programGuideSchedule.isCurrentProgram) {
            Toast.makeText(context, "Open live player", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Open detail page", Toast.LENGTH_LONG).show()
        }
        // Example of how a program can be updated. You could also change the underlying program.
        updateProgram(programGuideSchedule.copy(displayTitle = programGuideSchedule.displayTitle + " [clicked]"))
    }

    override fun onScheduleSelected(programGuideSchedule: ProgramGuideSchedule<SimpleProgram>?) {
        val titleView = view?.findViewById<TextView>(R.id.programguide_detail_title)
        titleView?.text = programGuideSchedule?.displayTitle
        val metadataView = view?.findViewById<TextView>(R.id.programguide_detail_metadata)
        metadataView?.text = programGuideSchedule?.program?.metadata
        val descriptionView = view?.findViewById<TextView>(R.id.programguide_detail_description)
        descriptionView?.text = programGuideSchedule?.program?.description
        val imageView = view?.findViewById<ImageView>(R.id.programguide_detail_image) ?: return
        if (programGuideSchedule != null) {
            Glide.with(imageView)
                .load("https://picsum.photos/462/240?random=" + programGuideSchedule.displayTitle.hashCode())
                .centerCrop()
                .error(R.drawable.programguide_icon_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(withCrossFade())
                .into(imageView)
        } else {
            Glide.with(imageView).clear(imageView)
        }
    }

    override fun isTopMenuVisible(): Boolean {
        return false
    }

    @SuppressLint("CheckResult")
    override fun requestingProgramGuideFor(localDate: LocalDate) {
        // Faking an asynchronous loading here
        setState(State.Loading)

        val MIN_CHANNEL_START_TIME = localDate.atStartOfDay().withHour(2).truncatedTo(ChronoUnit.HOURS).atZone(DISPLAY_TIMEZONE)
        val MAX_CHANNEL_START_TIME = localDate.atStartOfDay().withHour(8).truncatedTo(ChronoUnit.HOURS).atZone(DISPLAY_TIMEZONE)

        val MIN_CHANNEL_END_TIME = localDate.atStartOfDay().withHour(21).truncatedTo(ChronoUnit.HOURS).atZone(DISPLAY_TIMEZONE)
        val MAX_CHANNEL_END_TIME = localDate.plusDays(1).atStartOfDay().withHour(4).truncatedTo(ChronoUnit.HOURS).atZone(DISPLAY_TIMEZONE)

        val MIN_SHOW_LENGTH_SECONDS = TimeUnit.MINUTES.toSeconds(5)
        val MAX_SHOW_LENGTH_SECONDS = TimeUnit.MINUTES.toSeconds(120)


        Single.fromCallable {
            val channels = listOf(
                SimpleChannel("SUN-1", SpannedString("SUN 1"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-2", SpannedString("SUN 2"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("SUN-3", SpannedString("SUN 3"), "https://upload.wikimedia.org/wikipedia/commons/thumb/6/62/BBC_News_2019.svg/200px-BBC_News_2019.svg.png"),
                SimpleChannel("SUN-4", SpannedString("SUN 4"), "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c1/ZDF_logo.svg/200px-ZDF_logo.svg.png"),
                SimpleChannel("SUN-5", SpannedString("SUN 5"), "https://upload.wikimedia.org/wikipedia/en/thumb/7/76/Jednotka.svg/255px-Jednotka.svg.png"),
                SimpleChannel("SUN-6", SpannedString("SUN 6"), "https://upload.wikimedia.org/wikipedia/commons/2/2f/TV_Nova_logo_2017.png"),
                SimpleChannel("SUN-7", SpannedString("SUN 7"), "https://upload.wikimedia.org/wikipedia/commons/thumb/4/42/TV5MONDE_logo.png/320px-TV5MONDE_logo.png"),
                SimpleChannel("SUN-8", SpannedString("SUN 8"), "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e7/ORF2_logo_n.svg/320px-ORF2_logo_n.svg.png"),
                SimpleChannel("SUN-9", SpannedString("SUN 9"), "https://upload.wikimedia.org/wikipedia/commons/e/ec/Tvp1.png"),

                SimpleChannel("SUN-10", SpannedString("SUN 10"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-11", SpannedString("SUN 11"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("SUN-12", SpannedString("SUN 12"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-13", SpannedString("SUN 13"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("SUN-14", SpannedString("SUN 14"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-15", SpannedString("SUN 15"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("SUN-16", SpannedString("SUN 16"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-17", SpannedString("SUN 17"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("SUN-18", SpannedString("SUN 18"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-19", SpannedString("SUN 19"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),

                SimpleChannel("SUN-20", SpannedString("SUN 20"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-21", SpannedString("SUN 21"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("SUN-22", SpannedString("SUN 22"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-23", SpannedString("SUN 23"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("SUN-24", SpannedString("SUN 24"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-25", SpannedString("SUN 25"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("SUN-26", SpannedString("SUN 26"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-27", SpannedString("SUN 27"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("SUN-28", SpannedString("SUN 28"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-29", SpannedString("SUN 29"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),

                SimpleChannel("SUN-30", SpannedString("SUN 30"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-31", SpannedString("SUN 31"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("SUN-32", SpannedString("SUN 32"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-33", SpannedString("SUN 33"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("SUN-34", SpannedString("SUN 34"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-35", SpannedString("SUN 35"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("SUN-36", SpannedString("SUN 36"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-37", SpannedString("SUN 37"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("SUN-38", SpannedString("SUN 38"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("SUN-39", SpannedString("SUN 39"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png")
            )

            val showNames = listOf("Sport Passion", "Sherlock Holmes", "Kitchen Talk", "Overhaulin", "World Cup", "Game of Thrones",
                "Euro 2020", "The Autobahn A2", "News und Meteo", "Découverte", "Deadliest Catch", "Our Planet", "Friends", "Sweet Tooth")

            val channelMap = mutableMapOf<String, List<ProgramGuideSchedule<SimpleProgram>>>()

            channels.forEach { channel ->
                val scheduleList = mutableListOf<ProgramGuideSchedule<SimpleProgram>>()
                var nextTime = randomTimeBetween(MIN_CHANNEL_START_TIME, MAX_CHANNEL_START_TIME)
                while (nextTime.isBefore(MIN_CHANNEL_END_TIME)) {
                    val endTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextTime.toEpochSecond() + Random.nextLong(MIN_SHOW_LENGTH_SECONDS, MAX_SHOW_LENGTH_SECONDS)), ZoneOffset.UTC)
                    val schedule = createSchedule(showNames.random(), nextTime, endTime)
                    scheduleList.add(schedule)
                    nextTime = endTime
                }
                val endTime = if (nextTime.isBefore(MAX_CHANNEL_END_TIME)) randomTimeBetween(nextTime, MAX_CHANNEL_END_TIME) else MAX_CHANNEL_END_TIME
                val finalSchedule = createSchedule(showNames.random(), nextTime, endTime)
                scheduleList.add(finalSchedule)
                channelMap[channel.id] = scheduleList
            }
            return@fromCallable Pair(channels, channelMap)
        }.delay(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                setData(it.first, it.second, localDate)
                if (it.first.isEmpty() || it.second.isEmpty()) {
                    setState(State.Error("No channels loaded."))
                } else {
                    setState(State.Content)
                }
            }, {
                Log.e(TAG, "Unable to load example data!", it)
            })
    }

    private fun createSchedule(scheduleName: String, startTime: ZonedDateTime, endTime: ZonedDateTime): ProgramGuideSchedule<SimpleProgram> {
        val id = Random.nextLong(100_000L)
        val metadata = DateTimeFormatter.ofPattern("'Starts at' HH:mm").format(startTime)
        return ProgramGuideSchedule.createScheduleWithProgram(
            id,
            startTime.toInstant(),
            endTime.toInstant(),
            true,
            scheduleName,
            SimpleProgram(id.toString(),
                "This is an example description for the programme. This description is taken from the SimpleProgram class, so by using a different class, " +
                        "you could easily modify the demo to use your own class",
                metadata)
        )
    }

    private fun randomTimeBetween(min: ZonedDateTime, max: ZonedDateTime): ZonedDateTime {
        val randomEpoch = Random.nextLong(min.toEpochSecond(), max.toEpochSecond())
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(randomEpoch), ZoneOffset.UTC)
    }


    override fun requestRefresh() {
        // You can refresh other data here as well.
        requestingProgramGuideFor(currentDate)
    }

}
