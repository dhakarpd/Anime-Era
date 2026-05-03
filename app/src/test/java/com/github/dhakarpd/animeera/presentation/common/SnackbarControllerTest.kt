package com.github.dhakarpd.animeera.presentation.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SnackbarControllerTest {

    @Test
    fun `sendEvent should emit event to flow`() = runTest {
        val event = SnackbarEvent(message = "Test Message")

        // Using launch with UnconfinedTestDispatcher to start collection immediately
        val events = mutableListOf<SnackbarEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            SnackbarController.events.collect {
                events.add(it)
            }
        }

        SnackbarController.sendEvent(event)

        assertEquals(1, events.size)
        assertEquals("Test Message", events[0].message)

        job.cancel()
    }

    @Test
    fun `sendEvent with action should emit event with action`() = runTest {
        var actionCalled = false
        val action = SnackbarAction(
            name = "Retry",
            action = { actionCalled = true }
        )
        val event = SnackbarEvent(message = "Error", action = action)

        val events = mutableListOf<SnackbarEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            SnackbarController.events.collect {
                events.add(it)
            }
        }

        SnackbarController.sendEvent(event)

        assertEquals(1, events.size)
        assertEquals("Retry", events[0].action?.name)
        
        // Execute the action
        events[0].action?.action?.invoke()
        assertEquals(true, actionCalled)

        job.cancel()
    }

    @Test
    fun `multiple sendEvent calls should emit events in order`() = runTest {
        val event1 = SnackbarEvent(message = "Message 1")
        val event2 = SnackbarEvent(message = "Message 2")

        val events = mutableListOf<SnackbarEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            SnackbarController.events.collect {
                events.add(it)
            }
        }

        SnackbarController.sendEvent(event1)
        SnackbarController.sendEvent(event2)

        assertEquals(2, events.size)
        assertEquals("Message 1", events[0].message)
        assertEquals("Message 2", events[1].message)

        job.cancel()
    }
}
