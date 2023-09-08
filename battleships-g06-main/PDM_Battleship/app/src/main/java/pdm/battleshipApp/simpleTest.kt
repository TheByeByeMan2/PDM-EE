package pdm.battleshipApp

import pdm.battleshipApp.utils.JacksonConv


fun main(){
    convert()
}

fun convert(){
    data class A(val a: Int, val b: Int, val c: Int?)
    val json = "{\"a\":1,\"b\":2}"
    val data = JacksonConv.jsonToClass(json, A::class.java)
    println(data)
}