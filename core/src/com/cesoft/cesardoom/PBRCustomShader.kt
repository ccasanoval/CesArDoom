package com.cesoft.cesardoom

import com.badlogic.gdx.graphics.g3d.Attributes
import com.badlogic.gdx.graphics.g3d.Renderable
import net.mgsx.gltf.scene3d.shaders.PBRShader


class PBRCustomShader(renderable: Renderable?, config: Config?, prefix: String?) :
    PBRShader(renderable, config, prefix) {
    private var u_sphericalHarmonics = 0
    override fun init() {
        super.init()
        u_sphericalHarmonics = program.fetchUniformLocation("u_sphericalHarmonics", false)
    }

    override fun bindLights(renderable: Renderable, attributes: Attributes) {
        super.bindLights(renderable, attributes)
        val coefficients =
            attributes[SphericalHarmonicsAttribute::class.java, SphericalHarmonicsAttribute.Coefficients]
        if (coefficients != null) {
            val data = coefficients.sphericalHarmonics.data
            program.setUniform3fv(u_sphericalHarmonics, data, 0, data.size / 3)
        }
    }
}
