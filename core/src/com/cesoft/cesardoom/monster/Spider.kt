package com.cesoft.cesardoom.monster

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.graphics.g3d.utils.BaseAnimationController
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import com.cesoft.cesardoom.Log
import com.cesoft.cesardoom.PBRShadowCatcherShaderProvider
import games.rednblack.gdxar.GdxAnchor
import games.rednblack.gdxar.GdxPose
import net.mgsx.gltf.loaders.glb.GLBLoader
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneAsset
import net.mgsx.gltf.scene3d.scene.SceneManager
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class Spider(
    private val sceneManager: SceneManager,
    gdxAnchor: GdxAnchor,
) {
    private val builder = ModelBuilder()
    private lateinit var animation: SpiderAnimation
    private lateinit var modelScene: Scene
    private lateinit var shadowScene: Scene

    private val transform = BaseAnimationController.Transform()
    private val targetDir = Quaternion()
    private val targetPos = Vector3()
    private val targetScale = Vector3(1f, 1f, 1f)

    private var state: SpiderState = SpiderState.Idle

    init {
        val pose = gdxAnchor.gdxPose
        createMonster(pose.position, pose.rotation)
    }

    private fun setState(state: SpiderState, afterState: SpiderState?=null) {
        this.state = state
        animation.animate(state) {
            afterState?.let {
                this.state = afterState
                animation.animate(afterState, loop = true)
            }
        }
    }

    fun relocate(pose: GdxPose) {
        //Log.e("Spider", "relocate0----------$targetPos / $targetDir")
        targetDir.set(pose.rotation)
        targetPos.set(pose.position)
        //Log.e("Spider", "relocate9----------$targetPos / $targetDir")
        //TODO: update walking direction...
    }

    private fun createMonster(position: Vector3, rotation: Quaternion) {
        modelScene = Scene(modelAsset.scene)
        sceneManager.addScene(modelScene)

        modelScene.modelInstance.transform.translate(position)
        modelScene.modelInstance.transform.set(rotation)
        targetPos.set(position)
        targetDir.set(rotation)

        transform[targetPos, targetDir] = targetScale
        //modelInstances.put(anchorId, modelScene.modelInstance)

        val modelInstance = modelScene.modelInstance
        animation = SpiderAnimation(modelInstance)
        setState(SpiderState.Scream, SpiderState.Walk)

        createShadowFloor()
    }

    //Create a plane model for the virtual ground floor, need to show shadows
    private fun createShadowFloor() {
        builder.begin()
        val groundMaterial = Material(BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA))
        val meshPartBuilder = builder.part(
            "ground",
            GL20.GL_TRIANGLES,
            (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong(),
            groundMaterial
        )
        val size = 3f
        meshPartBuilder.rect(
            -size / 2f, 0f, -size / 2f,
            -size / 2f, 0f, size / 2f,
            size / 2f, 0f, size / 2f,
            size / 2f, 0f, -size / 2f,
            0f, 0f, 0f
        )
        val ground = ModelInstance(builder.end())
        ground.userData = PBRShadowCatcherShaderProvider.ShaderType.SHADOW_CATCHER
        shadowScene = Scene(ground)
        sceneManager.addScene(shadowScene)
    }

    fun update(deltaTime: Float) {//Gdx.graphics.deltaTime

        if(state == SpiderState.Walk) {
            targetPos.z += cos(targetDir.yawRad) * deltaTime * speedFactor
            targetPos.x += sin(targetDir.yawRad) * deltaTime * speedFactor
        }

        transform.lerp(targetPos, targetDir, targetScale, 0.1f)
        modelScene.let { modelScene ->
            //modelScene.modelInstance.transform[transform.translation] = transform.rotation
            modelScene.modelInstance.transform?.set(transform.translation, transform.rotation)
            modelScene.modelInstance.transform.scale(scale, scale, scale)
        }
        shadowScene.modelInstance?.transform?.set(transform.translation, transform.rotation)

        animation.update(Gdx.graphics.deltaTime)
    }

    companion object {
        fun init() {}
        private val modelAsset: SceneAsset = GLBLoader().load(Gdx.files.internal("spider.glb"))
        private const val speedFactor = 1/30f
        private const val scale = 0.015f
    }
}