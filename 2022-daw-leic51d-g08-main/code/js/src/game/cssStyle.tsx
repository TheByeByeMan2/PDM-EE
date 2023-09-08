import { Direction, ShipState, SourceState, TargetState } from "./GameDomains"

export const wrapperStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: "repeat(1)",
    gap: "10px",
    gridAutoRows: "minmax(100px, auto)",
}

export const shipTypeWrapperStyle: React.CSSProperties = {
    display: 'grid',
    gridGap: '10px',
}

export const gridWrapperStyle: React.CSSProperties = {
    display: 'grid',
    gridAutoFlow: 'dense',
}

export function sourceDivStyle(sourceState: SourceState, column: number, row: number): React.CSSProperties {
    let width = 50
    let height = 50
    let c = column
    let r = row
    if (sourceState.direction === Direction.HORIZONTAL) {
        width = width * sourceState.shipType.squares
    } else {
        height = height * sourceState.shipType.squares
        c = row
        r = column
    }

    return {
        gridColumn: c,
        gridRow: r,
        border: "solid",
        width: width.toString() + "px",
        height: height.toString() + "px",
        borderColor: sourceState.shipType.fleetQuantity !== 0 ? 'green' : 'red',
    }
}


export function gridCellColor(targetState: TargetState) {
    if (targetState.ship === undefined) return '#00FFFF'
    else {
        switch (targetState.ship.shipState) {
            case ShipState.ALIVE:
                return '#1E90FF'
            case ShipState.SHOT:
                return '#DC143C'
            case ShipState.SUNK:
                return '#696969'
            default:
                return undefined
        }
    }
}

export function targetDivStyle(targetState: TargetState, column: number, row: number): React.CSSProperties {
    const color = gridCellColor(targetState)
    return {
        gridColumn: column,
        gridRow: row,
        border: "solid",
        width: "50px",
        height: "50px",
        backgroundColor: color,
        borderColor: 'yellow'
    }
}