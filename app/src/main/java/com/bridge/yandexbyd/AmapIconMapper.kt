package com.bridge.yandexbyd

/**
 * Maps Yandex Navigator maneuver wording to AMap NEW_ICON ids that the BYD
 * cluster relay (com.example.amapservice) understands. ids are the index into
 * AmapService.TURN_STRING; the relay maps them to CAN codes itself.
 *
 * See CLUSTER_PROTOCOL.md for the full table. Russia is right-hand traffic.
 */
object AmapIconMapper {

    const val LEFT = 2
    const val RIGHT = 3
    const val SLIGHT_LEFT = 4
    const val SLIGHT_RIGHT = 5
    const val SHARP_LEFT = 6
    const val SHARP_RIGHT = 7
    const val UTURN = 8
    const val STRAIGHT = 9
    const val WAYPOINT = 10
    const val ROUNDABOUT = 11
    const val TOLL = 14
    const val DEST = 15
    const val CONTINUE = 20

    // Order matters: most specific phrases first.
    private val RULES: List<Pair<List<String>, Int>> = listOf(
        listOf("разворот", "u-turn", "u turn", "make a u")                       to UTURN,
        listOf("резко налево", "sharp left")                                     to SHARP_LEFT,
        listOf("резко направо", "sharp right")                                   to SHARP_RIGHT,
        listOf("держитесь левее", "левее", "слегка налево", "keep left", "slight left")   to SLIGHT_LEFT,
        listOf("держитесь правее", "правее", "слегка направо", "keep right", "slight right") to SLIGHT_RIGHT,
        listOf("кольц", "круговое", "круг", "roundabout", "съезд")               to ROUNDABOUT,
        listOf("налево", "поверните налево", "turn left", "turn to the left")    to LEFT,
        listOf("направо", "поверните направо", "turn right", "turn to the right") to RIGHT,
        listOf("прибыли", "пункт назначения", "финиш", "arrived", "destination", "have reached") to DEST,
        listOf("платн", "toll")                                                  to TOLL,
        listOf("по маршруту", "продолжайте", "continue", "head ", "go straight") to CONTINUE,
        listOf("прямо", "straight")                                              to STRAIGHT,
    )

    /** Returns a NEW_ICON id, or null if no keyword matched. */
    fun fromText(text: String): Int? {
        val lower = text.lowercase()
        for ((keywords, icon) in RULES) {
            if (keywords.any { lower.contains(it) }) return icon
        }
        return null
    }
}
