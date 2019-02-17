package com.mitchell.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.viewport.FitViewport
import com.mitchell.game.NuttyGame
import com.mitchell.game.config.GameConfig
import com.mitchell.game.entity.SpriteGenerator
import com.mitchell.game.entity.TiledObjectBodyBuilder
import ktx.app.KtxScreen
import ktx.collections.GdxArray
import ktx.graphics.use
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt



class GameScreen(game: NuttyGame) : KtxScreen {

    private var shapeRenderer = ShapeRenderer()
    private var camera = OrthographicCamera()
    private var box2dCam = OrthographicCamera(GameConfig.UNIT_WIDTH, GameConfig.UNIT_HEIGHT)
    private var viewport = FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera)
    private var batch = SpriteBatch()
    private val assetManager = game.assetManager

    private val tiledMap = assetManager.get<TiledMap>("nuttybirds.tmx")
    private val orthogonalTiledMapRenderer = OrthogonalTiledMapRenderer(tiledMap, batch)
    private val world: World = World(Vector2(0f, -10f), true)
    private val debugRenderer = Box2DDebugRenderer()

    private val toRemove = GdxArray<Body>()
    private val anchor = Vector2(convertMetresToUnits(6.125f), convertMetresToUnits(5.75f))
    private val firingPosition = anchor.cpy()
    private var distance = 0f
    private var angle = 0f

    private val sprites = ObjectMap<Body, Sprite>()
    private val slingshot: Sprite
    private val squirrel: Sprite
    private val staticAcorn: Sprite

    init {
        viewport.apply(true)
        orthogonalTiledMapRenderer.setView(camera)
        TiledObjectBodyBuilder.buildBuildingBodies(tiledMap, world)
        TiledObjectBodyBuilder.buildFloorBodies(tiledMap, world)
        TiledObjectBodyBuilder.buildBirdsBodies(tiledMap, world)

        world.setContactListener(NuttyContactListener())

        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                calculateAngleAndDistanceForBullet(screenX.toFloat(), screenY.toFloat())
                return true
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                createBullet()
                firingPosition.set(anchor.cpy())
                return true
            }
        }

        val bodies = GdxArray<Body>()
        world.getBodies(bodies)
        bodies.forEach {
            val sprite = SpriteGenerator.generateSpriteForBody(assetManager, it)
            if (sprite != null) sprites.put(it, sprite)
        }

        slingshot = Sprite(assetManager.get<Texture>("slingshot.png"))
        slingshot.setPosition(170f, 64f)
        squirrel = Sprite(assetManager.get<Texture>("squirrel.png"))
        squirrel.setPosition(32f, 64f)
        staticAcorn = Sprite(assetManager.get<Texture>("staticAcorn.png"))
    }

    override fun render(delta: Float) {
        update(delta)
        draw()
//        drawDebug()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun dispose() {
        batch.dispose()
        shapeRenderer.dispose()
    }

    private fun draw() {
        viewport.apply()
        batch.projectionMatrix = camera.combined
        orthogonalTiledMapRenderer.render()
        batch.use {
            sprites.values().forEach {
                it.draw(batch)
            }

            squirrel.draw(batch)
            staticAcorn.draw(batch)
            slingshot.draw(batch)
        }
    }

    private fun update(delta: Float) {
        clearDeadBodies()
        world.step(delta, 6, 2)
        box2dCam.position.set(GameConfig.UNIT_WIDTH / 2f, GameConfig.UNIT_HEIGHT / 2f, 0f)
        box2dCam.update()
        updateSpritePositions()
    }

    private fun updateSpritePositions() {
        sprites.keys().forEach {
            val sprite = sprites[it]
            sprite.setPosition(
                    convertMetresToUnits(it.position.x) - sprite.width / 2f,
                    convertMetresToUnits(it.position.y) - sprite.height / 2f
            )
            sprite.rotation = MathUtils.radiansToDegrees * it.angle
        }

        staticAcorn.setPosition(
                firingPosition.x - staticAcorn.width / 2f,
                firingPosition.y - staticAcorn.height / 2f
        )
    }

    private fun drawDebug() {
        viewport.apply()
        debugRenderer.render(world, box2dCam.combined)
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.rect(anchor.x - 5f, anchor.y - 5f, 10f, 10f)
        shapeRenderer.rect(firingPosition.x - 5f, firingPosition.y - 5f, 10f, 10f)
        shapeRenderer.line(anchor.x, anchor.y, firingPosition.x, firingPosition.y)
        shapeRenderer.end()
    }

    private fun createBullet() {
        val circleShape = CircleShape()
        circleShape.radius = 0.5f
        val bd = BodyDef()
        bd.type = BodyDef.BodyType.DynamicBody
        val bullet = world.createBody(bd)
        bullet.userData = "acorn"
        bullet.createFixture(circleShape, 1f)
        bullet.setTransform(
                Vector2(convertUnitsToMetres(firingPosition.x), convertUnitsToMetres(firingPosition.y)),
                0f
        )

        val sprite = Sprite(assetManager.get<Texture>("acorn.png"))
        sprite.setOrigin(sprite.width / 2f, sprite.height / 2f)
        sprites.put(bullet, sprite)

        circleShape.dispose()
        val velX = abs((GameConfig.MAX_STRENGTH * -cos(angle) * (distance / 100f)))
        val velY = abs((GameConfig.MAX_STRENGTH * -sin(angle) * (distance / 100f)))
        bullet.setLinearVelocity(velX, velY)
    }

    private fun clearDeadBodies() {
        toRemove.forEach {
            sprites.remove(it)
            world.destroyBody(it)
        }

        toRemove.clear()
    }

    private fun convertUnitsToMetres(pixels: Float): Float {
        return pixels / GameConfig.UNITS_PER_METER
    }

    private fun convertMetresToUnits(metres: Float): Float {
        return metres * GameConfig.UNITS_PER_METER
    }

    private fun angleBetweenToPoints(): Float {
        var angle = MathUtils.atan2(anchor.y - firingPosition.y, anchor.x - firingPosition.x)
        angle %= 2 * MathUtils.PI
        if (angle < 0) angle += MathUtils.PI2
        return angle
    }

    private fun distanceBetweenToPoints(): Float {
        val deltaX = anchor.x - firingPosition.x
        val deltaY = anchor.y - firingPosition.y

        return sqrt(deltaX * deltaX + deltaY * deltaY)
    }

    private fun calculateAngleAndDistanceForBullet(screenX: Float, screenY: Float) {
        firingPosition.set(screenX, screenY)
        viewport.unproject(firingPosition)
        distance = distanceBetweenToPoints()
        angle = angleBetweenToPoints()
        if (distance > GameConfig.MAX_DISTANCE) {
            distance = GameConfig.MAX_DISTANCE
        }
        if (angle > GameConfig.LOWER_ANGLE) {
            if (angle > GameConfig.UPPER_ANGLE) {
                angle = 0f
            } else {
                angle = GameConfig.LOWER_ANGLE
            }
        }

        firingPosition.set(
                anchor.x + (distance * -MathUtils.cos(angle)),
                anchor.y + (distance * -MathUtils.sin(angle))
        )
    }

    inner class NuttyContactListener : ContactListener {
        override fun beginContact(contact: Contact?) {
            val attacker = contact?.fixtureA
            val defender = contact?.fixtureB

            val worldManifold = contact!!.worldManifold
            if ("enemy" == defender?.userData) {
                val vel1 = attacker?.body?.getLinearVelocityFromWorldPoint(worldManifold.points[0])
                val vel2 = defender.body?.getLinearVelocityFromWorldPoint(worldManifold.points[0])
                val impactVelocity = vel1?.sub(vel2)

                if (Math.abs(impactVelocity!!.x) > 1 || Math.abs(impactVelocity.y) > 1) {
                    toRemove.add(defender.body)
                }
            }

        }

        override fun endContact(contact: Contact?) {
        }


        override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
        }

        override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
        }

    }

}