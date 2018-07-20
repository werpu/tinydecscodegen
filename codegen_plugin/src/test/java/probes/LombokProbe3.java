package probes;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


public class LombokProbe3 {

    @Setter
    @Getter
    private String probe1;


    private String probe2;

    @Setter
    private transient List<String> probe3;
}
