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


    override fun onDown(p0: MotionEvent?): Boolean {
//        TODO("Not yet implemented")

        return false
    }

    override fun onShowPress(p0: MotionEvent?) {
//        TODO("Not yet implemented")
    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
//        TODO("Not yet implemented")

        return false
    }

    override fun onLongPress(p0: MotionEvent?) {
//        TODO("Not yet implemented")
    }

    override fun onFling(m0: MotionEvent?, m1: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
//        super.onFling(m0, m1, velocityX, velocityY)

        //TODO: https://developer.android.com/training/gestures/scroll

        Log.d(TAG,"onFling velocity x: $velocityX")
        Log.d(TAG," onFling velocity y: $velocityY")

        return false
    }

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
                SimpleChannel("npo-1", SpannedString("NPO 1"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("npo-2", SpannedString("NPO 2"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("bbc-news", SpannedString("BBC NEWS"), "https://upload.wikimedia.org/wikipedia/commons/thumb/6/62/BBC_News_2019.svg/200px-BBC_News_2019.svg.png"),
                SimpleChannel("zdf", SpannedString("ZDF"), "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c1/ZDF_logo.svg/200px-ZDF_logo.svg.png"),
                SimpleChannel("jednotka", SpannedString("Jednotka"), "https://upload.wikimedia.org/wikipedia/en/thumb/7/76/Jednotka.svg/255px-Jednotka.svg.png"),
                SimpleChannel("tv-nova", SpannedString("TV nova"), "https://upload.wikimedia.org/wikipedia/commons/2/2f/TV_Nova_logo_2017.png"),
                SimpleChannel("tv-5-monde", SpannedString("TV5MONDE"), "https://upload.wikimedia.org/wikipedia/commons/thumb/4/42/TV5MONDE_logo.png/320px-TV5MONDE_logo.png"),
                SimpleChannel("orf-2", SpannedString("ORF 2"), "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e7/ORF2_logo_n.svg/320px-ORF2_logo_n.svg.png"),
                SimpleChannel("tvp-1", SpannedString("TVP 1"), "https://upload.wikimedia.org/wikipedia/commons/e/ec/Tvp1.png"),

                SimpleChannel("npo-10", SpannedString("NPO 10"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("npo-11", SpannedString("NPO 11"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("npo-12", SpannedString("NPO 12"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("npo-13", SpannedString("NPO 13"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("npo-14", SpannedString("NPO 14"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("npo-15", SpannedString("NPO 15"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("npo-16", SpannedString("NPO 16"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("npo-17", SpannedString("NPO 17"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("npo-18", SpannedString("NPO 18"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("npo-19", SpannedString("NPO 19"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),

                SimpleChannel("npo-20", SpannedString("NPO 20"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("npo-21", SpannedString("NPO 21"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("npo-22", SpannedString("NPO 22"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("npo-23", SpannedString("NPO 23"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("npo-24", SpannedString("NPO 24"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("npo-25", SpannedString("NPO 25"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("npo-26", SpannedString("NPO 26"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("npo-27", SpannedString("NPO 27"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
                SimpleChannel("npo-28", SpannedString("NPO 28"), "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/NPO_1_logo_2014.svg/320px-NPO_1_logo_2014.svg.png"),
                SimpleChannel("npo-29", SpannedString("NPO 29"), "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/NPO_2_logo_2014.svg/275px-NPO_2_logo_2014.svg.png"),
            )

            val showNames = listOf("News", "Sherlock Holmes", "It's Always Sunny In Philadelphia", "Second World War Documentary", "World Cup Final Replay", "Game of Thrones",
                "NFL Sunday Night Football", "NCIS", "Seinfeld", "ER", "Who Wants To Be A Millionaire", "Our Planet", "Friends", "House of Cards")

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
