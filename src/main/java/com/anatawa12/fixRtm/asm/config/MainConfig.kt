/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.asm.config

import com.anatawa12.fixRtm.Loggers
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.common.Loader

object MainConfig {
    private val configFile = Loader.instance().configDir.resolve("fix-rtm.cfg")
    private val config = Configuration(configFile)

    private const val categoryFixRTM = "fixrtm"
    private const val categoryModelLoading = "model_loading"
    private const val categoryBetterRtm = "better_rtm"
    private const val categoryBetterNgtLib = "better_ngtlib"

    @JvmField
    val modelPackLoadSpeed: ModelPackLoadSpeed

    init {
        val modelPackLoadSpeedInt = config.getInt(
            "modelPackLoadSpeed", categoryModelLoading,
            ModelPackLoadSpeed.WorkStealing.configValue,
            ModelPackLoadSpeed.minValue(),
            ModelPackLoadSpeed.maxValue(),
            ModelPackLoadSpeed.computeComment())

        val multiThreadConstructEnabledProp = config.getCategory(categoryModelLoading).remove("multiThreadConstructEnabled")
        if (multiThreadConstructEnabledProp != null) {
            @Suppress("SimplifyBooleanWithConstants")
            if (multiThreadConstructEnabledProp.getBoolean(true) == false) {
                config.getCategory(categoryModelLoading)
                    .get("modelPackLoadSpeed")!!
                    .set(ModelPackLoadSpeed.UseOriginal.configValue)
            }
        }
        modelPackLoadSpeed = ModelPackLoadSpeed.byValue(modelPackLoadSpeedInt)
    }

    @JvmField
    val multiThreadModelConstructEnabled = config.getBoolean(
        "multiThreadConstructEnabled", categoryModelLoading,
        true,
        "constructs models using a thread with a number of logical cores")

    @JvmField
    val cachedPolygonModel = config.getBoolean(
        "cachedPolygonModelEnabled", categoryModelLoading,
        true,
        "caches obj, mqo model.")

    private val scriptingModeStr = config.getString(
        "scriptingMode", categoryModelLoading,
        "use-default",
        "scripting mode. the value is one of the list below:\n" +
                "cache-with-sai      : the fastest mode but not stable. some script may make error.\n" +
                "better-with-nashorn : same runtime as RTM but a little faster than RTM.\n" +
                "use-rtm-normal      : same as RTM. this is the slowest mode.\n" +
                "use-default         : use default mode. currently use-rtm-normal.\n")

    val scriptingMode: ScriptingMode

    init {
        var scriptingMode = ScriptingMode.getByConfigValue(scriptingModeStr.lowercase())
        if (scriptingMode == null) {
            if (scriptingModeStr.lowercase() == ScriptingMode.defaultConfigValue) {
                scriptingMode = ScriptingMode.default
            } else {
                Loggers.getLogger("Config").fatal("your scriptingMode is not valid so we use default.")
                scriptingMode = ScriptingMode.default
            }
        }
        this.scriptingMode = scriptingMode
    }

    @JvmField
    val useOurScripting = scriptingMode != ScriptingMode.UseRtmNormal

    @JvmField
    val reduceConstructModelLog = config.getBoolean(
        "reduceConstructModelLog", categoryModelLoading,
        true,
        "reduce 'Construct Model' and 'Registr resource' logs.")

    @JvmField
    val dummyModelPackEnabled = config.getBoolean(
        "dummyModelPackEnabled", categoryBetterRtm,
        true,
        "use dummy ModelPack generated by fixRTM for not loaded models")

    @JvmField
    val markerDistanceMoreRealPosition = config.getBoolean(
        "markerDistancesMoreRealPosition", categoryBetterRtm,
        true,
        "shows distance signs of marker at more real position")

    @JvmField
    val changeTestTrainTextureEnabled = config.getBoolean(
        "changeTestTrainTexture", categoryBetterRtm,
        true,
        "change texture for test train to make easy to identify test train and electric train")

