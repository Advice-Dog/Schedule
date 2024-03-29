package com.advice.locations.data

import com.advice.core.local.Location
import com.advice.core.local.LocationRow
import com.advice.data.sources.LocationsDataSource
import com.advice.locations.data.repositories.LocationRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationRepositoryTest {

    private val tree = listOf(
        Location(
            1, "root", "root", "test", parent = 0,
            children = listOf(
                Location(2, "root-child-1", "child-1", "test", parent = 1),
                Location(3, "root-child-2", "child-2", "test", parent = 1),
                Location(
                    4, "root-child-3", "child-3", "test", parent = 1,
                    children = listOf(
                        Location(5, "grand-child-1", "grand-1", "test", parent = 4)
                    )
                )
            )
        )
    )

    private lateinit var subject: LocationRepository

    @Before
    fun setup() {
        subject = LocationRepository(
            mockk<LocationsDataSource>().apply {
                every { get() } returns flow { emit(tree) }
            }
        )
    }

    @Test
    fun `show all locations by default`() = runTest {
        assertEquals(5, subject.locations.first().size)
    }

    @Test
    fun `collapse root`() = runTest {
        val values = mutableListOf<List<LocationRow>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            subject.locations.toList(values)
        }

        subject.collapse(1)

        assertEquals(1, values[2].size)
    }

    @Test
    fun `collapse child and hide grand-child`() = runTest {
        val values = mutableListOf<List<LocationRow>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            subject.locations.toList(values)
        }

        subject.collapse(4)

        assertEquals(4, values[2].size)
    }
}
