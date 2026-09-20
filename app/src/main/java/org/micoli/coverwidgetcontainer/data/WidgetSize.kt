package org.micoli.coverwidgetcontainer.data

import kotlinx.serialization.Serializable

// Size of a widget on the 2x2 page grid, as rows x columns.
@Serializable
enum class WidgetSize(val rows: Int, val columns: Int, val label: String) {
    ONE_BY_ONE(1, 1, "1x1"),
    ONE_BY_TWO(1, 2, "1x2"),
    TWO_BY_TWO(2, 2, "2x2"),
    ;

    companion object {
        val DEFAULT = ONE_BY_TWO
    }
}
