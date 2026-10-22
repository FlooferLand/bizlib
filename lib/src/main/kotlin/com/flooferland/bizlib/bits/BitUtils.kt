package com.flooferland.bizlib.bits

import kotlinx.serialization.json.Json

typealias FixtureName = String
typealias MappingName = String
typealias Movements = HashMap<String, UShort>
typealias FixtureMap = HashMap<FixtureName, Movements>

object BitUtils {
    /** To convert from/to bottom and top drawer bits, this is added to them */
    const val NEXT_DRAWER: UShort = 150u
    const val DRAWER_MAX: UShort = 300u

    private val bitmapCache = mutableMapOf<String, FixtureMap>()
    private val drawerBit = Regex("""(\d+)(td|bd)""")

    /** Returns null if the bitmap does not exist, or if it could not be parsed */
    fun readBitmap(name: String): FixtureMap? {
        if (bitmapCache.containsKey(name)) return bitmapCache[name]
        val text = this::class.java.classLoader.getResource("bitmaps/$name.json")?.readText() ?: return null
        val json = runCatching { Json.decodeFromString<Map<String, UShort>>(text) }.getOrNull() ?: return null
        val map = FixtureMap()
        for ((key, value) in json) {
            val keySplit = key.split('.', limit = 2)
            if (keySplit.size < 2) continue
            val fixtureName = keySplit[0]
            val movementName = keySplit[1]
            val fixture = map.getOrPut(fixtureName, { Movements() })
            fixture[movementName] = value
        }
        bitmapCache[name] = map
        return map
    }

    fun parseBitId(text: String): UShort? {
        val match = drawerBit.matchEntire(text) ?: return text.toUShortOrNull()
        val num = match.groupValues[1].toUShortOrNull() ?: return null
        return if (match.groupValues[2] == "bd") (num + NEXT_DRAWER).toUShort() else num
    }
}