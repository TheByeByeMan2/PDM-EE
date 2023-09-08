import * as React from 'react'
import { createElement } from 'react'
import {
    backNavigate,
    ShowRespose
} from './utils'
import {
    getRanking,
    URL_GET_RANKING
} from './requests'

export type UserDetails = {
    userId: number,
    name: string,
    score: number,
    gamesPlayed: number,
}

function createRankingTable(users: Array<UserDetails> | undefined) {
    if (users === undefined) return (
        <div>
            ...is loading
        </div>)
    return createElement(
        "table",
        {}, createElement('tbody', {}, createElement("tr", {},
            createElement("td", { width: "134", align: "center" }, "Name"),
            createElement("td", { width: "134", align: "center" }, "Game Played"),
            createElement("td", { width: "134", align: "center" }, "Score")
        ),
            users.map((user, idx) =>
                createElement("tr", {key:idx},
                    createElement("td", { width: "134", align: "center" }, user.name),
                    createElement("td", { width: "134", align: "center" }, user.gamesPlayed),
                    createElement("td", { width: "134", align: "center" }, user.score),
                )
            ))
    )
}

export const exampleUsers: Array<UserDetails> = [
    { userId: 1, name: "user1", gamesPlayed: 100, score: 100 },
    { userId: 2, name: "user2", gamesPlayed: 99, score: 90 }
]

function createUsers(content: string | undefined): Array<UserDetails> {
    if (content === undefined) return undefined
    else {
        return JSON.parse(JSON.stringify(content, null, 2))
    }
}

export function RankingTable() {
    const fetch = getRanking()
    const ranking = createRankingTable(createUsers(fetch))
    return (
        <div>
            {backNavigate()}
            {ranking}
        </div>
    )

}