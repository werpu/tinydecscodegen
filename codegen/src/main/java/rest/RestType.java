package rest;

public enum RestType {

    GET, POST, SAVE, PUT, DELETE;


    public boolean isGet() {
        return this == GET;
    }

    public boolean isPost() {
        return this == POST;
    }

    public boolean isSave() {
        return this == SAVE;
    }

    public boolean isPut() {
        return this == PUT;
    }

    public boolean isDelete() {
        return this == DELETE;
    }

}
