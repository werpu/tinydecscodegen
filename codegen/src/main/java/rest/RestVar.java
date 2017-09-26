package rest;

import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import reflector.utils.ReflectUtils;
import reflector.utils.TypescriptTypeMapper;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Function;

@Getter
@EqualsAndHashCode
public class RestVar {

    private final RestVarType paramType;
    private final String name;
    private final boolean array;
    private final Type classType;
    private final GenericType[] generics;


    public RestVar(RestVarType paramType, String name, Type classType, boolean array) {

        this.paramType = paramType;
        this.name = name;
        this.classType = classType;
        generics = new GenericType[0];
        this.array = array;
    }


    public RestVar(RestVarType paramType, String name, boolean array, Type classType, GenericType... generics) {
        this.paramType = paramType;
        this.name = name;
        this.classType = classType;
        this.generics = generics;
        this.array = array;
    }

    public String toTypeScript(Function<String, String>... reducers) {

        GenericType varGeneric = new GenericType((classType != null) ? classType.getTypeName() : "", Arrays.asList(generics));
        return (Strings.isNullOrEmpty(name)) ? varGeneric.toTypescript(reducers) : name + ": " +varGeneric.toTypescript(reducers);
    }


    public String toTypeScript() {
        return this.toTypeScript(TypescriptTypeMapper::map, ReflectUtils::reduceClassName);
    }

}
