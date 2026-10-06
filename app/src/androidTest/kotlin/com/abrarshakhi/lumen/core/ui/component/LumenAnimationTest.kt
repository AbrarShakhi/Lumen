package com.abrarshakhi.lumen.core.ui.component

import androidx.test.platform.app.InstrumentationRegistry
import com.airbnb.lottie.LottieCompositionFactory
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LumenAnimationTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun everyBundledAnimationParses() {
        LumenAnimation.entries.forEach { animation ->
            val result = LottieCompositionFactory.fromRawResSync(context, animation.resource)

            assertNull(result.exception, "${animation.name} failed to parse")
            val composition = assertNotNull(result.value, "${animation.name} produced no composition")
            assertTrue(composition.layers.isNotEmpty(), "${animation.name} has no layers")
            assertTrue(composition.duration > 0f, "${animation.name} has no duration")
        }
    }
}
