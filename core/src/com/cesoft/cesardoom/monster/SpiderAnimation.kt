package com.cesoft.cesardoom.monster

import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.utils.AnimationController
import com.cesoft.cesardoom.AnimationComponent
import com.cesoft.cesardoom.AnimationParams

class SpiderAnimation(
    modelInstance: ModelInstance,
) {
    private val animation: AnimationComponent = AnimationComponent(modelInstance)

    fun update(deltaTime: Float) {
        animation.update(deltaTime)
    }

    fun animate(type: SpiderState, loop: Boolean = false, onEnd: () -> Unit = {}) {
        val time = when(type) {
            SpiderState.Attack -> attack
            SpiderState.Head -> head
            SpiderState.Idle -> idle
            SpiderState.Jump -> jump
            SpiderState.JumpRoot -> jumpRoot
            SpiderState.Scream -> scream
            SpiderState.Walk -> walk
        }
        animation.animate(
            AnimationParams(
                name,
                if(loop) -1 else +1,
                speed,
                time.first,
                time.second - time.first,
                0.5f,
                onEnd,
            ),
        )
    }

    companion object {
        private const val name = "basic"
        private const val speed = 1f

        // IDLE(10-110)
        // WALK(120-160)
        // SCREAM(170-270)
        // JUMP WITH ROOT(280-330)
        // JUMP(340-390)
        // HEAD(400-415)
        // ATACK SECTION(420-500)
        val idle = Pair(10f / 60f, 110f / 60f)
        val walk = Pair(120f / 60f, 160f / 60f)
        val scream = Pair(170 / 60f, 270 / 60f)
        val jumpRoot = Pair(280 / 60f, 330 / 60f)
        val jump = Pair(340 / 60f, 390 / 60f)
        val head = Pair(400 / 60f, 415 / 60f)
        val attack = Pair(442f / 60f, 500f / 60f)//TODO 420...
    }
}