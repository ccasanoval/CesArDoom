package com.cesoft.cesardoom

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g3d.Renderable
import com.badlogic.gdx.graphics.g3d.Shader
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider
import net.mgsx.gltf.scene3d.shaders.PBRShader
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider

class PBRShadowCatcherShaderProvider(config: PBRShaderConfig?) : PBRShaderProvider(config) {
    private val shadowCatcherShaderProvider =
        DefaultShaderProvider(null, Gdx.files.internal("shadowCatcher.glsl").readString())

    override fun createShader(
        renderable: Renderable,
        config: PBRShaderConfig,
        prefix: String
    ): PBRShader {
        var prefix2: String? = prefix
        if (renderable.environment.has(SphericalHarmonicsAttribute.Coefficients)) {
            prefix2 += "#define sphericalHarmonicsFlag\n"
        }
        return PBRCustomShader(renderable, config, prefix2)
    }

    override fun getShader(renderable: Renderable): Shader {
        if (renderable.userData is ShaderType) {
            return when (renderable.userData as ShaderType) {
                ShaderType.PBR -> super.getShader(renderable)
                ShaderType.SHADOW_CATCHER -> shadowCatcherShaderProvider.getShader(renderable)
            }
        }
        return super.getShader(renderable)
    }

    enum class ShaderType {
        SHADOW_CATCHER, PBR
    }
}
