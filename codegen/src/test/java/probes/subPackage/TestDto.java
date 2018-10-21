package probes.subPackage;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import probes.ProbeRetVal;

import java.util.List;

@EqualsAndHashCode
@AllArgsConstructor
public class TestDto<T, K extends List<String>> {

    public final String booga;

    @Getter
    public final String booga2;

    @Getter
    private final ProbeRetVal retVal;


}
