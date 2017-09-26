package rest;

public enum RestVarType {

    PathVariable, RequesParam, RequestBody, RequestRetval;

    public boolean isPathVariable() {
        return this == PathVariable;
    }

    public boolean isRequestParam() {
        return this == RequesParam;
    }

    public boolean isRequestBody() {
        return this == RequestBody;
    }

    public boolean isRequestRetVal() {
        return this == RequestRetval;
    }
}
