package com.cesoft.cesardoom

import com.badlogic.gdx.math.Vector3
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight

class CustomDirectionalShadowLight(
    shadowMapWidth: Int,
    shadowMapHeight: Int
): DirectionalShadowLight(shadowMapWidth, shadowMapHeight) {
    val lightIntensity = Vector3()
    override fun updateColor() {
        color.r = baseColor.r * lightIntensity.x
        color.g = baseColor.g * lightIntensity.y
        color.b = baseColor.b * lightIntensity.z
    }
}