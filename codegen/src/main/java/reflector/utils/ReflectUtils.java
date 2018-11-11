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
package reflector.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import rest.GenericType;
import rest.GenericVar;
import rest.RestVar;
import rest.RestVarType;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * a set of net.werpu.tools.supportive classes to help with reflection
 */
public class ReflectUtils {


    /**
     * semantic splitting of a comma separated list of types (but not their embedded generics)
     *
     * @param in a string representation of generic types
     * @return a list of generic types as string
     */
    public static List<String> semanticSplitting(String in) {

        List<String> tokens = Lists.newLinkedList();
        StringBuilder word = new StringBuilder();
        char[] chars = in.toCharArray();
        int parenthesesCnt = 0;
        for (int cnt = 0; cnt < chars.length; cnt++) {
            switch (chars[cnt]) {
                case ',':
                    if (parenthesesCnt == 0) {
                        tokens.add(word.toString());
                        word = new StringBuilder();
                    } else {
                        word.append(chars[cnt]);
                    }
                    break;
                case '<':
                    parenthesesCnt++;
                    word.append(chars[cnt]);
                    break;
                case '>':
                    parenthesesCnt--;
                    word.append(chars[cnt]);
                    break;
                default:
                    word.append(chars[cnt]);
                    break;
            }
        }
        tokens.add(word.toString());
        return tokens;
    }

    /**
     * parses a generic type variable information into a parsing tree
     *
     * @param tStr
     * @return
     */
    public static List<GenericType> buildGenericTypes(String tStr) {
        List<String> nativeTypes = semanticSplitting(tStr);
        List<GenericType> retVal = Lists.newArrayList();
        for (String nativeType : nativeTypes) {
            String owner = nativeType.replaceAll("^([^\\<]*)\\<+.*\\>\\s*$", "$1");
            String genericContent = (tStr.contains("<")) ? tStr.replaceAll("^[^\\<]*\\<+(.*)\\>\\s*$", "$1") : "";
            List genericTypes = Collections.emptyList();
            if (!owner.equals(nativeType)) {
                genericTypes = buildGenericTypes(genericContent);
            }
            GenericType genericType = new GenericType(owner, genericTypes);
            retVal.add(genericType);
        }


        return retVal;
    }

    /**
     * transforms the internal retval into our generic variable representation
     *
     * @param m the method which needs to be investigated
     * @return an optional of a variable with absent being the return value void
     */
    public static Optional<RestVar> getGenericRetVal(Method m) {
        try {
            ParameterizedType retCls = (ParameterizedType) m.getGenericReturnType();
            String ownerType = retCls.getActualTypeArguments()[0].getTypeName(); //java.util.List<bla>
            List<GenericType> childTypes = buildGenericTypes(ownerType);

            GenericType genericTypes = new GenericType("", childTypes);


            RestVar retVal = new RestVar(RestVarType.RequestRetval, null, null,
                    isArrayType(m.getReturnType()) || isArrayType(false, Optional.of(ownerType)),
                    null,
                    genericTypes);
            return Optional.of(retVal);

        } catch (RuntimeException ex) {
            //no generic
            Type retCls = m.getGenericReturnType();

            return Optional.ofNullable(new RestVar(RestVarType.RequestRetval, null, null, new GenericType(retCls.getTypeName(), Collections.emptyList()), false));
        }
    }


    //https://stackoverflow.com/questions/28400408/what-is-the-new-way-of-getting-all-methods-of-a-class-including-inherited-defau
    public static Collection<Method> getAllMethods(Class clazz,
                                                   boolean includeAllPackageAndPrivateMethodsOfSuperclasses,
                                                   boolean includeOverridenAndHidden) {

        Predicate<Method> include = m -> !m.isBridge() && !m.isSynthetic() &&
                Character.isJavaIdentifierStart(m.getName().charAt(0))
                && m.getName().chars().skip(1).allMatch(Character::isJavaIdentifierPart);

        Set<Method> methods = new LinkedHashSet<>();
        Collections.addAll(methods, clazz.getMethods());
        methods.removeIf(include.negate());
        Stream.of(clazz.getDeclaredMethods()).filter(include).forEach(methods::add);

        return methods;
    }

    /**
     * gets the entire inheritance hierarchy of a given class excluding java.lang.Object
     * <p>
     * the hierarchy is presented top down for easier gui displaying
     * with the current class being the top class
     * and the last class before object being the last element
     *
     * @return
     */
    public static List<Class> getInheritanceHierarchy(Class clazz) {
        List<Class> hierarchy = Lists.newLinkedList();
        while (!clazz.equals(Object.class)) {
            hierarchy.add(clazz);
            clazz = clazz.getSuperclass();
        }
        return hierarchy;
    }

