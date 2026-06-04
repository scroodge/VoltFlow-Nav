package com.bridge.yandexbyd

/**
 * Russian Cyrillic → Latin transliteration.
 *
 * The BYD DiLink 3.0 instrument cluster font has no Cyrillic glyphs (Latin +
 * Chinese only), so Cyrillic road names arrive but draw blank. We transliterate
 * to Latin before sending so names like "Сурганова" show as "Surganova".
 *
 * Latin/digits/punctuation pass through unchanged, so mixed strings are fine.
 */
object Translit {

    private val MAP: Map<Char, String> = buildMap {
        val pairs = listOf(
            'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d",
            'е' to "e", 'ё' to "e", 'ж' to "zh", 'з' to "z", 'и' to "i",
            'й' to "y", 'к' to "k", 'л' to "l", 'м' to "m", 'н' to "n",
            'о' to "o", 'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t",
            'у' to "u", 'ф' to "f", 'х' to "kh", 'ц' to "ts", 'ч' to "ch",
            'ш' to "sh", 'щ' to "shch", 'ъ' to "", 'ы' to "y", 'ь' to "",
            'э' to "e", 'ю' to "yu", 'я' to "ya",
        )
        for ((cyr, lat) in pairs) {
            put(cyr, lat)
            // Uppercase form: capitalise the Latin (e.g. Ш -> "Sh").
            put(cyr.uppercaseChar(), lat.replaceFirstChar { it.uppercaseChar() })
        }
    }

    /** True if the string contains any Cyrillic character. */
    fun hasCyrillic(s: String): Boolean = s.any { it in 'Ѐ'..'ӿ' }

    fun transliterate(s: String): String {
        if (!hasCyrillic(s)) return s
        val sb = StringBuilder(s.length + 4)
        for (ch in s) sb.append(MAP[ch] ?: ch.toString())
        return sb.toString()
    }
}
