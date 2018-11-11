package net.werpu.tools.supportive.fs.common.errors;

public class ResourceClassNotFound extends RuntimeException {
    public ResourceClassNotFound() {
    }

    public ResourceClassNotFound(String message) {
        super(message);
    }

    public ResourceClassNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceClassNotFound(Throwable cause) {
        super(cause);
    }

    public ResourceClassNotFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
