import * as React from "react"
import { useState } from "react"

export async function waitingSender(msg: string) {
    const resp = await fetch("/api/games/waiting/chat", {
        method: "POST",
        headers: {
            "content-type": "text/plain",
        },
        body: msg,
    })
    if (resp.status != 200) {
        throw new Error(`error, unexpected status ${resp.status}`)
    }
}