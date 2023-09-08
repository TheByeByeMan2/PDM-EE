

export async function gameSender(gameId:number,msg: string) {
    const resp = await fetch(`/api/games/${gameId}/chat`, {
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