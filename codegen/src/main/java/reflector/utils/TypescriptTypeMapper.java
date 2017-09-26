package reflector.utils;

public class TypescriptTypeMapper {

    public static final String map(String in) {
        in = in.trim();
        if (in.equals("java.lang.String")) {
            return "string";
        } else if (in.equals("int") || in.equals("long") || in.equals("Integer") || in.equals("Long") || in.equals("Numeric") || in.equals("java.lang.Integer") || in.equals("java.lang.Long") | in.equals("java.lang.Numeric")) {
            return "number";
        } else if (in.equals("float") || in.equals("double") || in.equals("Float") || in.equals("Double") || in.equals("java.lang.Float") || in.equals("java.lang.Double")) {
            return "float";
        } else if (in.equals("java.util.List") || in.equals("List") || in.equals("Set") || in.equals("java.util.Set")) {
            return "Array";
        } else {
            return ReflectUtils.reduceClassName(in);
        }
    }


}
