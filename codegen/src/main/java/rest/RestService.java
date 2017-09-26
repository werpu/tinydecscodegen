package rest;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

/**
 * Root class describing a single rest service
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class RestService {

    private final String serviceName;
    private final String serviceRootUrl;
    private final List<RestMethod> methods;


    public RestService(String serviceName, List<RestMethod> methods) {
        this.serviceName = serviceName;
        this.methods = methods;
        this.serviceRootUrl = "";
    }


}
