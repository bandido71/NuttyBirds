package com.mitchell.game.config

import com.badlogic.gdx.math.MathUtils

class GameConfig {
    companion object {
        const val WORLD_WIDTH = 960f
        const val WORLD_HEIGHT = 544f
        //const val UNITS_PER_METER = 16f
        const val UNITS_PER_METER = 32f
        const val UNIT_WIDTH = WORLD_WIDTH / UNITS_PER_METER
        const val UNIT_HEIGHT = WORLD_HEIGHT / UNITS_PER_METER
        const val MAX_STRENGTH = 15f
        const val MAX_DISTANCE = 100f
        const val UPPER_ANGLE = 3 * MathUtils.PI / 2f
        const val LOWER_ANGLE = MathUtils.PI / 2f
        const val ACORN_COUNT = 3
    }
}