package com.mitchell.game.entity

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Pool
import com.mitchell.game.NuttyGame

class Acorn(game: NuttyGame) : Pool.Poolable{
    private val assetManager = game.assetManager
    private val sprite = Sprite(assetManager.get<Texture>("acorn.png"))
    val width = sprite.width
    val height = sprite.height

    init {
        sprite.setOrigin(sprite.width / 2f, sprite.height / 2f)
    }

    fun setPosition(x: Float, y: Float) {
        sprite.setPosition(x, y)
    }

    fun setRotation(degrees: Float) {
        sprite.rotation = degrees
    }

    fun draw(batch: SpriteBatch) {
        sprite.draw(batch)
    }

    override fun reset() {
        sprite.setPosition(0f, 0f)
        sprite.rotation = 0f
    }

}