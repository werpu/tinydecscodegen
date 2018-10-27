package actions_all.shared;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static supportive.utils.IntellijUtils.getTsExtension;

public class NgFileNameTransformer implements FileNameTransformer {

    private String postfix;

    public NgFileNameTransformer(String postfix) {
        this.postfix = postfix;
    }

    @Override
    public String transform(String className) {

        Pattern pattern = Pattern.compile("([A-Z]{1})");
        Matcher matcher = pattern.matcher(className);

        StringBuilder builder = new StringBuilder();
        int i = 0;
        boolean first = true;
        while (matcher.find()) {
            String replacement = matcher.group(1);
            builder.append(className.substring(i, matcher.start()));
            if(first) {
                first = false;
                builder.append(replacement);
            } else {
                builder.append("-"+replacement);
            }

            i = matcher.end();
        }
        builder.append(className.substring(i, className.length()));

        return builder.toString().toLowerCase()+"."+postfix+getTsExtension();
    }
}