    /**
     * gets the entire inheritance hierarchy of a given class excluding java.lang.Object
     * <p>
     * the hierarchy is presented top down for easier gui displaying
     * with the current class being the top class
     * and the last class before object being the last element
     *
     * @return
     */
    public static List<String> getInheritanceHierarchyAsString(Class clazz) {
        List<String> retList = getInheritanceHierarchy(clazz).stream().map(theClass -> {
            return theClass.getName();
        }).collect(Collectors.toList());
        return retList;
    }

    /**
     * checks if the inheritance hierarchy has a parent other than object
     *
     * @param clazz the class to check
     * @return true if it extends another class
     */
    public static boolean hasParent(Class clazz) {
        return !clazz.getSuperclass().equals(Object.class) && !clazz.getSuperclass().equals(Enum.class);
    }

    public static Collection<GenericVar> getAllProperties(Class clazz, Class includingEndpoint) throws IntrospectionException {

        PropertyDescriptor[] allProps = Introspector.getBeanInfo(clazz, includingEndpoint.getSuperclass()).getPropertyDescriptors();


        final List<GenericVar> publicProperties = Arrays.asList(clazz.getDeclaredFields()).stream()
                .filter(declaredField -> {
                    return Modifier.isPublic(declaredField.getModifiers()) && !Modifier.isStatic(declaredField.getModifiers());
                }).map(declaredField -> {
                    List<GenericType> genericTypes = buildGenericTypes(declaredField.getGenericType().getTypeName());
                    return new GenericVar(declaredField.getName(), genericTypes.get(0), new GenericType[0]);
                }).collect(Collectors.toList());

        final Set<String> propIdx = publicProperties.parallelStream().map(prop -> {
            return prop.getName();
        }).collect(Collectors.toSet());


        List<GenericVar> accessors = Arrays.asList(allProps).stream()
                //filter out setter only properties
                .filter(propertyDescriptor -> {
                    //we need to filter out the write only and public props for deeper introspection
                    return propertyDescriptor.getReadMethod() != null && !propIdx.contains(propertyDescriptor.getName());
                }).map(propertyDescriptor -> {
                    // Introspector.getBeanInfo(GenericClass.class).getPropertyDescriptors()[2].getReadMethod().getGenericReturnType().getTypeName()
                    List<GenericType> genericTypes = buildGenericTypes(propertyDescriptor.getReadMethod().getGenericReturnType().getTypeName());
                    return new GenericVar(propertyDescriptor.getName(), genericTypes.get(0), new GenericType[0]);
                }).collect(Collectors.toList()); //now lets transform the remaining props

        List<GenericVar> retVal = Lists.newArrayListWithCapacity(publicProperties.size() + accessors.size());
        retVal.addAll(publicProperties);
        retVal.addAll(accessors);
        return retVal;
    }


    public static boolean isArrayType(Class retCls) {
        return retCls.isArray() || retCls.isInstance(Collections.emptyList());
    }

    public static boolean isArrayType(String retCls) {
        return retCls.contains("Set") ||
                retCls.contains("List") ||
                retCls.contains("Collection") ||
                retCls.contains("[");
    }


    public static boolean isArrayType(boolean array, Optional<String> genericTypeName) {
        if (!genericTypeName.isPresent()) {
            return false;
        }
        if (!array && !Strings.isNullOrEmpty(genericTypeName.get())) {
            array = array || genericTypeName.get().contains("Array") || genericTypeName.get().contains("List") || genericTypeName.get().contains("Set")
                    || genericTypeName.get().contains("[]");
        }
        return array;
    }


    public static String reduceClassName(String in) {
        if (Strings.isNullOrEmpty(in)) {
            return in;
        }
        return in.replaceAll(".*[\\.\\$]([^\\<^\\>\\$]+)[\\<\\>]{0,1}.*", "$1");
    }

    public static boolean isJavaType(Class paramType) {
        return isJavaType(paramType.getName());
    }

    public static boolean isJavaType(String paramType) {
        if (Strings.isNullOrEmpty(paramType)) { //something with generics
            return true;
        }
        return (!Strings.isNullOrEmpty(paramType)) &&
                (isNative(paramType) ||
                        isReduced(paramType) ||
                        paramType.startsWith("java."));
    }

    public static boolean isNative(String paramType) {
        return paramType.equals("int") ||
                paramType.equals("long") ||
                paramType.equals("double") ||
                paramType.equals("float") ||
                paramType.equals("boolean");
    }

    public static boolean isReduced(String paramType) {
        return paramType.equals("Integer") ||
                paramType.equals("Boolean") ||
                paramType.equals("Double") ||
                paramType.equals("Float") ||
                paramType.equals("String")||
                paramType.endsWith("Map") ||
                paramType.endsWith("List") ||
                paramType.endsWith("Set") ||
                paramType.endsWith("Tree");
    }
}
