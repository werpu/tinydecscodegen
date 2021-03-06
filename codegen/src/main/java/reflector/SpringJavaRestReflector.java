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
package reflector;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import org.springframework.web.bind.annotation.*;
import reflector.utils.ReflectUtils;
import rest.*;

import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;


/**
 * Reflection class to handle spring based rest services
 */
public class SpringJavaRestReflector {


    public static List<GenericClass> reflectDto(List<Class> toReflect, Class includingEndpoint) {


        return toReflect.stream().map(clazz -> {
            if (clazz.isEnum()) {
                //we have an enum here
                return reflectEnum(includingEndpoint, clazz);
            } else {
                return reflectClass(includingEndpoint, clazz);
            }


        }).collect(Collectors.toList());
    }

    /**
     * for now no endpoint for enums, it is all or nothing
     *
     * @param clazz
     * @return
     */
    private static GenericClass reflectEnum(Class includingEndpoint, Class clazz) {


        GenericEnum parent = null;


        List<String> attributes = Arrays.stream(clazz.getEnumConstants())
                .map(enumValue -> Enums.getField((Enum) enumValue).getName())
                .collect(Collectors.toList());

        if (includingEndpoint.getName().equals(clazz.getName()) && !clazz.getSuperclass().equals(Object.class) && !clazz.getSuperclass().equals(Enum.class)) {
            parent = (GenericEnum) reflectDto(Arrays.asList(clazz.getSuperclass()), includingEndpoint).get(0);
        }

        GenericType classDescriptor = ReflectUtils.buildGenericTypes(clazz.getTypeName()).get(0);

        return new GenericEnum(classDescriptor, parent, attributes);


    }

    private static GenericClass reflectClass(Class includingEndpoint, Class clazz) {
        Collection<GenericVar> props = emptyList();
        GenericClass parent = null;
        try {
            props = ReflectUtils.getAllProperties(clazz, includingEndpoint);

            if (includingEndpoint.getName().equals(clazz.getName()) && !clazz.getSuperclass().equals(Object.class)) {
                parent = reflectDto(Arrays.asList(clazz.getSuperclass()), includingEndpoint).get(0);
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        TypeVariable[] tv = clazz.getTypeParameters();
        //if(tv != null && tv.length > 0) {
        //    classDescriptor.set(Arrays.stream(tv).map(e -> new GenericType(reduceType(e), emptyList())).collect(Collectors.toList()));
        //}
        String generics = "<"+Arrays.stream(tv).map(SpringJavaRestReflector::reduceType).reduce((e1, e2) -> e1+","+e2).get()+">";
        if(generics == "<>") {
            generics = "";
        }

        GenericType classDescriptor = ReflectUtils.buildGenericTypes(clazz.getTypeName()+generics).get(0);



        return new GenericClass(classDescriptor, parent, emptyList(), props.stream().collect(Collectors.toList()));
    }

    public static String reduceType(TypeVariable tv) {
        String name = tv.getName();
        return name;
    }




    public static List<RestService> reflectRestService(List<Class> toReflect, boolean flattenResult) {
        return toReflect.parallelStream().filter(cls -> {
            return isRestService(cls);
        }).map(cls -> {
            String serviceUrl = fetchServiceUrl(cls);
            List<RestMethod> restMethods = fetchRestMethods(cls, flattenResult);
            return new RestService(fetchServiceName(cls), serviceUrl, cls.getName(), restMethods);
        }).collect(Collectors.toList());
    }


    private static List<RestMethod> fetchRestMethods(Class cls, boolean flattenResult) {

        Collection<Method> methods = ReflectUtils.getAllMethods(cls, false, false);
        return methods.stream()
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .flatMap(method -> mapMethod(method, flattenResult))
                .sorted(Comparator.comparing(RestMethod::getName))
                .collect(Collectors.toList());

    }

    public static Stream<RestMethod> mapMethod(Method method, boolean flattenResult) {
        RequestMapping mapping = method.getAnnotation(RequestMapping.class);
        String name = method.getName();
        String[] value = mapping.value();
        RequestMethod[] reqMethods = mapping.method(); //if there is more than one request method we map it over to additional typescript methods

        reqMethods = reqMethodDefault(reqMethods);

        //we can savely ignore produces since we always return a json object in our case

        List<RestVar> restVars = getRestVars(method).collect(Collectors.toList());
        String url = cutOffParams(value[0]);
        List<RestMethod> restMethods = Lists.newArrayList();


        Optional<RestVar> returnValue = ReflectUtils.getGenericRetVal(method);

        expandAllReqMethods(name, reqMethods, restVars, url, restMethods, returnValue);
        return restMethods.stream();
    }

    private static void expandAllReqMethods(String name, RequestMethod[] reqMethods, List<RestVar> restVars, String url, List<RestMethod> restMethods, Optional<RestVar> returnValue) {
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
    }

    private static RequestMethod[] reqMethodDefault(RequestMethod[] reqMethods) {
        if (reqMethods.length == 0) {
            reqMethods = new RequestMethod[1];
            reqMethods[0] = RequestMethod.GET;
        }
        return reqMethods;
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
        int index = 0;
        final Parameter[] params = method.getParameters();
        final List<Parameter> paramsList = Arrays.asList(params);


        final String names[] = new BytecodeReadingParanamer().lookupParameterNames(method, true);


        return Arrays.stream(params).filter(parameter -> parameter.isAnnotationPresent(PathVariable.class) || parameter.isAnnotationPresent(RequestParam.class) ||
                parameter.isAnnotationPresent(RequestBody.class)).map(parameter -> {
            int pos = paramsList.indexOf(parameter);
            Class paramType = parameter.getType();
            String name = (names.length <= pos) ? parameter.getName() : names[pos];
            RestVarType restVarType;
            String restName = "";
            if (parameter.isAnnotationPresent(PathVariable.class)) {
                restVarType = RestVarType.PathVariable;
            } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                restVarType = RestVarType.RequestParam;
                restName = parameter.getAnnotation(RequestParam.class).value();

            } else {
                restVarType = RestVarType.RequestBody;
            }
            boolean array = ReflectUtils.isArrayType(paramType);
            return new RestVar(restVarType, restName, name, new GenericType(paramType.getTypeName(), emptyList()), array);
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
            serviceName = ReflectUtils.reduceClassName(cls.getName());
        }

        if (serviceName.endsWith("Controller")) {
            serviceName = serviceName.replaceAll("Controller", "Service");
        }
        return serviceName;
    }


}
