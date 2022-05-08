package com.su.charging

import java.io.File

object Charging {

    val resPath = App.globalContext.getExternalFilesDir("res")!!

    val videoResPath: File = File(resPath, "video").apply {
        if (!exists())
            mkdirs()
    }

    val normalChargingVideo = File(videoResPath, "normal_charging.mp4")

    val quickChargingVideo = File(videoResPath, "quick_charging.mp4")

    val audioResPath: File = File(resPath, "audio").apply {
        if (!exists())
            mkdirs()
    }
}