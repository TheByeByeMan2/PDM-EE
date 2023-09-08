import * as React from 'react'
import {
    useState,
    createContext,
    useContext,
} from 'react'
import { UserIdAndToken } from '../login/login'

export enum AppLocalization {
    LOBBY = 'LOBBY', RANKING = 'RANKING', WAITING = 'WAITING', USER_INFO = 'USER INFO', BATTLE = 'BATTLE'
};

type GameIdAndRuleId = {
    gameId: number,
    ruleId: number
}

type LocalData = {
    userIdAndToken: UserIdAndToken | undefined
    game: GameIdAndRuleId | undefined
}

type ContextType = {
    LocalData: LocalData | undefined,
    setLocalData: (v: LocalData | undefined) => void
}
const GameLocalDataInContext = createContext<ContextType>({
    LocalData: undefined,
    setLocalData: () => { },
})

export function LocalDataContainer({ children }: { children: React.ReactNode }) {
    const [LocalData, setLocalData] = useState(undefined)
    console.log(`gameLocalDataContainer: ${LocalData}`)
    return (
        <GameLocalDataInContext.Provider value={{ LocalData: LocalData, setLocalData: setLocalData }}>
            {children}
        </GameLocalDataInContext.Provider>
    )
}

export function useCurrentLocalData() {
    return useContext(GameLocalDataInContext).LocalData
}

export function useSetCurrentLocalData() {
    return useContext(GameLocalDataInContext).setLocalData
}
