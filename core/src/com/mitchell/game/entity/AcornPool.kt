package com.mitchell.game.entity

import com.badlogic.gdx.utils.Pool
import com.mitchell.game.NuttyGame
import com.mitchell.game.config.GameConfig

class AcornPool(val game: NuttyGame) : Pool<Acorn>(GameConfig.ACORN_COUNT, GameConfig.ACORN_COUNT) {

    override fun newObject(): Acorn {
        return Acorn(game)
    }

}