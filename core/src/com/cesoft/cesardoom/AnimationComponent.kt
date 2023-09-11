package com.cesoft.cesardoom

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.utils.AnimationController

data class AnimationParams(
    var id: String,
    var loop: Int = 1,
    var speed: Float = 1f,
    var offset: Float = 0f,
    var duration: Float = -1f,
    var transitionTime: Float = 0.5f,
    var onEnd: () -> Unit,
)

class AnimationComponent(instance: ModelInstance) : Component {

    private val animationController: AnimationController = AnimationController(instance)

    init {
        animationController.allowSameAnimation = true
    }

    fun animate(params: AnimationParams) {
        animationController.animate(
            params.id,
            params.offset,
            params.duration,
            params.loop,
            params.speed,
            object: AnimationController.AnimationListener {
                override fun onEnd(animation: AnimationController.AnimationDesc?) {
                    params.onEnd()
                }
                override fun onLoop(animation: AnimationController.AnimationDesc?) {}
            },
            params.transitionTime,
        )
    }

    fun update(delta: Float) {
        animationController.update(delta)
    }


    companion object {
        private val mapper: ComponentMapper<AnimationComponent> =
            ComponentMapper.getFor(AnimationComponent::class.java)
        fun get(entity: Entity):AnimationComponent = mapper.get(entity)
    }
}