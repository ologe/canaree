package dev.olog.service.music

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.nhaarman.mockitokotlin2.*
import org.junit.Test

class NoisyTest {

    private val lifecycle = mock<Lifecycle>()
    private val context = mock<Context>()
    private val eventDispatcher = mock<EventDispatcher>()

    private val noisy = Noisy(
        context, lifecycle, eventDispatcher
    )

    @Test
    fun `test lifecycle subscribe`() {
        verify(lifecycle).addObserver(noisy)
    }

    @Test
    fun `test lifecycle unsubscribe`() {
        val spy = spy(noisy)

        spy.onDestroy(mock())
        verify(spy).unregister()
    }

    @Test
    fun `test register`() {
        noisy.register()

        verify(context).registerReceiver(any(), any())
    }

    @Test
    fun `test multiple register`() {
        noisy.register()
        noisy.register()
        noisy.register()

        verify(context).registerReceiver(any(), any())
    }

    @Test
    fun `test unregister without previous registration`() {
        noisy.unregister()

        verifyZeroInteractions(context)
    }

    @Test
    fun `test unregister success`() {
        noisy.register()
        noisy.unregister()

        verify(context).unregisterReceiver(any())
    }

    @Test
    fun `test multiple unregister`() {
        noisy.register()

        noisy.unregister()
        noisy.unregister()
        noisy.unregister()

        verify(context).unregisterReceiver(any())
    }

}