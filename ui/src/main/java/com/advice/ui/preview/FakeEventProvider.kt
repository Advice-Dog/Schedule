package com.advice.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.advice.core.local.Event
import com.advice.core.local.Location
import java.util.Date

class FakeEventProvider : PreviewParameterProvider<Event> {


    override val values: Sequence<Event>
        get() = events.asSequence()


    val events = listOf(
        Event(
            0,
            "DEFCON",
            "Payment Hacking Challenge",
            "Try yourself in ATM, Online bank, POST and Cards hacking challenges.\nPlease join the DEF CON Discord and see the #payv-labs-text channel for more information.",
            Date(),
            Date(Date().time + 8300_000L),
            "",
            listOf(SpeakerProvider.speakers.random()),
            listOf(TagProvider.tags.random()),
            Location(-1, "Main Stage", "Stage", null, "DEFCON"),
            listOf(),
            false
        )

    )
}