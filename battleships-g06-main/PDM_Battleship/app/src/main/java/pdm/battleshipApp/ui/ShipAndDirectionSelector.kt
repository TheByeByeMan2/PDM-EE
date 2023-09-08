package pdm.battleshipApp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pdm.battleshipApp.battleShip.model.coords.Direction
import pdm.battleshipApp.battleShip.model.ship.ShipType
import pdm.battleshipApp.ui.screens.CELL_DIM

val DIRECTION_BORDER_COLOR = Color.Blue
val SHIP_TYPE_BORDER_COLOR = Color.Blue
val SHIP_BOX_COLOR = Color.Green

@Composable
fun DirectionSelector(onClickSelectDirection: (Direction) ->Unit, currentDirection: Direction){
    Column(modifier = Modifier.border(border = BorderStroke(2.dp, DIRECTION_BORDER_COLOR))) {
        Direction.values().forEach {
            Row(verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = it.name == currentDirection.name,
                    onClick = {
                        onClickSelectDirection(it)
                    }
                )
                Text(text = it.name)
            }
        }
    }
}

@Composable
fun ShipSelector(onClickSelectShipType: (ShipType)->Unit, currentShipType: ShipType, remainShip: Map<ShipType, Int>){
    Column(modifier = Modifier.border(border = BorderStroke(2.dp, SHIP_TYPE_BORDER_COLOR))) {
        ShipType.values.forEach {
            val remain = remainShip[it]
            val count = it.fleetQuantity - remain!!
            onClickSelectShipType(nextShipType(remainShip, currentShipType))
            Row(verticalAlignment = Alignment.CenterVertically,) {
                RadioButton(
                    selected = it.name == currentShipType.name,
                    onClick = {
                        onClickSelectShipType(it)
                    }, enabled = count != it.fleetQuantity
                )
                Text("${count} x ${it.fleetQuantity}  ")
                repeat(it.squares){
                    Box(modifier = Modifier
                        .size(CELL_DIM)
                        .background(SHIP_BOX_COLOR))
                }
            }
        }
    }
}

private fun nextShipType(remainShip: Map<ShipType, Int>, currentShip: ShipType): ShipType{
    return if (remainShip[currentShip] == 0) {
        val idx = ShipType.values.indexOf(currentShip) + 1
        if (idx >= ShipType.values.size){
            ShipType.values[idx-1]
        } else {
            ShipType.values[idx]
        }
    }else currentShip
}
