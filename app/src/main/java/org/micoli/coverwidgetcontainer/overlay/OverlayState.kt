package org.micoli.coverwidgetcontainer.overlay

// True while the accessibility service is connected: cover widgets then only mark their position.
object OverlayState {
    @Volatile
    var active: Boolean = false
}
