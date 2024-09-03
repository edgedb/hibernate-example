module default {
    type Position {
        required x: int32;
        required y: int32;
    }

    type Carrot {
        position: Position;
    }

    type Figure {
        required name: str;
        position: Position;
        required score: int32;
        required stat_moves: int32;
    }

    type Rabbit {
        required figure: Figure;

        required nutrition: int32;
    }

    type Fox {
        required figure: Figure;
    }
}
