package com.cesoft.cesardoom

import com.badlogic.gdx.graphics.g3d.Attribute
import com.badlogic.gdx.graphics.g3d.environment.SphericalHarmonics
import java.util.Arrays


class SphericalHarmonicsAttribute : Attribute {
    val sphericalHarmonics = SphericalHarmonics()

    constructor(type: Long) : super(type)
    constructor(type: Long, harmonics: SphericalHarmonics) : super(type) {
        sphericalHarmonics.set(harmonics.data)
    }

    constructor(copyFrom: SphericalHarmonicsAttribute) : this(
        copyFrom.type,
        copyFrom.sphericalHarmonics
    )

    override fun copy(): Attribute {
        return SphericalHarmonicsAttribute(this)
    }

    override fun compareTo(o: Attribute): Int {
        return if (type != o.type) if (type < o.type) -1 else 1 else Arrays.hashCode((o as SphericalHarmonicsAttribute).sphericalHarmonics.data) - Arrays.hashCode(
            sphericalHarmonics.data
        )
    }

    companion object {
        const val SphericalHarmonicsAlias = "ambientSphericalHarmonicsCoefficients"
        val Coefficients = register(SphericalHarmonicsAlias)
        fun createCoefficients(harmonics: SphericalHarmonics): SphericalHarmonicsAttribute {
            return SphericalHarmonicsAttribute(Coefficients, harmonics)
        }
    }
}
