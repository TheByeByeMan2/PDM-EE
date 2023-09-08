import * as React from "react";
import { useCallback, useEffect, useReducer, useState } from "react";
import { useCurrentLocalData } from "../localData/LocalData";
import { UserIdAndToken } from "../login/login";
import { getPutShip, getServerGame, putShips, setGameReady, useFetch } from "../requests";
import { ShipType } from "../rule/Rule";
import { printlnLog } from "../utils";
import { gridWrapperStyle, shipTypeWrapperStyle, sourceDivStyle, targetDivStyle } from "./cssStyle";
import { Action, Direction, GridCell, OtherBoard, Position, SendInfoStruct, ShipState, ShotResult, SourceState, State, TargetState, Game, GameState, SendActions, GameChatMsg, GameRuleAndInfo, OtherAction } from "./GameDomains";
import { gameSender } from "./GameSender";

const SIZE = 10

const ERASER = {
    shipName: 'Eraser',
    squares: 1,
    fleetQuantity: 1
}

function gameLog(gameState,msg) {printlnLog('GAME', gameState + ' -> ' + msg)}

function calculatePositions(headPos: Position, squares: number, direction: Direction): Array<Position> {
    const res: Array<Position> = []
    if (direction === Direction.HORIZONTAL) {
        for (let index = 0; index < squares; index++) {
            const pos = {
                col: headPos.col + index,
                row: headPos.row
            }
            res.push(pos)
        }
        return res
    } else {
        for (let index = 0; index < squares; index++) {
            const pos = {
                col: headPos.col,
                row: headPos.row + index
            }
            res.push(pos)
        }
        return res
    }
}

function validHeadPos(positions: Position): Position {
    const col = positions.col - 1 > 0 ? positions.col - 1 : positions.col
    const row = positions.row - 1 > 0 ? positions.row - 1 : positions.row
    return {
        col: col,
        row: row
    }
}

function validPosition(targets: Array<GridCell>, positions: Position, shipType: ShipType, direction: Direction, maxRow: number, maxCol: number) {

    const validHeadPosition = validHeadPos(positions)
    const betaCol = positions.col === validHeadPosition.col ? -1 : 0
    const betaRow = positions.row === validHeadPosition.row ? -1 : 0

    if (direction === Direction.HORIZONTAL) {
        if (positions.col + shipType.squares - 1 > maxCol) throw new Error('Invalid Position Out of Board')
        var deltaRow = 0
        while (deltaRow < 3 + betaRow) {
            var deltaCol = 0
            if (validHeadPosition.row + deltaRow > maxRow) break
            const row = validHeadPosition.row + deltaRow
            while (deltaCol < shipType.squares + 2 + betaCol) {
                if (validHeadPosition.col + deltaCol > maxCol) break
                const col = validHeadPosition.col + deltaCol
                const find = targets.find((cell) => cell.position.row === row && cell.position.col === col)
                if (find.ship !== undefined) throw new Error('Position Invalid')
                deltaCol += 1

            }
            deltaRow += 1
        }
    } else {
        if (positions.row + shipType.squares - 1 > maxRow) throw new Error('Invalid Position Out of Board')
        var deltaCol = 0
        while (deltaCol < 3 + betaRow) {
            var deltaRow = 0
            if (validHeadPosition.row + deltaCol > maxRow) break
            const row = validHeadPosition.row + deltaCol
            while (deltaRow < shipType.squares + 2 + betaCol) {
                if (validHeadPosition.col + deltaRow > maxCol) break
                const col = validHeadPosition.col + deltaRow
                const find = targets.find((cell) => cell.position.row === row && cell.position.col === col)
                if (find.ship !== undefined) throw new Error('Position Invalid')
                deltaRow += 1

            }
            deltaCol += 1
        }
    }
}

