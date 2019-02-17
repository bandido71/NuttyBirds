package com.mitchell.game

import com.badlogic.gdx.Files
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.physics.box2d.Box2D
import com.mitchell.game.screen.LoadingScreen
import ktx.app.KtxGame
import ktx.assets.getResolver
import ktx.assets.setLoader

class NuttyGame : KtxGame<Screen>() {
    val assetManager = AssetManager()

    override fun create() {

        Box2D.init()
        val resolver = Files.FileType.Internal.getResolver()
        assetManager.setLoader(TmxMapLoader(resolver))
        val loadingScreen = LoadingScreen(this)

        addScreen(loadingScreen)
        setScreen<LoadingScreen>()
    }
}