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
import rest.RestVar;
import rest.RestVarType;


import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * a set of utils classes to help with reflection
 */
public class ReflectUtils {


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

    public static Optional<RestVar> getGenericRetVal(Method m) {
        try {
            ParameterizedType retCls = (ParameterizedType) m.getGenericReturnType();
            String ownerType = retCls.getActualTypeArguments()[0].getTypeName(); //java.util.List<bla>
            List<GenericType> childTypes = buildGenericTypes(ownerType);

            GenericType genericTypes = new GenericType("", childTypes);


            RestVar retVal = new RestVar(RestVarType.RequestRetval, null,
                    isArrayType(m.getReturnType()) || isArrayType(false, Optional.of(ownerType)),
                    null,
                    genericTypes);
            return Optional.of(retVal);

        } catch (RuntimeException ex) {
            //no generic
            Type retCls = m.getGenericReturnType();

            return Optional.ofNullable(new RestVar(RestVarType.RequestRetval, null, retCls, false));
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

      /*  final int access = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;

        Package p = clazz.getPackage();
        if (!includeAllPackageAndPrivateMethodsOfSuperclasses) {
            int pass = includeOverridenAndHidden ?
                    Modifier.PUBLIC | Modifier.PROTECTED : Modifier.PROTECTED;
            include = include.and(m -> {
                int mod = m.getModifiers();
                return (mod & pass) != 0
                        || (mod & access) == 0 && m.getDeclaringClass().getPackage() == p;
            });
        }
        if (!includeOverridenAndHidden) {
            Map<Object, Set<Package>> types = new HashMap<>();
            final Set<Package> pkgIndependent = Collections.emptySet();
            for (Method m : methods) {
                int acc = m.getModifiers() & access;
                if (acc == Modifier.PRIVATE) continue;
                if (acc != 0) types.put(methodKey(m), pkgIndependent);
                else types.computeIfAbsent(methodKey(m), x -> new HashSet<>()).add(p);
            }
            include = include.and(m -> {
                int acc = m.getModifiers() & access;
                return acc != 0 ? acc == Modifier.PRIVATE
                        || types.putIfAbsent(methodKey(m), pkgIndependent) == null :
                        noPkgOverride(m, types, pkgIndependent);
            });
        }
        for (clazz = clazz.getSuperclass(); clazz != null; clazz = clazz.getSuperclass())
            Stream.of(clazz.getDeclaredMethods()).filter(include).forEach(methods::add);
         */
        return methods;
    }

    static boolean noPkgOverride(
            Method m, Map<Object, Set<Package>> types, Set<Package> pkgIndependent) {
        Set<Package> pkg = types.computeIfAbsent(methodKey(m), key -> new HashSet<>());
        return pkg != pkgIndependent && pkg.add(m.getDeclaringClass().getPackage());
    }

    private static Object methodKey(Method m) {
        return Arrays.asList(m.getName(),
                MethodType.methodType(m.getReturnType(), m.getParameterTypes()));
    }

    public static boolean isArrayType(Class retCls) {
        return retCls.isArray() || retCls.isInstance(Collections.emptyList());
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
}
