/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */
package rest;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A generic rest method descriptor for
 * our parsing model
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class RestMethod extends GenericMethod {

    private final String url;
    private final String name;
    private final RestType restType; /* GET POST etc...*/
    private final String consumes;
    private final String comment;

    public RestMethod(String url, String name, RestType restType) {
        super(Optional.empty(), Collections.emptyList());
        this.url = url;
        this.name = name;
        this.restType = restType;
        consumes = null;
        comment = "";
    }

    public RestMethod(String url, String name, RestType restType, Optional<RestVar> returnValue) {
        super(returnValue, Collections.emptyList());
        this.url = url;
        this.name = name;
        this.restType = restType;
        consumes = null;
        comment = "";
    }

    public RestMethod(String url, String name, RestType restType, Optional<RestVar> returnValue, List<RestVar> params) {
        super(returnValue, params);
        this.url = url;
        this.name = name;
        this.restType = restType;
        consumes = null;
        comment = "";
    }

    public RestMethod(String url, String name, RestType restType, Optional<RestVar> returnValue, List<RestVar> params, String comment) {
        super(returnValue, params);
        this.url = url;
        this.name = name;
        this.restType = restType;
        this.consumes = null;
        this.comment = comment;
    }
}
