package io.github.vednolacni.encore.dream

import android.service.dreams.DreamService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import io.github.vednolacni.encore.graph
import io.github.vednolacni.encore.render.ScreenSpec
import io.github.vednolacni.encore.ui.EncoreRoot
import io.github.vednolacni.encore.ui.RootHooks
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Encore as the system screensaver: when the TV would otherwise show its ambient mode,
 * it shows what is playing, or the quiet clock. Any key ends the dream (non-interactive).
 * Once nothing has played for the "turn off after" time it ends itself and the TV may sleep:
 * a clock that stays for hours would mark an OLED. On the TV device it can only be chosen via
 * ADB: `adb shell settings put secure screensaver_components
 * io.github.vednolacni.encore/.dream.EncoreDream`.
 */
class EncoreDream : DreamService(), LifecycleOwner, SavedStateRegistryOwner {
    private val registry = LifecycleRegistry(this)
    private val savedState = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = registry
    override val savedStateRegistry: SavedStateRegistry get() = savedState.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedState.performRestore(null)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        isScreenBright = false
        window.decorView.setViewTreeLifecycleOwner(this)
        window.decorView.setViewTreeSavedStateRegistryOwner(this)
        val controller = graph.controller
        val spec = ScreenSpec.of(resources.displayMetrics)
        setContentView(
            ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@EncoreDream)
                setViewTreeSavedStateRegistryOwner(this@EncoreDream)
                setContent {
                    val ui by controller.ui.collectAsStateWithLifecycle()
                    val hooks = remember { RootHooks(positionOf = controller::positionOf, fieldTime = controller::fieldTime) }
                    // Any key ends the dream: it shows no buttons to choose among.
                    EncoreRoot(ui.copy(buttons = null), spec, hooks)
                }
            },
        )
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        graph.controller.attach(ScreenSpec.of(resources.displayMetrics))
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                graph.controller.ui.first { it.asleep }
                finish()
            }
        }
    }

    override fun onDreamingStopped() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        graph.controller.detach()
        super.onDreamingStopped()
    }

    override fun onDestroy() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }
}
