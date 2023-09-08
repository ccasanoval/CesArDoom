package com.cesoft.cesardoom

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.graphics.g3d.utils.BaseAnimationController
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.LongMap
import com.badlogic.gdx.utils.Pools
import games.rednblack.gdxar.GdxAnchor
import games.rednblack.gdxar.GdxArApplicationListener
import games.rednblack.gdxar.GdxFrame
import games.rednblack.gdxar.GdxLightEstimationMode
import games.rednblack.gdxar.GdxPlaneType
import games.rednblack.gdxar.GdxPose
import net.mgsx.gltf.loaders.glb.GLBLoader
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneAsset
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider
import net.mgsx.gltf.scene3d.utils.IBLBuilder
import net.mgsx.gltf.scene3d.utils.ShaderParser

class ArPlayground: GdxArApplicationListener() {
    private lateinit var sceneManager: CustomSceneManager
    private lateinit var directionalLight: CustomDirectionalShadowLight
    private var modelScene: Scene? = null
//    private var groundFloor: Scene? = null
    private var modelAsset: SceneAsset? = null

    private val targetDir = Quaternion()
    private val targetPos = Vector3()
    private val targetScale = Vector3(1f, 1f, 1f)

    private val builder = ModelBuilder()
    private val transform = BaseAnimationController.Transform()
    private val modelInstances = LongMap<ModelInstance>()

    override fun create() {
        //Setup some configs
        arAPI.setPowerSaveMode(false)
        arAPI.setAutofocus(true)
        arAPI.enableSurfaceGeometry(true)

        //Setup glTF rendering environment
        val config = PBRShaderProvider.createDefaultConfig()
        config.numBones = 40
        config.manualSRGB = PBRShaderConfig.SRGB.NONE
        config.fragmentShader = ShaderParser.parse(Gdx.files.internal("pbr/pbr.fs.glsl"))
        config.vertexShader = ShaderParser.parse(Gdx.files.internal("pbr/pbr.vs.glsl"))
        val depthConfig = PBRShaderProvider.createDefaultDepthConfig()
        depthConfig.numBones = 40
        sceneManager = CustomSceneManager(
            PBRShadowCatcherShaderProvider(config),
            PBRDepthShaderProvider(depthConfig)
        )
        directionalLight = CustomDirectionalShadowLight(2048, 2048)
        directionalLight.direction.set(1f, -3f, 1f).nor()
        directionalLight.color.set(Color.WHITE)
        sceneManager.environment.add(directionalLight)
        val iblBuilder = IBLBuilder.createOutdoor(directionalLight)
//        val diffuseCubemap = EnvironmentUtil.createCubemap(
//            InternalFileHandleResolver(),
//            "diffuse/diffuse_", "_0.jpg", EnvironmentUtil.FACE_NAMES_NEG_POS
//        )
        val diffuseCubemap = iblBuilder.buildIrradianceMap(256);
//        val specularCubemap = EnvironmentUtil.createCubemap(
//            InternalFileHandleResolver(),
//            "specular/specular_", "_", ".jpg", 10, EnvironmentUtil.FACE_NAMES_NEG_POS
//        )
        val specularCubemap = iblBuilder.buildRadianceMap(10);
        iblBuilder.dispose()
        val brdfLUT = Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"))
        sceneManager.setAmbientLight(1f)
        sceneManager.environment.set(
            PBRTextureAttribute(
                PBRTextureAttribute.BRDFLUTTexture,
                brdfLUT
            )
        )
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap))
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap))
        if (arAPI.lightEstimationMode == GdxLightEstimationMode.ENVIRONMENTAL_HDR) sceneManager.environment.set(
            SphericalHarmonicsAttribute(SphericalHarmonicsAttribute.Coefficients)
        )

        //Create a plane model for the virtual ground floor, need to show shadows
        builder.begin()
