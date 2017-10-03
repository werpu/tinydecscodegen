package utils;

public enum ArtifactType {
    SERVICE, DTO;

    public boolean isService() {
        return this == SERVICE;
    }

    public boolean isDto() {
        return this == DTO;
    }
}
