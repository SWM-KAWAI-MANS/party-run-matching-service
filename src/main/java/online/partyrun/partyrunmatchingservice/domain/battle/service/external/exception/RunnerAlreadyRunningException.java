package online.partyrun.partyrunmatchingservice.domain.battle.service.external.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class RunnerAlreadyRunningException extends BadRequestException {
    public RunnerAlreadyRunningException() {
        super("runner already running");
    }
}