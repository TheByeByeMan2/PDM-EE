
export type WaitingRoomResponse = {
    userId: number
    username: string,
    gameId: number | undefined
    isGo: boolean,
    time: Date,
    ruleId: number
}

export enum UserState{
    FREE = 'FREE', WAITING = 'WAITING', BATTLE='BATTLE'
}

export type UserComplexInfoRes = {
    userId: number,
    userState: UserState,
    gameId: number,
    ruleId: number
}

export type WaitingRoomMsgObj = {
    userId: number
    ruleId:number
}