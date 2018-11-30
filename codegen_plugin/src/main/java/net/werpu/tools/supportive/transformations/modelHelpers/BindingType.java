package net.werpu.tools.supportive.transformations.modelHelpers;

/**
 * probably the most complicated context of all
 * the component context.
 * <p>
 * The aim for this is following
 * a) find out all the needed imports functions etc...
 * b) find out the component as and if not present use ctrl per default
 * c) Find out all the inlined functions and try to push them to the class level
 * d) Find out all the contextual information regarding the component injects
 * e) find the template reference and try to load the template
 * f) find out about all the watchers currently
 * <p>
 * Upon all this info we should make a transformation source which tries to transform the template
 * depending on the angular level (not part of this class, will be written later)
 * A simple replacer like we have it for the Module Transformation does not cut it anymore
 */
public enum BindingType {
    INPUT, BOTH, ASTRING, FUNC, OPT_INPUT, OPT_BOTH, OPT_ASTRING, OPT_FUNC;

    public static BindingType translate(String in) {
        if (in.startsWith("<")) {
            return INPUT;
        } else if (in.startsWith("<?")) {
            return OPT_INPUT;
        } else if (in.startsWith("@")) {
            return ASTRING;
        } else if (in.startsWith("@?")) {
            return OPT_ASTRING;
        } else if (in.startsWith("=")) {
            return BOTH;
        } else if (in.startsWith("=?")) {
            return OPT_BOTH;
        } else if (in.startsWith("&")) {
            return FUNC;
        } else {
            return OPT_FUNC;
        }
    }
}
