package generator;

import java.util.StringTokenizer;

public class GeneratorUtils {
    private GeneratorUtils() {
    }
    
    public static String camelCase(String name) {
        return camelCase(name, false);
    }
    
    public static String camelCaseAll(String name) {
        return camelCase(name, true);
    }
    
    private static String camelCase(String name, boolean upperCaseFirstLetter) {
        StringTokenizer st = new StringTokenizer(name, "_-");
        String s = "";
        
        if (st.hasMoreTokens()) {
            boolean firstWord = true;
            while(st.hasMoreTokens()) {
                String token = st.nextToken();
                if(!upperCaseFirstLetter) {
                    if(firstWord) {
                        firstWord = false;
                        s += token;
                        continue;
                    }
                }
                String firstLetter = token.substring(0, 1);
                s += firstLetter.toUpperCase() + token.substring(1, token.length());
            }
        } else {
            s = name;
        }
        if(s.length() == 0) {
            throw new RuntimeException();
        }
        return s;
    }
}
