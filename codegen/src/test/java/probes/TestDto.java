package probes;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@AllArgsConstructor
public class TestDto {

    public final String booga;

    @Getter
    public final String booga2;

    @Getter
    private final ProbeRetVal retVal;


}