function putShip(targets: Array<GridCell>, positions: Array<Position>, shipType: ShipType): GridCell[] {
    const newArr = targets
    const ship = {
        shipType: shipType,
        shipState: ShipState.ALIVE,
        headPos: positions[0]
    }
    positions.forEach((pos) => {
        const value = newArr.findIndex((cell) => cell.position.col === pos.col && cell.position.row === pos.row && cell.ship === undefined)
        if (value >= 0) {
            newArr[value] = { position: pos, ship: ship }
        } else {
            throw new Error('Position Invalid')
        }
    })
    return newArr
}

function horizontalOrVerticalShip(targets: Array<GridCell>, headPos: Position): Direction {
    const nextColCell = targets.find((cell) => cell.position.col === headPos.col + 1 && cell.position.row === headPos.row)
    if (nextColCell.ship === undefined) return Direction.VERTICAL
    else return Direction.HORIZONTAL
}

function removeShip(targets: Array<GridCell>, pos: Position, shipType: ShipType): GridCell[] {
    const newArr = targets
    const shipIdx = newArr.findIndex((cell) => cell.position.col === pos.col && cell.position.row === pos.row && cell.ship !== undefined)
    const headPost = newArr[shipIdx].ship.headPos
    let count = 0
    const direction = horizontalOrVerticalShip(targets, headPost)
    if (direction === Direction.HORIZONTAL) {
        while (count < shipType.squares) {
            const idx = newArr.findIndex((cell) => cell.position.col === headPost.col + count && cell.position.row === headPost.row)
            newArr[idx].ship = undefined
            count++
        }
    } else {
        while (count < shipType.squares) {
            const idx = newArr.findIndex((cell) => cell.position.col === headPost.col && cell.position.row === headPost.row + count)
            newArr[idx].ship = undefined
            count++
        }
    }
    return newArr
}

function getGridCellByPosition(targets: Array<GridCell>, pos: Position): GridCell {
    return targets.find((cell) => cell.position.col === pos.col && cell.position.row === pos.row)
}

function rotateDirection(direction: Direction): Direction {
    if (direction === Direction.HORIZONTAL) return Direction.VERTICAL
    else return Direction.HORIZONTAL
}

function reduce(state: State, action: Action): State {
    let pos = undefined
    if (action.type !== 'init' && action.type !== 'rotate') {
        pos = {
            col: action.targetIx,
            row: action.targetIy
        }
    }
    switch (action.type) {
        case 'drop':
            const getSource = state.sources.find((v) => v.shipType.shipName === action.sourceId)
            const getShipType = getSource.shipType
            const decrementShipType = {
                shipName: getShipType.shipName,
                squares: getShipType.squares,
                fleetQuantity: getShipType.fleetQuantity - 1
            }
            validPosition(state.targets, pos, decrementShipType, getSource.direction, SIZE, SIZE)
            const newTarget = putShip(state.targets, calculatePositions(pos, getSource.shipType.squares, getSource.direction), decrementShipType)

            return {
                sources: state.sources.map((value) => value.shipType.shipName === action.sourceId ? { shipType: decrementShipType, direction: value.direction } : value),
                targets: newTarget
            }

        //fix increment when remove
        case 'remove':
            const cell = getGridCellByPosition(state.targets, pos)
            if (cell.ship === undefined) throw new Error('Not have ship to remove')
            //console.log(state.targets.filter((value) => value.ship.shipType.shipName === cell.ship.shipType.shipName).length)
            const incrementShipType = {
                shipName: cell.ship.shipType.shipName,
                squares: cell.ship.shipType.squares,
                fleetQuantity: cell.ship.shipType.fleetQuantity + 1
            }
            return {
                sources: state.sources.map((value) => value.shipType.shipName === cell.ship.shipType.shipName ? { shipType: incrementShipType, direction: value.direction } : value),
                targets: removeShip(state.targets, pos, cell.ship.shipType),
            }

        case 'init':
            gameLog('My Board Manager','INIT')
            return action.state

        case 'rotate':
            return {
                sources: state.sources.map((value) => value.shipType.shipName ? { shipType: value.shipType, direction: action.direction } : value),
                targets: state.targets
            }

        default:
            // unknown action, type system ensures this cannot happen
            return state
    }
}

function reduceOther(other: OtherBoard, action: OtherAction): OtherBoard {
    switch(action.type) {
        case 'init':
            return action.otherBoard
    }
    return other
}


