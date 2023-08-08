package online.partyrun.partyrunmatchingservice.domain.battle.service.external.exception;

import online.partyrun.partyrunmatchingservice.global.exception.InternalServerException;

public class BattleAlreadyExistException extends InternalServerException {
    public BattleAlreadyExistException() {
        super("Battle already exists");
    }
}