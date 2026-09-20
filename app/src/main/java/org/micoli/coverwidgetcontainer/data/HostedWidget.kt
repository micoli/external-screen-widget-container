package org.micoli.coverwidgetcontainer.data

import kotlinx.serialization.Serializable

@Serializable
data class HostedWidget(
    val appWidgetId: Int,
    val provider: String,
    val label: String,
)
