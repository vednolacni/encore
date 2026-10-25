package io.github.vednolacni.encore.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.vednolacni.encore.domain.CheckedRelease
import kotlinx.coroutines.flow.first

private val Context.updateStore: DataStore<Preferences> by preferencesDataStore(name = "updates")

/** What GitHub last said about Encore's newest release, kept apart from the settings. */
class UpdateStore(context: Context) {
    private val store = context.applicationContext.updateStore

    /** What GitHub said last time, if it has ever answered. */
    suspend fun recall(): CheckedRelease? {
        val p = store.data.first()
        val at = p[CHECKED_AT] ?: return null
        val tag = p[LATEST] ?: return null
        return CheckedRelease(at, tag)
    }

    suspend fun remember(checked: CheckedRelease) {
        store.edit {
            it[CHECKED_AT] = checked.atMinutes
            it[LATEST] = checked.tag
        }
    }

    private companion object {
        val CHECKED_AT = intPreferencesKey("checked_at")
        val LATEST = stringPreferencesKey("latest")
    }
}
