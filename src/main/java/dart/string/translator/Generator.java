package dart.string.translator;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import org.stringtemplate.v4.ST;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.cloud.translate.Translate.*;

public final class Generator {

    private static final char START_DELIMITER = '~';
    private static final char END_DELIMITER = '~';
    private final String language;
    private final Translate translateService;
    private final List<String> tags = new ArrayList<>();
    private final List<String> sourceValues = new ArrayList<>();
    private final TokenExtractor tokenExtractor;
    private final OutputStream to;
    private final String placeHolderTemplate;
    private final List<Translation> translations;
    private static final int GROUP_SIZE = 1600;

    public Generator(String sourceLang, String targetLang, Translate translateService, InputStream from, OutputStream to) {
        this.language = targetLang;
        this.translateService = translateService;
        this.tokenExtractor = new TokenExtractor(from);
        placeHolderTemplate = tokenExtractor.extract();
        this.to = to;
        loadTags();

        int currentCount = 0;
        List<String> currentRequest = new ArrayList<>();
        List<Translation> allTranslations = new ArrayList<>();
        for(String word : sourceValues){
            if (word.length() + currentCount <= GROUP_SIZE){
                currentRequest.add(word);
                currentCount += word.length();
            }else{
                allTranslations.addAll(translateService.translate(currentRequest, TranslateOption.sourceLanguage(sourceLang), TranslateOption.targetLanguage(targetLang)));
                currentRequest = new ArrayList<>();
                currentRequest.add(word);
                currentCount = word.length();
            }
        }
        if (currentRequest.size() != 0){
            allTranslations.addAll(translateService.translate(currentRequest, TranslateOption.sourceLanguage(sourceLang), TranslateOption.targetLanguage(targetLang)));
        }
        this.translations = allTranslations;
    }

    private void loadTags() {
        tokenExtractor.getTokens().forEach(token -> {
            tags.add(token.id);
            sourceValues.add(token.plainText);
        });
    }

    public void run() {
        List<String> translatedTexts = translations.stream().map(Translation::getTranslatedText).collect(Collectors.toList());
        for (int i = 0; i < translatedTexts.size(); i++){
            System.err.printf("Source: [%s] Target: [%s]\n", sourceValues.get(i), translatedTexts.get(i));
        }
        ST template = new ST(placeHolderTemplate, START_DELIMITER, END_DELIMITER);
        for (int i = 0; i < translatedTexts.size(); i++) {
            String insertion = translatedTexts.get(i);
            if (sourceValues.get(i).endsWith(" "))
                insertion = insertion + " ";
            if (sourceValues.get(i).startsWith(" "))
                insertion = " " + insertion;
            template.add(tags.get(i), insertion);
        }
        template.add(String.format("token%d", tokenExtractor.getTokens().size() - 1), toCamel(language));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(to));
        out.println(template.render());
        out.close();
    }

    private InputStream getFileFromResourcesAsStream(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        if (inputStream == null)
            throw new IllegalArgumentException("file not found! " + fileName);
        else return inputStream;
    }

    private String toCamel(String target) {
        return (target.charAt(0) + "").toUpperCase() + target.substring(1);
    }
}
