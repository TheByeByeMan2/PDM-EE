import * as React from "react";
import { createElement, useState } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useCurrentLocalData } from "../localData/LocalData";
import { UserIdAndToken } from "../login/login";
import { URL_ALL_RULES } from "../requests";
import { InputRule, Rule, ShipType } from "./Rule";


export function AddRule() {
    const localData = useCurrentLocalData()
    const currentUser = localData.userIdAndToken
    const [ruleInputs, setRuleInputs] = useState({
        gridSize: '',
        shotNumber: '',
        timeout: '',
    })
    const [shipTypeInputs, setShipTypeInputs] = useState({
        shipName: '',
        squares: '',
        fleetQuantity: '',
    })
    const [removeShipTypeInput, setRemoveShipTypeInput] = useState({
        shipName: '',
    })
    const [isRuleSubmitting, setIsRuleSubmitting] = useState(false)
    const [redirect, setRedirect] = useState(false)
    const [isShipTypeSubmitting, setisShipTypeSubmitting] = useState(false)
    const [isShipTypeRemoving, setisShipTypeRemoving] = useState(false)
    const [error, setError] = useState(undefined)
    const [shipTypeList, setshipTypeList] = useState<Array<ShipType>>([])
    const location = useLocation()
    if (redirect) {
        return <Navigate to={location.state?.source?.pathname || "/waitingRoom"} replace={true} />
    }

    function handleRuleChange(ev: React.FormEvent<HTMLInputElement>) {
        const name = ev.currentTarget.name
        setRuleInputs({ ...ruleInputs, [name]: ev.currentTarget.value })
        setError(undefined)
    }

    function handleShipTypeChange(ev: React.FormEvent<HTMLInputElement>) {
        const name = ev.currentTarget.name
        setShipTypeInputs({ ...shipTypeInputs, [name]: ev.currentTarget.value })
        setError(undefined)
    }

    function handleRemoveShipTypeChange(ev: React.FormEvent<HTMLInputElement>) {
        const name = ev.currentTarget.name
        setRemoveShipTypeInput({ ...removeShipTypeInput, [name]: ev.currentTarget.value })
        setError(undefined)
    }

    function handleShipTypeRemove(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        setisShipTypeRemoving(true)
        const shipName = removeShipTypeInput.shipName
        console.log('remove shipName = ' + shipName)
        const newArr = removeShipList(shipTypeList, shipName)
        setshipTypeList(newArr)
        setisShipTypeRemoving(false)
    }

    function handleShipTypeSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        try {
            setisShipTypeSubmitting(true)
            const shipName = shipTypeInputs.shipName
            const squares = shipTypeInputs.squares
            const fleetQuantity = shipTypeInputs.fleetQuantity
            isNumber(squares)
            isNumber(fleetQuantity)
            console.log('shipName = ' + shipName + '; ' + 'squares = ' + squares + '; ' + 'fleetQuantity = ' + fleetQuantity)
            const newArr = addShipList(shipTypeList, {
                shipName: shipName,
                squares: Number(squares),
                fleetQuantity: Number(fleetQuantity),
            })

            setshipTypeList(newArr)

            setisShipTypeSubmitting(false)
        } catch (error) {
            setisShipTypeSubmitting(false)
            setError(error.message)
        }
    }

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        try {
            setIsRuleSubmitting(true)
            const gridSize = ruleInputs.gridSize
            const shotNumber = ruleInputs.shotNumber
            const timeout = ruleInputs.timeout
            checkGridSize(gridSize)
            isNumber(shotNumber)
            isNumber(timeout)
            arrNotEmpty(shipTypeList)
            console.log('gridSize = ' + gridSize + '; ' + 'shotNumber = ' + shotNumber + '; ' + 'timeout = ' + timeout)
            shipTypeList.map(ship => console.log('shipName = ' + ship.shipName + '; ' + 'squares = ' + ship.squares + '; ' + 'fleetQuantity = ' + ship.fleetQuantity))
            addRuleRequest({
                gridSize: gridSize,
                shotNumber: Number(shotNumber),
                timeout: Number(timeout)
            }, shipTypeList, currentUser).then(res => {
                setIsRuleSubmitting(false)
                if (res) {
                    const redirect = location.state?.source?.pathname || "/waitingRoom"
                    setRedirect(true)
                } else {
                    setError("Invalid create")
                }
            })

            setIsRuleSubmitting(false)
        } catch (error) {
            setIsRuleSubmitting(false)
            setError(error.message)
        }
    }

    return (
        <table>
            <tbody>
                <tr>
                    <td>
                        <form onSubmit={handleSubmit}>
                            <fieldset disabled={isRuleSubmitting}>
                                <div>
                                    <label htmlFor="gridSize">Grid Size</label>
                                    <input id="gridSize" type="text" name="gridSize" value={ruleInputs.gridSize} onChange={handleRuleChange} />
                                </div>
                                <div>
                                    <label htmlFor="shotNumber">Shot Number</label>
                                    <input id="shotNumber" type="text" name="shotNumber" value={ruleInputs.shotNumber} onChange={handleRuleChange} />
                                </div>
                                <div>
                                    <label htmlFor="timeout">Timeout</label>
                                    <input id="timeout" type="text" name="timeout" value={ruleInputs.timeout} onChange={handleRuleChange} />
                                </div>
                            </fieldset>
                            <div><button type="submit">create</button></div>
                        </form>
                    </td>
                    <td>
                        <form onSubmit={handleShipTypeSubmit}>
                            <fieldset disabled={isShipTypeSubmitting}>
                                <div>
                                    <label htmlFor="shipName">Ship Name</label>
                                    <input id="shipName" type="text" name="shipName" value={shipTypeInputs.shipName} onChange={handleShipTypeChange} />
                                </div>
                                <div>
                                    <label htmlFor="squares">squares</label>
                                    <input id="squares" type="text" name="squares" value={shipTypeInputs.squares} onChange={handleShipTypeChange} />
                                </div>
                                <div>
                                    <label htmlFor="fleetQuantity">Fleet Quantity</label>
                                    <input id="fleetQuantity" type="text" name="fleetQuantity" value={shipTypeInputs.fleetQuantity} onChange={handleShipTypeChange} />
                                </div>
                                <div>
                                    <button type="submit">create Ship</button>
                                </div>
                            </fieldset>
                        </form>
                    </td>
                    <td>
                        <div>{showShips(shipTypeList)}</div>
                    </td>
                    <td>
                        <form onSubmit={handleShipTypeRemove}>
                            <fieldset disabled={isShipTypeRemoving}>
                                <div>
                                    <label htmlFor="shipName">Ship Name</label>
                                    <input id="rmShipName" type="text" name="shipName" value={removeShipTypeInput.shipName} onChange={handleRemoveShipTypeChange} />
                                </div>
                                <div>
                                    <button type="submit">remove Ship</button>
                                </div>
                            </fieldset>
                        </form>
                    </td>
                </tr>
                <tr>
                    {error}
                </tr>
            </tbody>
        </table>
    )
}

