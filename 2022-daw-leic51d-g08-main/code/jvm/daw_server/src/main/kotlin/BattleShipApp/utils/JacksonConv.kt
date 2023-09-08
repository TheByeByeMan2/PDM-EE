package BattleShipApp.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

class JacksonConv {
    companion object{
        private val objMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())
        fun classToJson(t: Any): String {
            return objMapper.writeValueAsString(t)
        }
        fun <T>jsonToClass(json: String,  valueType: Class<T>): T{
            return objMapper.readValue(json, valueType)
        }
    }
}