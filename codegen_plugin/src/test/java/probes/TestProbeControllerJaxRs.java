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

package probes;


import javax.ws.rs.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Werner Punz on 15.02.2016.
 */
@Path("rest/testprobe1")
public class TestProbeControllerJaxRs {

    /**
     * my comment
     *
     * @param appUuid
     * @param right
     * @param filter
     * @return
     */
    @GET()
    @Path("/approval/getit/resource/{app}/{right}/{filter}")
    public ReturnValue<ProbeRetVal> probeGet(@PathParam("app") String appUuid, @PathParam("right") String right, @PathParam("filter") String filter) {
        return new ReturnValue<ProbeRetVal>(new ProbeRetVal());
    }

    @GET()
    @Path("/approval/getit2/scope/{app}/{right}/{filter}")
    public ReturnValue<List<ProbeRetVal>> probeGet2(@PathParam("app") String appUuid,
                                                    @PathParam("right") String rightUuid,
                                                    @PathParam("filter") String filter) {
        return new ReturnValue<>(Collections.emptyList());
    }


    @POST()
    @Path("/approval/postit/scope/{app}/{right}/{filter}")
    public ReturnValue<List<ProbeRetVal>> probePost(@QueryParam("app") String appUuid,
                                                    @QueryParam("right") String rightUuid,
                                                    String filter) {
        return new ReturnValue<>(Collections.emptyList());
    }

    @POST()
    @Path("/approval/postit/scope/{app}/{right}/{filter}")
    public ReturnValue<Map<String, ProbeRetVal>> probePost2(@QueryParam("app") String appUuid,
                                                            @QueryParam("right") String rightUuid,
                                                            String filter) {
        return new ReturnValue<>(Collections.emptyMap());
    }

    @POST()
    @Path("/approval/postit/scope/{app}/{right}/{filter}")
    public ReturnValue<Map<String, Map<String, Integer>>> probePost3(@QueryParam("app") String appUuid,
                                                                     @QueryParam("right") String rightUuid,
                                                                     String filter) {
        return new ReturnValue<>(Collections.emptyMap());
    }
}
