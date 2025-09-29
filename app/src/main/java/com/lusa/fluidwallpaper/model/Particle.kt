package com.lusa.fluidwallpaper.model

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Int,
    var size: Float? = 0f,
    var life: Float? = 0f
)