package online.partyrun.partyrunmatchingservice.domain.waiting.exception;

public class DuplicateMemberException extends IllegalArgumentException {
    public DuplicateMemberException(String memberId) {
        super(memberId + "was duplicated");
    }
}
