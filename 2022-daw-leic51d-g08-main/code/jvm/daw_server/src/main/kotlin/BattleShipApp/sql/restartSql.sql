begin transaction;

drop table if exists grids;
drop table if exists gameGridFleet;
drop table if exists shipTypes;
drop table if exists games;
drop table if exists waitingRoom;
drop table if exists user_data;
drop table if exists tokens;
drop table if exists users;
drop table if exists rules;

SET search_path TO public;
DROP EXTENSION IF EXISTS "uuid-ossp";

CREATE EXTENSION "uuid-ossp" SCHEMA public;

/*CREATE EXTENSION IF NOT EXISTS "uuid-ossp";*/

create table users
(
    id          serial primary key,
    name        varchar(15) not null unique,
    passVer     varchar(35) not null,
    userState   varchar(15) not null,
    constraint userState_check check (
            userState in ('FREE', 'WAITING', 'BATTLE')
        )
);

create table tokens
(
    userId      int primary key,
    tokenVer    uuid DEFAULT public.uuid_generate_v4() unique,
    constraint fk_userId
        foreign key (userId)
        references users (id)
);

create table rules
(
    ruleId serial primary key,
    grid_size varchar(5) not null,
    Number_of_shots int not null,
    player_timeout int not null
);


create table waitingRoom
(
    userId          serial primary key,
    username        varchar(15) not null unique,
    gameId          int,
    isGo            boolean not null,
    time            timestamp not null,
    ruleId          int not null,
    constraint fk_userId
        foreign key (userId)
            references users (id),
    constraint fk_username
        foreign key (username)
            references users (name),
    constraint fk_ruleId
        foreign key (ruleId)
            references rules (ruleId)
);

create table user_data
(
    userId      int primary key,
    score       int not null ,
    gamesPlayed int not null ,
    constraint fk_userIdRanking
        foreign key (userId)
            references users (id),
    constraint scoreRule check (
            score >= 0
            )
);

create table games
(
    gameId      serial primary key,
    userA       int not null,
    userB       int not null,
    turn        int not null,
    initialTurn timestamp,
    readyA      boolean not null,
    readyB      boolean not null,
    winner      varchar(20),
    finishA     boolean not null,
    finishB     boolean not null,
    ruleId      int not null,
    remainingShot   int not null,
    gameState   varchar(20) not null,
    constraint fk_userA
        foreign key (userA)
            references users (id),
    constraint fk_userB
        foreign key (userB)
            references users (id),
    constraint fk_ruleId
        foreign key (ruleId)
            references rules (ruleid),
    constraint gameState_check check (
            gameState in ('START', 'BATTLE', 'END')
        )

);

create table shipTypes
(
    ruleId      int not null,
    shipName     varchar(15),
    squares int not null,
    fleetQuantity int not null,
    constraint squareCheck check (
            squares > 0
        ),
    constraint quantityCheck check (
            fleetQuantity >= 0
        ),
    constraint fk_ruleId
        foreign key (ruleId)
            references rules (ruleId),
    primary key (ruleId, shipName)
);


create table gameGridFleet
(
    userId      int not null,
    shipName    varchar(15) not null,
    remainingQuantity int not null,
    constraint fk_userId
        foreign key (userId)
            references users (id),
    constraint remainingQuantityCheck check (
            remainingQuantity >= 0
        ),
    primary key (userId, shipName)
);

create table grids
(
    gameId int not null,
    userId int not null,
    col int not null,
    row int not null,
    shipState varchar (10),
    shipName     varchar(15),
    constraint gridId_games
        foreign key (gameId)
            references games (gameId),
    constraint user_users
        foreign key (userId)
            references users (id),
    constraint ship_state_check check (
            shipState in ('ALIVE', 'SHOT', 'SINK')
        ),
    primary key(gameId, userId, col, row)
);

commit;
end transaction;

begin transaction;

--insert into rules(ruleId, grid_size, Number_of_shots, player_timeout) values (1,'10x10', 1, 120000);
insert into rules(ruleId, grid_size, Number_of_shots, player_timeout) values (2,'10x10', 1, 120000);


/*insert into shipTypes values (1, 'carrier', 5, 1);
insert into shipTypes values (1, 'battleship', 4, 2);
insert into shipTypes values (1, 'cruiser', 3, 2);
insert into shipTypes values (1, 'submarine', 3, 2);
insert into shipTypes values (1, 'destroyer', 2, 2);*/

insert into shiptypes values (2, 'submarine', 2, 1);

commit;
end transaction;