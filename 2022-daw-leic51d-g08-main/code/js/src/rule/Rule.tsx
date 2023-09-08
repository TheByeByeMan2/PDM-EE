
export type Rule = {
    ruleId :number,
    gridSize: string,
    shotNumber: number,
    timeout: number,
}

export type InputRule = {
    gridSize: string,
    shotNumber: number,
    timeout: number,
}

export type ShipType = {
    shipName: string,
    squares: number,
    fleetQuantity: number,
}

export type RequestAddRule = {
    rule: InputRule,
    shipTypes: Array<ShipType>   
}