function handleDragOver(event: React.DragEvent<HTMLDivElement>) {
    event.preventDefault()
    gameLog('HandleDragOver',"dragOver")
    event.dataTransfer.dropEffect = "copy";
}

function showTarget(target: TargetState) {
    if (target === undefined) return undefined
    else return `${target.position.col}-${target.position.row}`
}

function initializeGrid(colSize: number, rowSize: number): Array<GridCell> {
    const res: Array<GridCell> = []
    for (let col = 1; col <= colSize; col++) {
        for (let row = 1; row <= rowSize; row++) {
            const pos = {
                col: col,
                row: row
            }
            res.push({ position: pos, ship: undefined })
        }
    }
    return res
}

function getLocalShipTypes(): Array<ShipType> {
    const list: Array<ShipType> = []
    list.push({ shipName: 'Carrier', squares: 5, fleetQuantity: 1 })
    list.push({ shipName: 'Battleship', squares: 4, fleetQuantity: 2 })
    list.push({ shipName: 'Cruiser', squares: 3, fleetQuantity: 3 })
    list.push({ shipName: 'Submarine', squares: 2, fleetQuantity: 4 })
    return list
}

function localInitialState(): State {
    const list: Array<SourceState> = []
    const shipTypes = getLocalShipTypes()
    shipTypes.push(ERASER)
    shipTypes.forEach((shipType) => {
        list.push({ shipType: shipType, direction: Direction.HORIZONTAL })
    })
    const grid = initializeGrid(SIZE, SIZE)
    return {
        sources: list,
        targets: grid
    }
}

function emptyInitialState(): State {
    const list: Array<SourceState> = []
    const grid: Array<GridCell> = []
    return {
        sources: list,
        targets: grid
    }
}

function emtpyBoard(): OtherBoard{
    return {
        board: []
    }
}

function serverInitialState(ruleInfo: GameRuleAndInfo): State {
    const shipTypes = []
    ruleInfo.shipTypes.forEach((ship) => {
        shipTypes.push({ shipType: ship, direction: Direction.HORIZONTAL })
    })
    shipTypes.push({ shipType: ERASER, direction: Direction.HORIZONTAL })
    const split = ruleInfo.rule.gridSize.split('x')
    const maxCol = Number(split[0])
    const maxRow = Number(split[1])
    const grid = initializeGrid(maxCol, maxRow)
    return {
        sources: shipTypes,
        targets: grid
    }
}

function initialOtherBoard(ruleInfo: GameRuleAndInfo): OtherBoard {
    const split = ruleInfo.rule.gridSize.split('x')
    const maxCol = Number(split[0])
    const maxRow = Number(split[1])
    const grid = initializeGrid(maxCol, maxRow)
    return {
        board: grid
    }
}

function addPosition(arr: Array<SendInfoStruct>, obj: SendInfoStruct): Array<SendInfoStruct> {
    const newArr = arr
    newArr.push(obj)
    return newArr
}

function checkShipIsAllPut(ships: Array<SendInfoStruct>, shipTypes: Array<ShipType>) {
    shipTypes.forEach((shipType) => {
        const fleetQuantity = shipType.fleetQuantity
        const find = ships.filter((value) => value.shipName === shipType.shipName).length
        if (fleetQuantity !== find) throw new Error('insufficient ship number')
    })
}

async function shotGrid(userId: number, token: string, position: Position, setError: (value: any) => void, gameId: number): Promise<ShotResult | undefined> {
    try {
        const body = {
            userId: userId,
            position: {
                col: position.col,
                row: position.row
            }
        }
        const resp = await fetch(`api/games/${gameId}/battle`, {
            method: "GET",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token,
                'userId': userId.toString()
            },
            body: JSON.stringify(body)
        })
        if (resp.status != 200) {
            setError(`error, unexpected status ${resp.status}`)
        }
        const resBody: ShotResult = await resp.json()
        return JSON.parse(JSON.stringify(resBody, null, 2))
    } catch (err) {
        setError(err.message)
    }
}

