package online.partyrun.partyrunmatchingservice.domain.matching.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class BattleAlreadyExistException extends BadRequestException {
    public BattleAlreadyExistException() {
        super("Battle already exists");
    }
}
