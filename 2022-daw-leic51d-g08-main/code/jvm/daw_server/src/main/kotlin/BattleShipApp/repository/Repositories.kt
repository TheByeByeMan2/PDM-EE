package BattleShipApp.repository

import org.springframework.stereotype.Component


@Component
class Repositories(
    val usersRep: UsersRepository = UsersRepository(),
    val gamesRep: GamesRepository = GamesRepository(),
    val gridRep: GridsRepository = GridsRepository(),
    val waitingRoomRep: WaitingRoomRepository = WaitingRoomRepository(),
    val rulesRep: RulesRepository = RulesRepository(),
    val shipTypeRep: ShipTypeRepository = ShipTypeRepository(),
    val gameGridFleetRep: GameGridFleetRepository = GameGridFleetRepository(),
    val tokensRepository: TokensRepository = TokensRepository(),
) {
}