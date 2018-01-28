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
