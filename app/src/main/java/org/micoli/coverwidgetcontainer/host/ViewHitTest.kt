package org.micoli.coverwidgetcontainer.host

import android.view.View
import android.view.ViewGroup

object ViewHitTest {

    fun findClickable(view: View, x: Float, y: Float): View? {
        if (view.visibility != View.VISIBLE) return null
        if (view is ViewGroup) {
            for (i in view.childCount - 1 downTo 0) {
                val child = view.getChildAt(i)
                val childX = x + view.scrollX - child.left
                val childY = y + view.scrollY - child.top
                if (childX < 0 || childY < 0 || childX >= child.width || childY >= child.height) continue
                findClickable(child, childX, childY)?.let { return it }
            }
        }
        return view.takeIf { it.isClickable && it.isEnabled }
    }
}
