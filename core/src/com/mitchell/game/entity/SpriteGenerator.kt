package com.mitchell.game.entity

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.physics.box2d.Body
import com.mitchell.game.NuttyGame

class SpriteGenerator {

    companion object {
        @JvmStatic
        fun generateSpriteForBody(assetManager: AssetManager, body: Body): Sprite? {
            if ("horizontal" == body.userData) {
                return createSprite(assetManager, "obstacleHorizontal.png")
            }
            if ("vertical" == body.userData) {
                return createSprite(assetManager, "obstacleVertical.png")
            }
            if ("enemy" == body.userData) {
                return createSprite(assetManager, "bird.png")
            }

            return null
        }

        @JvmStatic
        private fun createSprite(assetManager: AssetManager, textureName: String): Sprite {
            val sprite = Sprite(assetManager.get<Texture>(textureName))
            sprite.setOrigin(sprite.width / 2, sprite.height / 2)

            return sprite
        }
    }
}