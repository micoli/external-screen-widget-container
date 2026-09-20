package org.micoli.coverwidgetcontainer.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.micoli.coverwidgetcontainer.R

private const val GOOD_LOCK_PACKAGE = "com.samsung.android.goodlock"

@Composable
fun SetupHelpScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val goodLockIntent = context.packageManager.getLaunchIntentForPackage(GOOD_LOCK_PACKAGE)

    ScreenScaffold(title = stringResource(R.string.help_title), onBack = onBack) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.help_body))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }) {
                    Text(stringResource(R.string.help_open_settings))
                }
                OutlinedButton(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                    Text(stringResource(R.string.help_open_accessibility))
                }
                if (goodLockIntent != null) {
                    OutlinedButton(onClick = { context.startActivity(goodLockIntent) }) {
                        Text(stringResource(R.string.help_open_goodlock))
                    }
                }
            }
        }
    }
}
