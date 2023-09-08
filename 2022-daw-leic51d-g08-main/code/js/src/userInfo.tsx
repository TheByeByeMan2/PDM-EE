import * as React from 'react'
import { createElement } from 'react'
import { useCurrentLocalData } from './localData/LocalData'
import { UserDetails } from './ranking'
import { getUserInfo } from './requests'
import { backNavigate } from './utils'

function createUserDetailObject(content: string | undefined): UserDetails | undefined {
    if (content === undefined) return undefined
    else {
        return JSON.parse(JSON.stringify(content, null, 2))
    }
}

function userInformation(user: UserDetails | undefined) {
    if (user === undefined) return (
        <div>
            ...is loading
        </div>
    )
    return createElement(
        "dl",
        {}, createElement("dt", {}, "Name:"),
        createElement("dd", {}, user.name),
        createElement("dt", {}, "Game Played:"),
        createElement("dd", {}, user.gamesPlayed),
        createElement("dt", {}, "Score:"),
        createElement("dd", {}, user.score)
    )
}

export function UserInfoPage() {
    const localData = useCurrentLocalData()
    console.log(localData.userIdAndToken)
    const userDetailsObject = createUserDetailObject(getUserInfo(localData.userIdAndToken))
    const userInfo = userInformation(userDetailsObject)

    return (
        <div>
            {backNavigate()}
            {userInfo}
        </div>
    )
}