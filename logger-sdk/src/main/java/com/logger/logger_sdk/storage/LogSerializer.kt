package com.logger.logger_sdk.storage

interface LogSerializer<T>{
    fun serialize(value:T):String
    fun deserialize(json:String):T
}