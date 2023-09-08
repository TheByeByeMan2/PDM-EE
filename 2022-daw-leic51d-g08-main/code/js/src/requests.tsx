import {
    useState,
    useEffect,
} from 'react'
import { Game, GridCellResponse, SendInfoStruct } from './game/GameDomains'
import {
    UserLogin, UserIdAndToken
} from './login/login'
import { UserDetails } from './ranking'
import { Rule, ShipType } from './rule/Rule'
import { UserComplexInfoRes } from './waitingRoom/waittingRoomDomains'

const PROTOCOL = "http://"
const PORT = ":8081"
const HOST = "localhost"
const API = "/api"

const GET_RANKING = "/ranking"
const GET_AUTHORS = "/authors"
const LOGIN = "/login"
const USER_INFO = '/user'
const WAITING_ROOM = '/games/waiting'
const ALL_RULES = "/rules"

export const URL_GET_RANKING = PROTOCOL + HOST + PORT + API + GET_RANKING
export const URL_GET_AUTHORS = PROTOCOL + HOST + PORT + API + GET_AUTHORS
export const URL_LOGIN_USER = PROTOCOL + HOST + PORT + API + LOGIN
export const URL_USER_INFO = PROTOCOL + HOST + PORT + API + USER_INFO
export const URL_WAITING_ROOM = PROTOCOL + HOST + PORT + API + WAITING_ROOM
export const URL_ALL_RULES = PROTOCOL + HOST + PORT + API + ALL_RULES

enum Action {
    PUT = "PUT", REMOVE = "REMOVE"
}

export type ResponseStruct = {
    body: object,
    header: string,
}

export function getRanking(): string {
    return useFetch_NoAuthentication(URL_GET_RANKING)
}

export function getAuthors(): string {
    return useFetch_NoAuthentication(URL_GET_AUTHORS)
}

export function loginUser(userLogin: UserLogin): UserIdAndToken {
    return loginFetch(userLogin)
}

export async function regsiterFetch(username: string, pass: string) {
    async function doFetch() {
        const resp = await fetch('api/register', {
            method: 'POST',
            body: JSON.stringify({ userName: username, pass: pass }),
            headers: {
                'Content-Type': 'application/json',
            }
        })
        const bodyRes = await resp.json()
        if (resp.status !== 201) throw new Error(bodyRes)
        if (bodyRes) {
            return bodyRes
        } else return undefined
    }

    return await doFetch()
}

export function getUserInfo(userIdAndToken: UserIdAndToken): string {
    const url = URL_USER_INFO + '/' + userIdAndToken.userId
    return useFetch_authorized(url, userIdAndToken.token)
}

export async function getUserComplextInfo(userIdAndToken: UserIdAndToken): Promise<UserComplexInfoRes> {
    const resp = await fetch(`api/user/special/${userIdAndToken.userId}`, {
        method: "GET",
        headers: {
            'Content-Type': 'application/json',
            'Authorization': userIdAndToken.token,
            'userId': userIdAndToken.userId.toString()
        },
    })
    return await resp.json()
}

export async function getServerGame(userIdAndToken: UserIdAndToken, gameId: number): Promise<Game> {
    const resp = await fetch(`api/games/${gameId}/info`, {
        method: "GET",
        headers: {
            'Content-Type': 'application/json',
            'Authorization': userIdAndToken.token,
            'userId': userIdAndToken.userId.toString()
        },
    })
    return await resp.json()   
}

export async function setGameReady(userIdAndToken: UserIdAndToken, gameId: number): Promise<Game> {
    const resp = await fetch(`api/games/${gameId}/isReady`, {
        method: "POST",
        headers: {
            'Content-Type': 'application/json',
            'Authorization': userIdAndToken.token,
        },
        body: JSON.stringify({ userId: userIdAndToken.userId })
    })
    return await resp.json()
}

