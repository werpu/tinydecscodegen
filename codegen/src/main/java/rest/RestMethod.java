package rest;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A generic rest method descriptor
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class RestMethod {

    private final String url;
    private final String name;
    private final RestType restType; /* GET POST etc...*/
    private final Optional<RestVar> returnValue;
    private final List<RestVar> params;
    private final String consumes;

    public RestMethod(String url, String name, RestType restType) {
        this.url = url;
        this.name = name;
        this.restType = restType;

        returnValue = Optional.empty();
        params = Collections.emptyList();
        consumes = null;
    }

    public RestMethod(String url, String name, RestType restType, Optional<RestVar> returnValue) {
        this.url = url;
        this.name = name;
        this.restType = restType;
        this.returnValue = returnValue;
        params = Collections.emptyList();
        consumes = null;
    }

    public RestMethod(String url, String name, RestType restType, Optional<RestVar> returnValue, List<RestVar> params) {
        this.url = url;
        this.name = name;
        this.restType = restType;
        this.returnValue = returnValue;
        this.params = params;
        consumes = null;
    }


}