export const getGameUrl = (gameId) => `api/games/${gameId}/info`

function numberToDirection(n: number): Direction {
    if (n > 1) throw new Error('Invalid direction number')
    if (n === 0) return Direction.HORIZONTAL
    else return Direction.VERTICAL
}

function directionToNumber(d: Direction) {
    if (d === Direction.HORIZONTAL) return 0
    else return 1
}

enum LocalGameState {
    PREPARING = 'PREPARING', INIT = 'INIT', BUILDING = 'BUILDING', WAITING = 'WAITING', INIT_BATTLE = 'INIT_BATTLE', BATTLE = 'BATTLE', WIN = 'WIN'
}

export function Game() {
    // add gameState to cmp 
    const [error, setError] = useState(undefined)
    try {
        const [localGameState, setLocalGameState] = useState(LocalGameState.PREPARING)
        const [getRuleInfo, setGetRuleInfo] = useState(undefined)
        const localData = useCurrentLocalData()
        const [state, dispatch] = useReducer(reduce, emptyInitialState())

        const [currentDropShipName, setCurrentDropShipName] = useState(undefined)
        const [currentDirection, setCurrentDirection] = useState(Direction.HORIZONTAL)

        const [otherBoard, otherDispatch] = useReducer(reduceOther, emtpyBoard())
        const [selectShip, setSelectShip] = useState<Array<SendInfoStruct>>([])
        const [serverGame, setServerGame] = useState<Game>(undefined)
        const [lastChatMsg, setLastChatMsg] = useState(undefined)

        const [startGameManager, setStartGameManager] = useState(false)
        //const [startWaiting, setStartWaiting] = useState(false)
        //const [startManagerMsg, setStartManagerMsg] = useState(false)

        gameLog('Global',state)

        useEffect(() => {
            gameLog('Get Event Source',`creating new EventSource`)
                const eventSource = new EventSource(`api/games/${localData.game.gameId}/chat`)
                eventSource.onmessage = (ev) => {
                    setLastChatMsg(ev.data)
                }
                return () => {
                    eventSource.close()
                }
        }, [])

        useEffect(() => {
            gameLog('Chat Manager','Start Manager chat mensage')
            if (lastChatMsg !== undefined) {
                gameLog('Chat Manager','get player msg')
                const obj = JSON.parse(lastChatMsg)
                const msg:GameChatMsg = JSON.parse(obj.msg)
                gameLog('Chat Manager','parsed obj: '+JSON.stringify(obj))
                gameLog('Chat Manager','my info: '+JSON.stringify(localData))
                if (msg.userId !== localData.userIdAndToken.userId && localData.game.gameId === msg.gameId) {
                    gameLog('Chat Manager','catch other player msg: '+JSON.stringify(obj))
                    if (msg.action === SendActions.READY){
                        getServerGame(localData.userIdAndToken, localData.game.gameId).then((game) => {
                            if (game.readyA && game.readyB) {
                                setLocalGameState(LocalGameState.INIT_BATTLE)
                            }
                    })
                    }
                } else {
                    gameLog('Msg Manager','catch msg faild')
                }
            } else [
                gameLog('Chat manager', 'last chat msg is undefined')
            ]
        }, [lastChatMsg])

        const handleDrop = useCallback(function handleDrop(event: React.DragEvent<HTMLDivElement>) {
            setError(undefined)
            try {
                const directionNumber = parseInt(event.currentTarget.attributes.getNamedItem('data-direction').textContent)
                const direction = numberToDirection(directionNumber)
                const sourceIx = event.dataTransfer.getData("text/plain")
                const targetIx = parseInt(event.currentTarget.attributes.getNamedItem('data-ix').textContent)
                const targetIy = parseInt(event.currentTarget.attributes.getNamedItem('data-iy').textContent)
                const drop: Action = {
                    'type': 'drop',
                    sourceId: sourceIx,
                    targetIx: targetIx,
                    targetIy: targetIy,
                }
                const remove: Action = {
                    'type': 'remove',
                    sourceId: sourceIx,
                    targetIx: targetIx,
                    targetIy: targetIy
                }
                const dispatchValue: Action = sourceIx === ERASER.shipName ? remove : drop
                setSelectShip(addPosition(selectShip, { shipName: sourceIx, position: { col: targetIx, row: targetIy }, direction: direction }))
                dispatch(dispatchValue)
            } catch (err) {
                setError(err.message)
            }
        }, [dispatch])

        async function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
            // send put ship to server
            // send set ready
            // send is ready signal
            ev.preventDefault()
            setError(undefined)
            try {
                gameLog('Hand Submit', selectShip)
                checkShipIsAllPut(selectShip, getRuleInfo.shipTypes)
                //setStartWaiting(true)
                await putShips(localData.userIdAndToken, localData.game.gameId, selectShip)
                gameLog('Hand submit', 'next put Ship')
                const v =await getPutShip(localData.userIdAndToken, localData.game.gameId)
                console.log(v)
                setGameReady(localData.userIdAndToken, localData.game.gameId).then((game)=>{
                    if (game.readyA && game.readyB) {
                        setLocalGameState(LocalGameState.INIT_BATTLE)
                    } else {
                        setLocalGameState(LocalGameState.WAITING)
                    }
                })
                const msg:GameChatMsg = {
                    gameId: localData.game.gameId,
                    userId: localData.userIdAndToken.userId,
                    action: SendActions.READY
                }
                gameSender(localData.game.gameId, JSON.stringify(msg))
                setStartGameManager(true)
            } catch (error) {
                setError(error.message)
            }
        }

        function handleRotate(ev: React.FormEvent<HTMLFormElement>) {
            ev.preventDefault()
            setError(undefined)
            try {
                const previoRotate = rotateDirection(currentDirection)
                const rtt: Action = {
                    'type': 'rotate',
                    direction: previoRotate
                }
                dispatch(rtt)
                setCurrentDirection(previoRotate)
            } catch (error) {
                setError(error.message)
            }
        }

        function handleDragStart(event: React.DragEvent<HTMLDivElement>) {
            setError(undefined)
            try {
                const shipName = event.currentTarget.attributes.getNamedItem('data-shipname').textContent
                setCurrentDropShipName(shipName)
                console.log(`dragStart - ${shipName}`)
                event.dataTransfer.effectAllowed = "all"
                event.dataTransfer.setData("text/plain", shipName)
            } catch (error) {
                setError(error.message)
            }
        }

        const selecteShipType = () => {
            return (
                <><div style={shipTypeWrapperStyle}>
                    {state.sources.map((source, ix) => <div
                        key={source.shipType.shipName}
                        style={sourceDivStyle(source, 1, ix + 1)}
                        draggable={source.shipType.fleetQuantity !== 0}
                        onDragStart={handleDragStart}
                        data-shipname={source.shipType.shipName}>
                        {source.shipType.shipName + '\n' + source.shipType.fleetQuantity}
                    </div>)}
                </div><div>
                        <form onSubmit={handleRotate}>
                            <div>
                                <button type="submit">rotate</button>
                            </div>
                        </form>
                    </div></>
            )
        }

        const inigmaBoard = () => {
            const handleClick = (e) => {
                // todo('get from server and use dispatcherOther for effect the board')
                const targetIx = parseInt(e.currentTarget.attributes.getNamedItem('data-ix').textContent)
                const targetIy = parseInt(e.currentTarget.attributes.getNamedItem('data-iy').textContent)
                console.log('Clicked on => ' + targetIx + '-' + targetIy)
            }
            return (
                <>
                    <div style={gridWrapperStyle}>
                        {otherBoard.board.map((cell, ix) => <div key={cell.position.col + '-' + cell.position.row} onClick={handleClick}
                            style={targetDivStyle(cell, cell.position.col, cell.position.row)}
                            data-ix={cell.position.col}
                            data-iy={cell.position.row}>
                            {
                                showTarget(cell)
                            }
                        </div>)}
                    </div></>
            )
        }

        if (localGameState === LocalGameState.PREPARING) {
            if (localData.game !== undefined) {
                const resp = fetch(`api/rules/${localData.game.ruleId}`, {
                    method: "GET",
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': localData.userIdAndToken.token,
                        'userId': localData.userIdAndToken.userId.toString()
                    },
                })
                resp.then((value) => {
                    value.json().then((body) => {
                        setGetRuleInfo(body)
                    })
                })
            } else throw new Error('In user local game data not have rule Id')
            if (getRuleInfo !== undefined) {
                setLocalGameState(LocalGameState.INIT)
            }
            return (
                <div>...Loading</div>
            )
        }

        if (localGameState === LocalGameState.INIT) {

            const initialize: Action = {
                'type': 'init',
                state : serverInitialState(getRuleInfo)
            }
            dispatch(initialize)

            if (state !== undefined) setLocalGameState(LocalGameState.BUILDING)
            return(<div>INITIALIZE BOARD</div>)
        }

        if (localGameState === LocalGameState.BUILDING) {
            gameLog('BUILDING', 'entry')
            return (
                <table border={1}>
                    <tbody>
                        <tr>
                            <td align="center"><form onSubmit={handleSubmit}>
                                <div>
                                    <button type="submit">ready</button>
                                </div>
                            </form>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div style={gridWrapperStyle}>
                                    {state.targets.map((cell, ix) => <div key={cell.position.col + '-' + cell.position.row}
                                        style={targetDivStyle(cell, cell.position.col, cell.position.row)}
                                        onDragOver={cell.ship === undefined || currentDropShipName === ERASER.shipName ? handleDragOver : undefined}
                                        onDrop={cell.ship === undefined || currentDropShipName === ERASER.shipName ? handleDrop : undefined}
                                        data-ix={cell.position.col}
                                        data-iy={cell.position.row}
                                        data-direction={directionToNumber(currentDirection)}>
                                        {
                                            showTarget(cell)
                                        }
                                    </div>)}
                                </div>
                            </td>
                            <td>
                                {
                                    selecteShipType()
                                }
                            </td>
                        </tr>
                        <tr>
                            <td>
                                {error}
                            </td>
                        </tr>
                    </tbody>
                </table>
            )
        }

        if (localGameState === LocalGameState.WAITING) {
            gameLog('WAITING', 'start')
            //manager chat msg
            try {
                return (<div>IS SEARCHING</div>)
            } catch (err) {
                setError(err.message)
            }
        }

        if (localGameState === LocalGameState.INIT_BATTLE){
            const initialize: OtherAction = {
                'type': 'init',
                otherBoard: initialOtherBoard(getRuleInfo)
            }
            otherDispatch(initialize)
            if (otherBoard !== undefined) setLocalGameState(LocalGameState.BATTLE)
            return(<div>BIULDING OTHER BOARD</div>)
        }

        if (localGameState === LocalGameState.BATTLE){
            return (
                <table border={1}>
                    <tbody>
                        <tr>
                            <td align="center"><form onSubmit={handleSubmit}>
                                <div>
                                    <button type="submit">ready</button>
                                </div>
                            </form>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div style={gridWrapperStyle}>
                                    {state.targets.map((cell, ix) => <div key={cell.position.col + '-' + cell.position.row}
                                        style={targetDivStyle(cell, cell.position.col, cell.position.row)}
                                        onDragOver={cell.ship === undefined || currentDropShipName === ERASER.shipName ? handleDragOver : undefined}
                                        onDrop={cell.ship === undefined || currentDropShipName === ERASER.shipName ? handleDrop : undefined}
                                        data-ix={cell.position.col}
                                        data-iy={cell.position.row}
                                        data-direction={directionToNumber(currentDirection)}
                                    >
                                        {
                                            showTarget(cell)
                                        }
                                    </div>)}
                                </div>
                            </td>
                            <td>
                                {
                                    inigmaBoard()
                                }
                            </td>
                        </tr>
                        <tr>
                            <td>
                                {error}
                            </td>
                        </tr>
                    </tbody>
                </table>
            )
        }

    } catch (error) {
        setError(error.message)
    }
}