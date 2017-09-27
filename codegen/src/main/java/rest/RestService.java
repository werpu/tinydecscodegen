/*

Copyright 2017 Werner Punz

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software
is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package rest;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Root class describing a single rest service
 */
@Getter
@EqualsAndHashCode
public class RestService extends GenericClass {

    private final String serviceName;
    private final String serviceRootUrl;

    public RestService(String serviceName, List<RestMethod> methods) {
        super(new GenericType(serviceName, Collections.emptyList()), methods);
        this.serviceName = serviceName;
        this.serviceRootUrl = "";
    }

    public RestService(String serviceName, String serviceUrl, List<RestMethod> methods) {
        super(new GenericType(serviceName, Collections.emptyList()), methods);
        this.serviceName = serviceName;
        this.serviceRootUrl = serviceUrl;
    }


}
