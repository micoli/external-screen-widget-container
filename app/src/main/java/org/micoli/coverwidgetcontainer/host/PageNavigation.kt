package org.micoli.coverwidgetcontainer.host

object PageNavigation {

    fun wrap(current: Int, delta: Int, pageCount: Int): Int {
        if (pageCount <= 0) return 0
        return Math.floorMod(current + delta, pageCount)
    }
}
