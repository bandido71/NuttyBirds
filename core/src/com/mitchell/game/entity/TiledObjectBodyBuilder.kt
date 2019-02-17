package com.mitchell.game.entity

import com.badlogic.gdx.maps.objects.EllipseMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World

class TiledObjectBodyBuilder {
    companion object {
        private const val PIXELS_PER_TILE = 32f
        private const val HALF = 0.5f

        @JvmStatic
        fun buildBuildingBodies(tiledMap: TiledMap, world: World) {
            val objects = tiledMap.layers["Physics_Buildings"].objects

            objects.forEach {
                val rectangle = getRectangle(it as RectangleMapObject)
                val bd = BodyDef()
                bd.type = BodyDef.BodyType.DynamicBody
                val body = world.createBody(bd)

                if (it.rectangle.width > it.rectangle.height) {
                    body.userData = "horizontal"
                } else {
                    body.userData = "vertical"
                }

                body.createFixture(rectangle, 1f)
                body.setTransform(getTransformForRectangle(it.rectangle), 0f)
                rectangle.dispose()
            }
        }

        @JvmStatic
        fun buildBirdsBodies(tiledMap: TiledMap, world: World) {
            val objects = tiledMap.layers["Physics_Birds"].objects
            objects.forEach {
                val ellipseMapObject = it as EllipseMapObject
                val circle = getCircle(ellipseMapObject)
                val bd = BodyDef()
                bd.type = BodyDef.BodyType.DynamicBody
                val body = world.createBody(bd)
                val fixture = body.createFixture(circle, 1f)
                fixture.userData = "enemy"
                body.userData = "enemy"

                val ellipse = ellipseMapObject.ellipse
                body.setTransform(
                        Vector2(
                                (ellipse.x + ellipse.width * HALF) / PIXELS_PER_TILE,
                                (ellipse.y + ellipse.height * HALF) / PIXELS_PER_TILE
                        ),
                        0f
                )

                circle.dispose()
            }
        }

        @JvmStatic
        fun buildFloorBodies(tiledMap: TiledMap, world: World) {
            val objects = tiledMap.layers["Physics_Floor"].objects
            objects.forEach {
                val rectangle = getRectangle(it as RectangleMapObject)
                val bd = BodyDef()
                bd.type = BodyDef.BodyType.StaticBody
                val body = world.createBody(bd)
                body.userData = "floor"
                body.createFixture(rectangle, 1f)
                body.setTransform(getTransformForRectangle(it.rectangle), 0f)
                rectangle.dispose()
            }
        }

        @JvmStatic
        private fun getCircle(ellipseMapObject: EllipseMapObject): CircleShape {
            val ellipse = ellipseMapObject.ellipse
            val circleShape = CircleShape()
            circleShape.radius = ellipse.width * HALF / PIXELS_PER_TILE

            return circleShape
        }

        @JvmStatic
        private fun getRectangle(rectangleMapObject: RectangleMapObject): PolygonShape {
            val rectangle = rectangleMapObject.rectangle
            val polygon = PolygonShape()

            polygon.setAsBox(
                    rectangle.width * HALF / PIXELS_PER_TILE,
                    rectangle.height * HALF / PIXELS_PER_TILE
            )

            return polygon
        }

        @JvmStatic
        private fun getTransformForRectangle(rectangle: Rectangle): Vector2 {
            return Vector2(
                    (rectangle.x + (rectangle.width * HALF)) / PIXELS_PER_TILE,
                    (rectangle.y + (rectangle.height * HALF)) / PIXELS_PER_TILE
            )
        }

    }
}