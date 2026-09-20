package org.micoli.coverwidgetcontainer.data

import java.util.Locale

object ContainerIndex {
    const val COUNT = 20

    private val providerClassPattern = Regex("""Container(\d{2})Provider$""")

    fun providerClassName(packageName: String, index: Int): String =
        String.format(Locale.ROOT, "%s.cover.Container%02dProvider", packageName, index)

    fun fromClassName(className: String): Int? = parse(providerClassPattern, className)

    private fun parse(pattern: Regex, className: String): Int? {
        val digits = pattern.find(className)?.groupValues?.get(1) ?: return null
        return digits.toInt().takeIf { it in 1..COUNT }
    }
}
