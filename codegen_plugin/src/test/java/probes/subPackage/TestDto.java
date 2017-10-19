package probes.subPackage;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import probes.ProbeRetVal;

@EqualsAndHashCode
@AllArgsConstructor
public class TestDto {

    public final String booga;

    @Getter
    public final String booga2;

    @Getter
    private final ProbeRetVal retVal;


}
