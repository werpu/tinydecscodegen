package configuration;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class TinyDecsConfiguration implements Serializable {
    private int returnValueStripLevel;
    private String sourceRestFramework;
    private String targetClientFramework;

    public TinyDecsConfiguration() {
    }
}
