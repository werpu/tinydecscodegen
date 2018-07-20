package probes;


import lombok.Data;

import java.util.List;

@Data
public class LombokProbe1 {

    private final String probe1;

    private String probe2;

    private transient List<String> probe3;

}
