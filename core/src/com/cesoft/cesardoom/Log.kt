package com.cesoft.cesardoom

import com.badlogic.gdx.Gdx

object Log {
//    init {
//        Gdx.app.logLevel = Application.LOG_DEBUG
//    }
//    fun d(tag: String, msg: String) {
//        Gdx.app.debug(tag, msg)
//    }
    fun e(tag: String, msg: String) {
        Gdx.app.error(tag, msg)
    }
}