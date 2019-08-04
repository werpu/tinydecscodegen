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

    /**
     * my comment
     * @param appUuid
     * @param right
     * @param filter
     * @return
     */
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