//        val groundMaterial = Material(BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA))
//        val meshPartBuilder = builder.part(
//            "ground",
//            GL20.GL_TRIANGLES,
//            (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong(),
//            groundMaterial
//        )
//        val size = 3f
//        meshPartBuilder.rect(
//            -size / 2f,
//            0f,
//            -size / 2f,
//            -size / 2f,
//            0f,
//            size / 2f,
//            size / 2f,
//            0f,
//            size / 2f,
//            size / 2f,
//            0f,
//            -size / 2f,
//            0f,
//            0f,
//            0f
//        )
//        val ground = ModelInstance(builder.end())
//        ground.userData = PBRShadowCatcherShaderProvider.ShaderType.SHADOW_CATCHER
//        groundFloor = Scene(ground)

        //Load AR Model
        //modelAsset = GLBLoader().load(Gdx.files.internal("BrainStem.glb"))
        modelAsset = GLBLoader().load(Gdx.files.internal("spider.glb"))

        //Setup glTF -> AR Bind
        sceneManager.setCamera(arAPI.arCamera)
        directionalLight.setViewport(
            6f,
            6f,
            sceneManager.camera.near,
            sceneManager.camera.far)

        //Start AR!
        arAPI.setRenderAR(true)
    }

    override fun renderARModels(frame: GdxFrame) {
        //Update environment light based on AR frame
        when (frame.lightEstimationMode) {
            GdxLightEstimationMode.ENVIRONMENTAL_HDR -> {
                sceneManager.setAmbientLight(frame.sphericalHarmonics)
                directionalLight.direction.set(
                    frame.lightDirection.x,
                    frame.lightDirection.y,
                    -frame.lightDirection.z
                )
                directionalLight.lightIntensity.set(frame.lightIntensity)
            }
            GdxLightEstimationMode.AMBIENT_INTENSITY -> {
                sceneManager.setAmbientLight(frame.ambientIntensity)
                directionalLight.intensity = 1f
                directionalLight.baseColor.set(frame.lightColor)
            }
            else -> {
                sceneManager.setAmbientLight(1f)
                directionalLight.baseColor.set(Color.WHITE)
            }
        }

        //Force update models based on the tracking anchor calculated by the framework
        for(anchor in frame.anchors) {
            val model = modelInstances[anchor.id]
            model?.transform?.set(anchor.gdxPose.position, anchor.gdxPose.rotation)
        }
        transform.lerp(targetPos, targetDir, targetScale, 0.1f)
        modelScene?.let { modelScene ->
            modelScene.modelInstance.transform[transform.translation] = transform.rotation
            val scale = 0.015f
            modelScene.modelInstance.transform.scale(scale, scale, scale)
        }
//        groundFloor?.modelInstance?.transform?.set(transform.translation, transform.rotation)

        sceneManager.update(Gdx.graphics.deltaTime)
        sceneManager.render()
    }

    override fun render() {
        if (Gdx.input.isTouched) {
            handleTouch(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        }
    }

    private fun handleTouch(x: Float, y: Float) {
        if (modelScene == null) {
            val newAnchor: GdxAnchor? = arAPI.requestHitPlaneAnchor(x, y, GdxPlaneType.ANY)
            if (newAnchor != null) {
                modelScene = Scene(modelAsset!!.scene)

                //TODO:
                modelScene!!.animations.playAll()

                sceneManager.addScene(modelScene)
                val p = newAnchor.gdxPose
                modelScene!!.modelInstance.transform.translate(p.position)
                modelScene!!.modelInstance.transform.set(p.rotation)
                targetDir.set(p.rotation)
                targetPos.set(p.position)
                transform[targetPos, targetDir] = targetScale
                modelInstances.put(newAnchor.id, modelScene!!.modelInstance)
                Pools.free(newAnchor)
//                sceneManager.addScene(groundFloor)
            }
        } else {
            val p: GdxPose? = arAPI.requestHitPlanePose(x, y, GdxPlaneType.ANY)
            if (p != null) {
                targetDir.set(p.rotation)
                targetPos.set(p.position)
                modelScene!!.animations.playAll()
                //modelScene!!.animations.
                Pools.free(p)
            }
        }
    }
}
