import * as React from 'react'
import {createElement} from 'react'
import{
    backNavigate
} from './utils'
import{
    getAuthors
}from './requests'

export type ProjectInfo = {
    authors :Array<string>,
    version: number
}

function createAuthor(projectInfo: ProjectInfo | undefined){
    if (projectInfo === undefined) return (
        <div>
            ...is loading
        </div>
    )
    return createElement(
        "dl",
        {}, createElement("dt", {}, "Authors:"),
        projectInfo.authors.map((author,idx) =>
            createElement("dd", {key:idx}, author),
            ),
        createElement("dt", {}, "Version:"),
        createElement("dd", {}, projectInfo.version.toString())
    )
}

const exampleAuthor: Array<string> = [
    "Kaiwei","Tiago"
] 

function createAuthors(content: string | undefined):ProjectInfo | undefined {
    if (content === undefined) return undefined
    else {
        return JSON.parse(JSON.stringify(content, null, 2))
    }
}

export function AuthorsInfo() {
    const fetch = getAuthors()
    const authors = createAuthor(createAuthors(fetch))

    return (
        <div>
            {backNavigate()}
            {authors}
        </div>
    )

}