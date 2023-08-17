package online.partyrun.partyrunmatchingservice.domain.waiting.root;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.waiting.exception.InvalidDistanceException;

import java.util.Arrays;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum RunningDistance {
    M1000(1000),
    M3000(3000),
    M5000(5000),
    M10000(10000);

    int meter;

    public static RunningDistance getBy(int meter) {
        return Arrays.stream(RunningDistance.values())
                .filter(d -> d.meter == meter)
                .findAny()
                .orElseThrow(() -> new InvalidDistanceException(meter));
    }
}
