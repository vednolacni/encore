package io.github.vednolacni.encore.app

import io.github.vednolacni.encore.domain.CheckedRelease
import io.github.vednolacni.encore.domain.UpdateInfo
import io.github.vednolacni.encore.domain.UpdateState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Encore's own new versions. As a screen starts showing it, it asks GitHub for the newest
 * release if GitHub last answered more than a day ago ([checkIfDue]), when that is turned on.
 * The menu's Update line tells what it knows; OK there downloads the new version and hands it to
 * Android's installer, which installs it once the person agrees on the TV. What GitHub said is
 * kept ([recall], [remember]). A build of one's own looks for nothing.
 */
class Updates(
    private val scope: CoroutineScope,
    /** The version installed, as Android names it. */
    version: String,
    /** The newest release's tag, as GitHub names it; null when it names none this app reads. */
    private val latest: suspend () -> String?,
    /** Downloads the release with the tag given and has it installed; false when that can't be done. */
    private val install: suspend (String) -> Boolean,
    private val recall: suspend () -> CheckedRelease?,
    private val remember: suspend (CheckedRelease) -> Unit,
    private val now: () -> Long,
    private val log: (String) -> Unit = {},
) {
    private val _info = MutableStateFlow(UpdateInfo(name = version))
    val info: StateFlow<UpdateInfo> = _info.asStateFlow()

    /** Asks GitHub, unless it answered less than a day ago. */
    fun checkIfDue() {
        if (_info.value.installed == null) return
        scope.launch {
            val kept = recall()
            if (kept != null) _info.update { it.copy(latest = it.latest ?: kept.tag) }
            if (kept == null || minutes() - kept.atMinutes !in 0 until DAY_MINUTES) check()
        }
    }

    /** OK on the menu's line: installs the new version, or asks GitHub now while there is none. */
    fun ok() {
        val info = _info.value
        if (info.installed == null) return
        val tag = info.available
        scope.launch { if (tag != null) download(tag) else check() }
    }

    private suspend fun check() {
        if (!begin(UpdateState.CHECKING)) return
        try {
            val tag = latest()
            if (tag != null) {
                remember(CheckedRelease(minutes(), tag))
                _info.update { it.copy(latest = tag) }
                log("Update: GitHub's newest release is $tag")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log("Update: no answer from GitHub (${e.javaClass.simpleName})")
        } finally {
            _info.update { it.copy(state = UpdateState.IDLE) }
        }
    }

    private suspend fun download(tag: String) {
        if (!begin(UpdateState.DOWNLOADING)) return
        var handed = false
        try {
            handed = install(tag)
            if (!handed) log("Update: $tag could not be downloaded or installed")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log("Update: $tag could not be downloaded (${e.javaClass.simpleName})")
        } finally {
            _info.update { it.copy(state = if (handed) UpdateState.IDLE else UpdateState.FAILED) }
        }
    }

    /** Starts [state], unless a question or a download already runs. */
    private fun begin(state: UpdateState): Boolean {
        while (true) {
            val was = _info.value
            if (was.state == UpdateState.CHECKING || was.state == UpdateState.DOWNLOADING) return false
            if (_info.compareAndSet(was, was.copy(state = state))) return true
        }
    }

    private fun minutes(): Int = (now() / 60_000L).toInt()

    private companion object {
        /** GitHub is asked again a day after it answered. */
        const val DAY_MINUTES = 24 * 60
    }
}
