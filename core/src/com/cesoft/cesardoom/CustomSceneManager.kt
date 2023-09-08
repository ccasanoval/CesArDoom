package com.cesoft.cesardoom

import com.badlogic.gdx.graphics.g3d.environment.SphericalHarmonics
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider
import net.mgsx.gltf.scene3d.scene.SceneManager


class CustomSceneManager : SceneManager {
    private val sphericalHarmonicsCoefficients = FloatArray(9 * 3)

//    constructor()
//    constructor(maxBones: Int) : super(maxBones)
    constructor(shaderProvider: ShaderProvider?, depthShaderProvider: DepthShaderProvider?) : super(
        shaderProvider,
        depthShaderProvider
    )

//    constructor(
//        shaderProvider: ShaderProvider?,
//        depthShaderProvider: DepthShaderProvider?,
//        renderableSorter: RenderableSorter?
//    ) : super(shaderProvider, depthShaderProvider, renderableSorter)

    fun setAmbientLight(sphericalHarmonics: SphericalHarmonics) {
        val attribute: SphericalHarmonicsAttribute? =
            environment[SphericalHarmonicsAttribute::class.java, SphericalHarmonicsAttribute.Coefficients]
        if (attribute != null) {
            val coefficients = sphericalHarmonics.data
            for (i in 0 until 9 * 3) {
                sphericalHarmonicsCoefficients[i] =
                    coefficients[i] * sphericalHarmonicFactors[i / 3]
            }
            attribute.sphericalHarmonics.set(sphericalHarmonicsCoefficients)
        }
    }

    companion object {
        val sphericalHarmonicFactors = floatArrayOf(
            0.282095f,
            -0.325735f,
            0.325735f,
            -0.325735f,
            0.273137f,
            -0.273137f,
            0.078848f,
            -0.273137f,
            0.136569f
        )
    }
}
