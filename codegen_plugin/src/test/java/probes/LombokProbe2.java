package probes;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LombokProbe2 {

    private String probe1;

    private String probe2;

    private transient List<String> probe3;
}
