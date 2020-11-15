package dart.string.translator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class TokenExtractor {

    private static final Pattern DART_STRING_PATTERN = Pattern.compile("(['\"])(?:(?!\\1).)*\\1");
    private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("\\$(([\\w\\d][\\w\\d]+)|(\\{([^'\"{}]+)}))");
    private static final Pattern IMPORT_LINE = Pattern.compile("^import\\s+(['\"])(?:(?!\\1).)*\\1;$");
    private static final String EMPTY_STRING = "";
    private final String template;
    private final List<Token> tokens = new ArrayList<>();
    private final List<String> ignoredImports = new ArrayList<>();

    public TokenExtractor(String template) {
        this.template = template;
    }

    public TokenExtractor(InputStream source) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(source));
        StringBuilder builder = new StringBuilder();
        reader.lines().forEach(line -> {
            if (IMPORT_LINE.asPredicate().test(line)) {
                ignoredImports.add(line + "\n");
            } else {
                builder.append(line).append("\n");
            }
        });
        template = builder.toString();
    }

    public String extract() {
        final int[] tokenId = {0};

        String placeholderTemplate = String.join("", ignoredImports) + Util.replaceAllTokens(template, DART_STRING_PATTERN, dartString -> {
            char quotation = dartString.charAt(0);
            StringBuilder builder = new StringBuilder();
            builder.append(quotation);
            String content = dartString.substring(1, dartString.length() - 1);
            builder.append(Util.replaceAllContexts(content, INTERPOLATION_PATTERN, text -> {
                if (text.isEmpty()) return EMPTY_STRING;
                tokens.add(new Token(String.format("token%d", tokenId[0]), text));
                return String.format("~token%d~", tokenId[0]++);
            }));
            return builder.append(quotation).toString();
        });
        tokens.add(new Token(String.valueOf(tokenId[0] + 1), ""));
        return placeholderTemplate.replaceFirst("class\\sStringsEn", String.format("class Strings~token%d~", tokenId[0]));
    }

    public List<Token> getTokens() {
        return tokens;
    }

}
