import * as React from 'react'
import { useNavigate} from "react-router-dom";
import{
    loginFetch,
    useFetch
} from './requests'
import {
    Link
} from 'react-router-dom'

import { UserIdAndToken, UserLogin } from './login/login';

export const backNavigate = () => {
    let navigate = useNavigate();
    return (
        <>
          <button onClick={() => navigate(-1)}>Back</button> 
        </>
    );
};

export function goToHome(){
    return (
        <Link to="/"> home </Link>
    )

}

export function ShowRespose(url: string){
    return(
        <div>
            <p>{url}</p>
            <Show url={url} />
        </div>
    )
}



export function Show({ url }: { url: string }) {
    const content = useFetch(url)

    if (!content) {
        return (
            <div>
                ...loading...
            </div>
        )
    }

    return (
        <div>
            <pre>
                {JSON.stringify(content, null, 2)}
            </pre>
        </div>
    )
}

export function ShowLoginRespose(data: UserLogin){
    return(
        <div>
            <p>{JSON.stringify(data)}</p>
            <ShowLogin data={data} />
        </div>
    )
}

export function ShowLogin({ data }: { data: UserLogin }) {
    const content = loginFetch(data)

    if (!content) {
        return (
            <div>
                ...loading...
            </div>
        )
    }

    return (
        <div>
            <pre>
                {JSON.stringify(content, null, 2)}
            </pre>
        </div>
    )
}

export function printlnLog(title, msg){
    const t = '::: '+title+' :::'
    const m = ' -| '+msg
    console.log(t+'\n'+m)
}