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
package reflector;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import rest.*;
import org.springframework.web.bind.annotation.*;
import reflector.utils.ReflectUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Reflection class to handle spring based rest services
 */
public class SpringRestReflector {

    public static List<RestService> reflect(List<Class> toReflect, boolean flattenResult) {
        return toReflect.parallelStream().filter(cls -> {
            return isRestService(cls);
        }).map(cls -> {
            String serviceUrl = fetchServiceUrl(cls);
            List<RestMethod> restMethods = fetchRestMethods(cls, flattenResult);
            return new RestService(fetchServiceName(cls), serviceUrl, restMethods);
        }).collect(Collectors.toList());
    }


    private static List<RestMethod> fetchRestMethods(Class cls, boolean flattenResult) {

        Collection<Method> methods = ReflectUtils.getAllMethods(cls, false, false);
        return methods.stream()
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .map(method -> {
                    return mapMethod(method, flattenResult);
                })
                .flatMap(rMethod -> rMethod)
                .sorted((o1, o2) -> {return o1.getName().compareTo(o2.getName());})
                .collect(Collectors.toList());

    }

    private static Stream<RestMethod> mapMethod(Method method, boolean flattenResult) {
        RequestMapping mapping = method.getAnnotation(RequestMapping.class);
        String name = method.getName();
        String[] value = mapping.value();
        RequestMethod[] reqMethods = mapping.method(); //if there is more than one request method we map it over to additional typescript methods
        String[] consumes = mapping.consumes();
        //we can savely ignore produces since we always return a json object in our case

        List<RestVar> restVars = getRestVars(method).collect(Collectors.toList());

        String url = cutOffParams(value[0]);

        List<RestMethod> restMethods = Lists.newArrayList();


        boolean array = ReflectUtils.isArrayType(method.getReturnType());

        Optional<RestVar> returnValue = ReflectUtils.getGenericRetVal(method);


        for (RequestMethod requestMethod : reqMethods) {

            try {
                RestType restType = Enum.valueOf(RestType.class, requestMethod.name());
                RestMethod restMethodDcl = new RestMethod(url,
                        (reqMethods.length == 1) ? name : name + "_" + requestMethod.name(),
                        restType, returnValue, restVars);
                restMethods.add(restMethodDcl);
            } catch (IllegalArgumentException ex) {
                System.out.println(ex);
            }

        }
        return restMethods.stream();
    }


    private static String cutOffParams(String in) {
        return in.replaceAll("/\\s*\\{\\s*[A-Za-z0-9\\$\\#]+\\s*\\}\\s*", "");
    }

    /**
     * determines the local restvars
     *
     * @param method
     * @return
     */
    private static Stream<RestVar> getRestVars(Method method) {
        return Arrays.stream(method.getParameters()).filter(parameter -> {
            return parameter.isAnnotationPresent(PathVariable.class) || parameter.isAnnotationPresent(RequestParam.class) ||
                    parameter.isAnnotationPresent(RequestBody.class);
        }).map(parameter -> {
            String name = parameter.getName();
            Class paramType = parameter.getType();
            RestVarType restVarType = null;

            if (parameter.isAnnotationPresent(PathVariable.class)) {
                restVarType = RestVarType.PathVariable;
            } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                restVarType = RestVarType.RequesParam;
            } else {
                restVarType = RestVarType.RequestBody;
            }
            boolean array = ReflectUtils.isArrayType(paramType);
            return new RestVar(restVarType, name, paramType, array);
        });
    }

    private static boolean isRestService(Class cls) {
        return cls.isAnnotationPresent(RestController.class);
    }

    /**
     * fetches the service url for the annotated service
     *
     * @param cls
     * @return
     */
    public static String fetchServiceUrl(Class cls) {
        String serviceUrl;
        if (cls.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping ann = (RequestMapping) cls.getAnnotation(RequestMapping.class);
            if (ann.path().length > 0) {
                serviceUrl = ann.path()[0];
            } else {
                serviceUrl = ann.value()[0];
            }
        } else {
            serviceUrl = "";
        }
        return serviceUrl;
    }

    /**
     * fetches the service url for the annotated service
     *
     * @param cls
     * @return
     */
    public static String fetchServiceName(Class cls) {
        String serviceName;
        if (cls.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping ann = (RequestMapping) cls.getAnnotation(RequestMapping.class);
            if (Strings.isNullOrEmpty(ann.name())) {
                serviceName = ReflectUtils.reduceClassName(cls.getName());
            } else {
                serviceName = ann.name();
            }
        } else {
            serviceName = "";
        }

        if(serviceName.endsWith("Controller"))  {
            serviceName = serviceName.replaceAll("Controller", "Service");
        }
        return serviceName;
    }


}
