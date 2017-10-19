package probes;

import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Werner Punz on 15.02.2016.
 */
@RestController
@RequestMapping("rest/testprobe1")
public class TestProbeController {


    @RequestMapping(value = "/approval/getit/resource/{app}/{right}/{filter}", method = {RequestMethod.GET})
    public ReturnValue<ProbeRetVal> probeGet(@PathVariable("app") String appUuid, @PathVariable("right") String right, @PathVariable("filter") String filter) {
        return new ReturnValue<ProbeRetVal>(new ProbeRetVal());
    }

    @RequestMapping(value = "/approval/getit2/scope/{app}/{right}/{filter}", method = {RequestMethod.GET})
    public ReturnValue<List<ProbeRetVal>> probeGet2(@PathVariable("app") String appUuid,
                                                    @PathVariable("right") String rightUuid,
                                                    @PathVariable("filter") String filter) {
        return new ReturnValue<>(Collections.emptyList());
    }


    @RequestMapping(value = "/approval/postit/scope/{app}/{right}/{filter}", method = {RequestMethod.POST})
    public ReturnValue<List<ProbeRetVal>> probePost(@RequestParam("app") String appUuid,
                                                    @RequestParam("right") String rightUuid,
                                                    @RequestBody() String filter) {
        return new ReturnValue<>(Collections.emptyList());
    }

    @RequestMapping(value = "/approval/postit/scope/{app}/{right}/{filter}", method = {RequestMethod.POST})
    public ReturnValue<Map<String, ProbeRetVal>> probePost2(@RequestParam("app") String appUuid,
                                                           @RequestParam("right") String rightUuid,
                                                           @RequestBody() String filter) {
        return new ReturnValue<>(Collections.emptyMap());
    }

    @RequestMapping(value = "/approval/postit/scope/{app}/{right}/{filter}", method = {RequestMethod.POST})
    public ReturnValue<Map<String, Map<String, Integer>>> probePost3(@RequestParam("app") String appUuid,
                                                            @RequestParam("right") String rightUuid,
                                                            @RequestBody() String filter) {
        return new ReturnValue<>(Collections.emptyMap());
    }
}
