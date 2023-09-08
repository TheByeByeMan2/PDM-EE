import * as React from "react";
import { useState } from "react";
import { Navigate, useLocation, useNavigate, Link } from "react-router-dom";
import { backNavigate } from "../utils";
import { URL_LOGIN_USER } from "../requests";
import { useCurrentLocalData, useSetCurrentLocalData } from "../localData/LocalData";

export type UserLogin = {
    userName: string;
    pass: string;
};

export type UserIdAndToken = {
    userId: number;
    token: string;
};

export async function authentication(
    username: string,
    password: string,
): Promise<UserIdAndToken | undefined> {

    async function doFetch() {
        const resp = await fetch(URL_LOGIN_USER, {
            method: 'POST',
            body: JSON.stringify({ userName: username, pass: password }),
            headers: {
                'Content-Type': 'application/json',
            },
        })
        const bodyRes: number = await resp.json()
        const tokenRes: string = resp.headers.get("Authorization")
        if (bodyRes && tokenRes) {
            return { userId: bodyRes, token: tokenRes }
        } else return undefined
    }

    return await doFetch()
}

export function Login() {
    console.log("Login");
    const [inputs, setInputs] = useState({
        username: "Player2",
        password: "Player2Pass@",
    })
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [error, setError] = useState(undefined)
    const [redirect, setRedirect] = useState(false)
    const currentLocalData = useCurrentLocalData()
    const setLocalData = useSetCurrentLocalData()
    const location = useLocation()
    if (redirect) {
        return <Navigate to={location.state?.source?.pathname || "/userInfo"} replace={true} />
    }
    function handleChange(ev: React.FormEvent<HTMLInputElement>) {
        const name = ev.currentTarget.name
        setInputs({ ...inputs, [name]: ev.currentTarget.value })
        setError(undefined)
    }
    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        setIsSubmitting(true)
        const username = inputs.username
        const password = inputs.password
        authentication(username, password)
            .then(res => {
                setIsSubmitting(false)
                if (res) {
                    setLocalData({userIdAndToken: res, game: undefined})
                    setRedirect(true)
                } else {
                    setError("Invalid username or password")
                }
            })
            .catch(error => {
                setIsSubmitting(false)
                setError(error.message)
            })
    }

    return (
        <form onSubmit={handleSubmit}>
            <fieldset disabled={isSubmitting}>
                <div>
                    <label htmlFor="username">Username</label>
                    <input id="username" type="text" name="username" value={inputs.username} onChange={handleChange} />
                </div>
                <div>
                    <label htmlFor="password">Password</label>
                    <input id="password" type="text" name="password" value={inputs.password} onChange={handleChange} />
                </div>
                <div>
                    <button type="submit">Login</button>
                </div>
            </fieldset>
            {error}
        </form>
    )
}


export function LoginPage() {
    return (
        <div>
            <table>
                <tbody>
                    <tr>
                        <td>{backNavigate()}</td>
                    </tr>
                    <tr>
                        <td>{Login()}</td>
                    </tr>
                    <tr>
                        <td>
                            <Link to="/register"> register </Link>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    );
}