package com.flooferland.bizlib.bits

data class BotBitmapFile(
    val fixture: MutableMap<MappingName, FixtureName>,
    val settings: Map<String, Any>,
    val bits: Map<MappingName, Map<Short, BitMappingData>>
)
