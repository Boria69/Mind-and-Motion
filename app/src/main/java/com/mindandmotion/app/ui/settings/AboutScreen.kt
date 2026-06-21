package com.mindandmotion.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindandmotion.app.ui.components.AppTopBar

/**
 * @param appVersion implicit "1.0"; [TU] poate trimite BuildConfig.VERSION_NAME
 *   dacă activează `buildFeatures.buildConfig = true` în app/build.gradle.kts.
 */
@Composable
fun AboutScreen(
    appVersion: String = "1.0",
    onBack: (() -> Unit)? = null
) {
    Scaffold(topBar = { AppTopBar(title = "Despre", onBack = onBack) }) { padding ->
        Column(
            Modifier.padding(padding).padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Mind & Motion",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text("Versiunea $appVersion", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Text(
                "O aplicație simplă de productivitate: task-uri, journal zilnic și " +
                    "Pomodoro, toate într-un singur loc, fără cont, fără cloud — datele " +
                    "tale rămân pe telefon.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Text("Realizat de echipa Mind & Motion, 2026.", style = MaterialTheme.typography.labelMedium)
        }
    }
}
