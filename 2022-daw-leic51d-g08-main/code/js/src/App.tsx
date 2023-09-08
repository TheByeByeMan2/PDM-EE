import * as React from 'react'
import {
    createBrowserRouter, Link, Outlet, RouterProvider, useParams,
} from 'react-router-dom'
import { RankingTable } from "./ranking"
import { AuthorsInfo } from "./authors"
import { LoginPage } from "./login/login"
import { RequireAuthn } from './login/RequireAuthn'
import { UserInfoPage } from './userInfo'
import { Chat } from './chat/Chat'
import { WaitingRoom } from './waitingRoom/waitingRoom'
import { AddRule } from './rule/AddRule'
import { AppLocalization, AppLocationContainer, useCurrentAppLocation, useSetAppLocation } from './appLocation'
import { Game } from './game/Game'
import { LocalDataContainer } from './localData/LocalData'
import { RegisterPage } from './Register'

// para login funcionar tem de ser children de <AuthnContainer><Outlet /></AuthnContainer>
const router = createBrowserRouter([
    {
        "path": "/",
        "element":<LocalDataContainer><Outlet /></LocalDataContainer>,
        "children": [
            {
                "path": "/",
                "element": <Home />,
            },
            {
                "path": "/login",
                "element": <Login />
            },
            {
                "path": "/waitingRoom",
                "element":<RequireAuthn> <StartGame /> </RequireAuthn> ,
            },
            {
                "path": "/waitingRoom/rule/add",
                "element": <RequireAuthn><AddNewRule /></RequireAuthn>
            },
            {
                "path": "/userInfo",
                "element": <RequireAuthn><UserInfo /></RequireAuthn>
            },
            {
                "path": "/ranking",
                "element": <Ranking />
            },
            {
                "path": "/authors",
                "element": <Authors />
            },
            {
                "path": "/register",
                "element": <Register />
            },
            {
                "path": "/chat",
                "element": <Chat />
            },
            {
                "path": "/game",
                "element": <Game />
            },
        ]
    },

])

export function App() {
    return (
        <RouterProvider router={router} />
    )
}

function Home() {
    const setLocation = useSetAppLocation()
    setLocation(AppLocalization.LOBBY)
    return (
        <div>
            <h1>Home</h1>
            <ol>
                <li><Link to="/ranking"> ranking </Link></li>
                <li><Link to="/userInfo"> UserInfo</Link></li>
                <li><Link to="/login"> login </Link></li>
                <li><Link to="/authors"> authors </Link></li>
                <li><Link to="/waitingRoom"> start </Link></li>
            </ol>
            <Outlet />
        </div>
    )
}

function Ranking() {
    const setLocation = useSetAppLocation()
    setLocation(AppLocalization.RANKING)
    return (
        <div>
            <h1>RANKING</h1>
            <RankingTable />
        </div>
    )
}

function Login() {
    return (
        <div>
            <h1>LOGIN</h1>
            <LoginPage />
        </div>
    )
}

function UserInfo() {
    const setLocation = useSetAppLocation()
    setLocation(AppLocalization.USER_INFO)
    return (
        <div>
            <h1>USER INFO</h1>
            <UserInfoPage />
        </div>
    )
}

function StartGame() {
    const setLocation = useSetAppLocation()
    setLocation(AppLocalization.WAITING)
    return (
        <div>
            <h1>START GAME</h1>
            <WaitingRoom />
        </div>
    )
}

function AddNewRule(){
    return (
        <div>
            <h1>ADD RULE</h1>
            <AddRule />
        </div>
    )
}

function Register() {
    return (
        <div>
            <h1>REGISTER</h1>
            <RegisterPage />
        </div>
    )
}


function Authors() {
    return (
        <div>
            <h1>AUTHORS</h1>
            <AuthorsInfo />
        </div>
    )
}