package com.cesoft.cesardoom

import com.badlogic.gdx.math.Vector3
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight


class CustomDirectionalShadowLight: DirectionalShadowLight {
    constructor(shadowMapWidth: Int, shadowMapHeight: Int) : super(shadowMapWidth, shadowMapHeight)
    constructor(
        shadowMapWidth: Int,
        shadowMapHeight: Int,
        shadowViewportWidth: Float,
        shadowViewportHeight: Float,
        shadowNear: Float,
        shadowFar: Float
    ) : super(
        shadowMapWidth,
        shadowMapHeight,
        shadowViewportWidth,
        shadowViewportHeight,
        shadowNear,
        shadowFar
    )

    val lightIntensity = Vector3()
    override fun updateColor() {
        color.r = baseColor.r * lightIntensity.x
        color.g = baseColor.g * lightIntensity.y
        color.b = baseColor.b * lightIntensity.z
    }
}