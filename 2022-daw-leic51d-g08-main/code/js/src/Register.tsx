import * as React from "react";
import { useState } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { regsiterFetch } from "./requests";

function checkPass(pass: string, verPass: string) {
    if (pass !== verPass) throw new Error('The pass not are equal')
}

export function RegisterPage() {
    const [error, setError] = useState(undefined)
    const [inputs, setInputs] = useState({
        username: "",
        password: "",
        confirmPass: ""
    })
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [redirect, setRedirect] = useState(false)
    const location = useLocation()
    if (redirect) {
        return <Navigate to={location.state?.source?.pathname || "/login"} replace={true} />
    }
    function handleChange(ev: React.FormEvent<HTMLInputElement>) {
        const name = ev.currentTarget.name
        setInputs({ ...inputs, [name]: ev.currentTarget.value })
        setError(undefined)
    }
    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        setIsSubmitting(true)
        try {
            setError(undefined)
            const username = inputs.username
            const password = inputs.password
            const verPass = inputs.confirmPass
            checkPass(password, verPass)
            if (password === verPass) {
                regsiterFetch(username, password).then((value) => {
                    if (value){setRedirect(true)}
                    else {
                        console.log('register no return')
                        setIsSubmitting(false)
                    }
                })
            }
        } catch (err) {
            setError(err.message)
            setIsSubmitting(false)
        }
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
                    <input id="password" type="password" name="password" value={inputs.password} onChange={handleChange} />
                </div>
                <div>
                    <label htmlFor="confirmPass">confirmPass</label>
                    <input id="confirmPass" type="password" name="confirmPass" value={inputs.confirmPass} onChange={handleChange} />
                </div>
                <div>
                    <button type="submit">Register</button>
                </div>
            </fieldset>
            {error}
        </form>
    )
}