export async function putShips(userIdAndToken: UserIdAndToken, gameId: number, shipTypes: Array<SendInfoStruct>) {
    try{
        console.log(shipTypes)
    shipTypes.forEach((value) => 
        putOneShip(userIdAndToken, gameId,value).then(value => {
            console.log(value)
        })
    )
    }catch(err){
        throw new Error(err.mensage)
    }
}

export async function getPutShip(userIdAndToken: UserIdAndToken, gameId: number): Promise<Array<GridCellResponse>> {
    const resp = await fetch(`api/games/${gameId}/building`, {
        method: "GET",
        headers: {
            'Content-Type': 'application/json',
            'Authorization': userIdAndToken.token,
            'userId': userIdAndToken.userId.toString()
        },
    })
    const body =  await resp.json()
    if (resp.status == 200){
        return body
    } else {
        throw new Error(`error, unexpected status ${resp.status}`)
    }
}

async function putOneShip(userIdAndToken: UserIdAndToken, gameId: number, shipType: SendInfoStruct): Promise<GridCellResponse> {
    const resp = await fetch(`api/games/${gameId}/building`, {
        method: "POST",
        headers: {
            'Content-Type': 'application/json',
            'Authorization': userIdAndToken.token,
        },
        body: JSON.stringify({ userId: userIdAndToken.userId, shipName: shipType.shipName, position: shipType.position, direction: shipType.direction, action: Action.PUT })
    })
    const body =  await resp.json()
    if (resp.status == 200){
        return body
    } else {
        throw new Error(`error, unexpected status ${resp.status}`)
    }
}


export function loginFetch(data: UserLogin): UserIdAndToken | undefined {
    const [resp_body, setBody] = useState(undefined)
    const [resp_token, setToken] = useState(undefined)
    useEffect(() => {
        let cancelled = false
        async function doFetch() {
            const resp = await fetch(URL_LOGIN_USER, {
                method: 'POST',
                body: JSON.stringify(data),
                headers: {
                    'Content-Type': 'application/json',
                }
            })
            const bodyRes = await resp.json()
            const tokenRes = resp.headers.get("Authorization")
            if (!cancelled) {
                console.log('body =' + resp_body)
                console.log('token =' + resp_token)
                setBody(bodyRes)
                setToken(tokenRes)
            }
        }
        setBody(undefined)
        setToken(undefined)
        doFetch()
        return () => {
            cancelled = true
        }
    }, [URL_LOGIN_USER])
    return {
        userId: resp_body,
        token: resp_token
    }
}

export function
    useFetch(url: string, method: string = 'GET', authorization_header: string = undefined, userId_header: string = undefined, data: object = undefined): string | undefined {
    const [content, setContent] = useState(undefined)
    useEffect(() => {
        let cancelled = false
        async function doFetch() {
            const resp = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': authorization_header,
                    'userId': userId_header,
                },
                mode:'no-cors',
                body: JSON.stringify(data)
            })
            const body = await resp.json()
            if (!cancelled) {
                setContent(body)
            }
        }
        setContent(undefined)
        doFetch()
        return () => {
            cancelled = true
        }
    }, [url])
    return content
}

export function useFetch_authorized(url: string, authorization_header:string): string | undefined{
    const [content, setContent] = useState(undefined)
    useEffect(() => {
        let cancelled = false
        async function doFetch() {
            const resp = await fetch(url, {
                method: 'GET',
                headers:{
                    'Content-Type': 'application/json',
                    'Authorization': authorization_header,
                }
            })
            const body = await resp.json()
            if (!cancelled) {
                console.log(body)
                setContent(body)
            }
        }
        setContent(undefined)
        doFetch()
        return () => {
            cancelled = true
        }
    }, [url])
    return content
}

export function useFetch_NoAuthentication(url: string): string | undefined{
    const [content, setContent] = useState(undefined)
    useEffect(() => {
        let cancelled = false
        async function doFetch() {
            const resp = await fetch(url, {
                method: 'GET',
            })
            const body = await resp.json()
            if (!cancelled) {
                console.log(body)
                setContent(body)
            }
        }
        setContent(undefined)
        doFetch()
        return () => {
            cancelled = true
        }
    }, [url])
    return content
}
