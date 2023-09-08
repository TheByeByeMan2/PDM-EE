package pdm.battleshipApp.service

import okhttp3.Call
import okhttp3.EventListener

class AppListener : EventListener() {
    override fun callStart(call: Call) {
        super.callStart(call)
    }

    override fun requestBodyStart(call: Call) {
        super.requestBodyStart(call)
    }

}