package dart.string.translator;

import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class App {

    private static final String GOOGLE_API_KEY = "GOOGLE_API_KEY";
    private static final String PROJECT_ID = "PROJECT_ID";

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 0) {
            System.out.println("Usage:\n" +
                    "\t\tjava -jar <Program name> <dart input file> <input language code> <output language code> > <name>.dart\n" +
                    "or\n" +
                    "\t\tjava -jar <Program name> all");
            System.exit(0);
        } else {
            Translate service = TranslateOptions
                    .newBuilder()
                    .setApiKey(System.getenv(GOOGLE_API_KEY))
                    .setProjectId(System.getenv(PROJECT_ID))
                    .build()
                    .getService();
            List<Language> languages = service.listSupportedLanguages();
            if (args[0].equalsIgnoreCase("all")) {
                languages.forEach(language -> System.out.println(String.format("Name: [%s] <-> Code: [%s]\n", language.getName(), language.getCode())));
                System.exit(0);
            } else {
                new Generator(
                        args[1],
                        args[2],
                        service,
                        new FileInputStream(args[0]),
                        System.out)
                        .run();
            }
        }
    }
}
