package com.cesoft.cesardoom

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Pools
import com.cesoft.cesardoom.monster.Spider
import games.rednblack.gdxar.GdxAnchor
import games.rednblack.gdxar.GdxArApplicationListener
import games.rednblack.gdxar.GdxFrame
import games.rednblack.gdxar.GdxLightEstimationMode
import games.rednblack.gdxar.GdxPlaneType
import games.rednblack.gdxar.GdxPose
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider
import net.mgsx.gltf.scene3d.utils.IBLBuilder
import net.mgsx.gltf.scene3d.utils.ShaderParser

//TODO: If camera change position -> change spider direction
//TODO: Create a list of monsters
//TODO: Detect if user pick on monster, and destroy it with some effect
//TODO: Add sound fx: scream, attack...
//TODO: Use ArCore Raw Depth
class ArPlayground: GdxArApplicationListener() {
    private lateinit var sceneManager: CustomSceneManager
    private lateinit var directionalLight: CustomDirectionalShadowLight

    private var spider: Spider? = null

    override fun create() {
        createEnvironment()

        //Setup some configs
        arAPI.setPowerSaveMode(false)
        arAPI.setAutofocus(true)
        arAPI.enableSurfaceGeometry(true)
        //Start AR!
        arAPI.setRenderAR(true)
    }

    //Setup glTF rendering environment
    private fun createEnvironment() {
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
        directionalLight.direction.set(1f, -3f, 1f).nor()//Direction adapted by camera later: GdxLightEstimationMode.ENVIRONMENTAL_HDR
        directionalLight.color.set(Color.WHITE)
        sceneManager.environment.add(directionalLight)
        val iblBuilder = IBLBuilder.createOutdoor(directionalLight)
        val diffuseCubemap = iblBuilder.buildIrradianceMap(256);
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

        //Setup glTF -> AR Bind
        sceneManager.setCamera(arAPI.arCamera)
        directionalLight.setViewport(
            6f,
            6f,
            sceneManager.camera.near,
            sceneManager.camera.far)
    }

    override fun renderARModels(frame: GdxFrame) {
        //Update environment light based on AR frame
        when(frame.lightEstimationMode) {
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
        // But to do this we should save the origin point and the offset of walking...
//        for(anchor in frame.anchors) {
//            if(spider?.anchorId == anchor.id) {
//                spider?.relocate(anchor.gdxPose.position, anchor.gdxPose.rotation)
//            }
//        }

        spider?.update(Gdx.graphics.deltaTime)
        sceneManager.update(Gdx.graphics.deltaTime)
        sceneManager.render()
    }

    override fun render() {
        //TODO: I wan the spider to follow the player (the camera position in scene)
        //If i could get the position of the spider in screen so to recreate in case camera change position.... as if it was a touch
        if (Gdx.input.isTouched) {
            Log.e("Play", "touch------------------INPUT: ${Gdx.input.x} / ${Gdx.input.y}")
            handleTouch(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        }
    }

    private fun handleTouch(x: Float, y: Float) {
        if (spider == null) {
            val newAnchor: GdxAnchor? = arAPI.requestHitPlaneAnchor(x, y, GdxPlaneType.ANY)
            if (newAnchor != null) {
                spider = Spider(sceneManager, newAnchor)//, arAPI.arCamera)
                //Pools.free(newAnchor)---> Wanna keep using its position inside Spider...
            }
        }
        else {
            val pose: GdxPose? = arAPI.requestHitPlanePose(x, y, GdxPlaneType.ANY)
            if (pose != null) {
                spider?.relocate(pose.position, pose.rotation)
                Pools.free(pose)
            }
        }
    }
}
