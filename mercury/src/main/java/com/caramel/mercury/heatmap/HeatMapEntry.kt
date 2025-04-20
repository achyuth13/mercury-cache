package com.caramel.mercury.heatmap

/**
 * Heat map entry
 *
 * @property value
 * @property type
 * @property score
 * @constructor Create empty Heat map entry
 */
data class HeatMapEntry(
    val value: String,
    val type: String,
    val score: Int
)