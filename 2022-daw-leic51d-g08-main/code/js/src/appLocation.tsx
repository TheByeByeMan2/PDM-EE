import * as React from 'react'
import {
    useState,
    createContext,
    useContext,
} from 'react'

export enum AppLocalization {
    LOBBY = 'LOBBY', RANKING = 'RANKING', WAITING = 'WAITING', USER_INFO = 'USER INFO', BATTLE = 'BATTLE'
  };

type AppLocationContextType = {
    appLocation: AppLocalization | undefined,
    setAppLocation: (v: AppLocalization | undefined) => void
}

const LoggedInContext = createContext<AppLocationContextType>({
    appLocation: undefined,
    setAppLocation: () => { },
})

export function AppLocationContainer({ children }: { children: React.ReactNode }) {
    const [appLocation, setAppLocation] = useState(undefined)
    console.log(`AppLocationContainer: ${appLocation}`)
    return (
        <LoggedInContext.Provider value={{ appLocation: appLocation, setAppLocation: setAppLocation }}>
            {children}
        </LoggedInContext.Provider>
    )
}

export function useCurrentAppLocation() {
    return useContext(LoggedInContext).appLocation
}

export function useSetAppLocation() {
    return useContext(LoggedInContext).setAppLocation
}