    @JvmField
    val addModelPackInformationInAllCrashReports: Boolean
    init {
        val comment = "adds model pack information about all models in compressed format in all crash reports. " +
                "This may make your crash report very fat."
        val defaultValue = false

        val prop = config.get(categoryBetterRtm, "addModelPackInformationInAllCrashReports", false)
        prop.languageKey = "addModelPackInformationInAllCrashReports"
        if (prop.comment == null || !prop.comment.endsWith("[default: $defaultValue]"))
            prop.set(defaultValue)
        prop.comment = "$comment [default: $defaultValue]"
        addModelPackInformationInAllCrashReports = prop.getBoolean(defaultValue)
    }

    @JvmField
    val useThreadLocalProperties = config.getBoolean(
        "useThreadLocalProperties", categoryBetterRtm,
        true,
        "fix compatibility problem with CustomNPCs using ThreadLocalProperties. " +
                "ThreadLocalProperties is not stable enough so this fix is optional.")

    @JvmField
    val allowPlacingVehiclesOnProtectedRail = config.getBoolean(
        "allowPlacingVehiclesOnProtectedRail", categoryBetterRtm,
        true,
        "allow placing vehicles on protected rail")

    @JvmField
    val showRailLength = config.getBoolean(
        "showRailLength", categoryBetterRtm,
        true,
        "Show current length of rail at the center of path")

    @JvmField
    val mergeMarker = config.getBoolean(
        "mergeMarker", categoryBetterRtm,
        true,
        "Merge diagonal and axis aligned marker to one marker.\n" +
                "Actually, this just changes registered item in creative tab to merged marker.\n" +
                "Regardless this flag, both unmerged & merged marker item is exist.\n" +
                "You can change this flag regardless server setting.")

    @JvmField
    val actionPartsImprovements = config.getBoolean(
            "actionPartsImprovements", categoryBetterRtm,
            true,
            "Improvements for Action Parts. This includes:\n" +
                    "  - Deny clicking far action parts.")

    @JvmField
    val addAllowAllPermissionEnabled = config.getBoolean(
        "addAllowAllPermission", categoryBetterNgtLib,
        true,
        "adds a permission meaning all permissions are approved")

    @JvmField
    val addNegativePermissionEnabled = config.getBoolean(
        "addNegativePermission", categoryBetterNgtLib,
        true,
        "adds permissions to disallow some permission. this overrides op and 'fixrtm.all_permit'.")

    @JvmField
    val expandPlayableSoundCount = config.getBoolean(
        "expandPlayableSoundCount", categoryFixRTM,
        true,
        "expands the count of playable sound count at the same time. this may cause compatibility issue with Immersive Vehicles.")

    init {
        if (config.hasChanged()) {
            config.save()
        }
    }

    enum class ScriptingMode(vararg val configValues: String) {
        CacheWithSai("cache-with-sai", "cache-with-rhino"),
        BetterWithNashorn("better-with-nashorn"),
        UseRtmNormal("use-rtm-normal"),
        ;

        companion object {
            private val byConfigValue = values()
                .flatMap { it.configValues.map { k -> k to it } }
                .toMap()

            fun getByConfigValue(value: String) = byConfigValue[value]

            val default = UseRtmNormal

            const val defaultConfigValue = "use-default"
        }
    }

    enum class ModelPackLoadSpeed(val configValue: Int, val description: String) {
        UseOriginal(0, "Slowest; Use Original"),
        SingleThreaded(1, "Slow; Single thread"),
        MultiThreaded(2, "Faster; use one of third of your processor"),
        WorkStealing(3, "Fastest; use all processors; Default"),
        ;
        companion object {
            private val byValue = values().associateBy { it.configValue }
            fun byValue(value: Int) = byValue[value] ?: error("invalid or unsupported loading speed")
            fun minValue() = values().minByOrNull { it.configValue }!!.configValue
            fun maxValue() = values().maxByOrNull { it.configValue }!!.configValue
            fun computeComment() = values()
                .sortedBy { it.configValue }
                .joinToString("") { "${it.configValue}: ${it.description}\n" }
        }
    }
}
