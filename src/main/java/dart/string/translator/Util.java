package dart.string.translator;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Util {
    private Util() {
    }

    public static String replaceAllTokens(String original, Pattern pattern, Function<String, String> replacer) {
        StringBuilder modified = new StringBuilder();
        int lastIndex = 0;
        Matcher matcher = pattern.matcher(original);
        while (matcher.find()) {
            modified.append(original, lastIndex, matcher.start());
            modified.append(replacer.apply(matcher.group(0)));
            lastIndex = matcher.end();
        }
        if (lastIndex < original.length()) {
            modified.append(original, lastIndex, original.length());
        }
        return modified.toString();
    }

    public static String replaceAllContexts(String original, Pattern pattern, Function<String, String> replacer) {
        StringBuilder modified = new StringBuilder();
        int lastIndex = 0;
        Matcher matcher = pattern.matcher(original);
        while (matcher.find()) {
            modified.append(replacer.apply(original.substring(lastIndex, matcher.start())));
            modified.append(matcher.group(0));
            lastIndex = matcher.end();
        }
        if (lastIndex < original.length()) {
            modified.append(replacer.apply(original.substring(lastIndex)));
        }
        return modified.toString();
    }
}
