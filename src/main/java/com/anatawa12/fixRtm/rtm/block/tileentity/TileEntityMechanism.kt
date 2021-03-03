package com.anatawa12.fixRtm.rtm.block.tileentity

import com.anatawa12.fixRtm.addEntityCrashInfoAboutModelSet
import jp.ngt.rtm.block.tileentity.TileEntityMechanism
import net.minecraft.crash.CrashReportCategory

@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "unused")
fun TileEntityMechanism.addInfoToCrashReport(category: CrashReportCategory) =
    addEntityCrashInfoAboutModelSet(category) { resourceState?.resourceSet?.config }