const exempleShipTypes = [
    {
        shipName: "destroyer",
        squares: 2,
        fleetQuantity: 2
    },
    {
        shipName: "submarine",
        squares: 3,
        fleetQuantity: 2
    }
]

function removeShipList(list: Array<ShipType>, shipname: string): Array<ShipType> {

    return list.filter(ship => ship.shipName != shipname)
}

function addShipList(list: Array<ShipType>, ship: ShipType): Array<ShipType> {
    if (list.find(elem => elem.shipName === ship.shipName)) {
        throw new Error(`the ${ship.shipName} is already exits`)
    } else {
        const newArr = list.concat({
            shipName: ship.shipName,
            squares: ship.squares,
            fleetQuantity: ship.fleetQuantity,
        })
        return newArr
    }
}

function showShips(ships: Array<ShipType> | undefined) {

    if (ships === undefined) return (<div>...is loading</div>)
    return (
        createElement('table', {},
            createElement("tr", {},
                createElement("td", { width: "134", align: "center" }, "Ship name"),
                createElement("td", { width: "134", align: "center" }, "square"),
                createElement("td", { width: "134", align: "center" }, "fleet Quantity")
            ),
            ships.map(ship =>
                createElement("tr", {},
                    createElement("td", { width: "134", align: "center" }, ship.shipName),
                    createElement("td", { width: "134", align: "center" }, ship.squares),
                    createElement("td", { width: "134", align: "center" }, ship.fleetQuantity),
                )
            ))
    )
}

function isNumber(n: string) {
    if (Number(n)) { } else {
        throw new Error(`the ${n} not is a number`)
    }
}

function checkGridSize(g: string) {
    const s = g.split('x')
    if (Number(s[0]) && Number(s[1])) { } else {
        throw new Error(`the grid size must be (Number)(x)(number) and ${g} not is grid size`)
    }
}

function arrNotEmpty(arr: Array<ShipType>) {
    if (arr.length === 0) throw new Error('the list of ship can not be empty')
}

async function addRuleRequest(rule: InputRule, ships: Array<ShipType>, userIdAndToken: UserIdAndToken): Promise<number | undefined> {
    const json = {
        rule: rule,
        shipTypes: ships
    }
    const body = JSON.stringify(json)

    async function doFetch() {
        const resp = await fetch(URL_ALL_RULES, {
            method: 'POST',
            body: body,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': userIdAndToken.token,
                'userId': userIdAndToken.userId.toString()
            }
        })
        const bodyRes: number = await resp.json()
        if (bodyRes) {
            return bodyRes
        } else return undefined
    }

    return await doFetch()
}