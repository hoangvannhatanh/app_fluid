package com.lusa.fluidwallpaper.model

data class Preset(
    val name: String,
    val effectType: Int,
    val speed: Float,
    val viscosity: Float,
    val turbulence: Float,
    val color1: FloatArray,
    val color2: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Preset

        if (name != other.name) return false
        if (effectType != other.effectType) return false
        if (speed != other.speed) return false
        if (viscosity != other.viscosity) return false
        if (turbulence != other.turbulence) return false
        if (!color1.contentEquals(other.color1)) return false
        if (!color2.contentEquals(other.color2)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + effectType
        result = 31 * result + speed.hashCode()
        result = 31 * result + viscosity.hashCode()
        result = 31 * result + turbulence.hashCode()
        result = 31 * result + color1.contentHashCode()
        result = 31 * result + color2.contentHashCode()
        return result
    }
}
