import * as React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useCurrentLocalData } from '../localData/LocalData'

export function RequireAuthn({ children }: { children: React.ReactNode }): React.ReactElement {
    const localData = useCurrentLocalData()
    const location = useLocation()
    console.log(`currentUser = ${localData}`)
    if (localData === undefined || localData.userIdAndToken === undefined) {
        console.log("redirecting to login")
        return <Navigate to="/login" state={{source: location.pathname}} replace={true}/>
    } else {
        return <>{children}</>
    }

}