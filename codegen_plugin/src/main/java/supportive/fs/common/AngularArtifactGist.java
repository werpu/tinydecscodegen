package supportive.fs.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@RequiredArgsConstructor
public class AngularArtifactGist implements Serializable {
    private final String artifactName;
    private final String tagName;
    private final String className;
    private final String filePath;
}
