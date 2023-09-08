import { Rule, ShipType } from "../rule/Rule"

export enum ShipState {
    ALIVE = 'ALIVE', SHOT = 'SHOT', SUNK = 'SUNK'
}

export enum Direction {
    VERTICAL = 'VERTIVAL', HORIZONTAL = 'HORIZONTAL'
}

export type Position = {
    col: number,
    row: number
}

export type Ship = {
    shipType: ShipType
    shipState: ShipState
    headPos: Position
}

export type GridCell = {
    position: Position
    ship: Ship | undefined
}

export type ShipTypeAndDirection = {
    shipType: ShipType
    direction: Direction
}

export type SendInfoStruct = {
    shipName: string
    position: Position
    direction: Direction
}

export type SourceState =
    | ShipTypeAndDirection

export type TargetState =
    | GridCell

export type State = {
    sources: Array<SourceState>

    targets: Array<TargetState>
}

export type OtherBoard = {
    board: Array<TargetState>
}

export type Action =
    | { type: 'drop', sourceId: string, targetIx: number, targetIy: number}
    | { type: 'remove', sourceId: string, targetIx: number, targetIy: number }
    | { type: 'init', state:State}
    | { type: 'rotate', direction: Direction}

export type OtherAction = 
    | { type: 'init', otherBoard: OtherBoard}

export enum GameState {
    START = 'START', BATTLE = 'BATTLE', END = 'END'
}

export type Game = {
    gameId: number
    userA: number
    userB: number
    turn: number
    initialTurn: Date
    readyA: boolean
    readyB: boolean
    winner: string
    finishA: boolean
    finishB: boolean
    ruleId: number
    remainingShoot: number
    gameState: GameState
}

export type GridCellResponse = {
    gameId: number,
    userId: number,
    column: number,
    row: number,
    shipState: string,
    shipName: string
}

export enum ShotState {
    MISS = 'MISS', SHOT = 'SHOT'
}

export type ShotResult = {
    shotState: ShotState
    gridCell: GridCellResponse | null
}

export enum SendActions {
    READY = "READY", SHOT = "SHOT", WIN = "WIN"
}

export type GameChatMsg = {
    gameId: number,
    userId: number,
    action: SendActions
}

export type GameRuleAndInfo = {
    rule: Rule,
    shipTypes: Array<ShipType>
}

enum ServerGameState {
    START = 'START', BATTLE = 'BATTLE', END = 'END'
}