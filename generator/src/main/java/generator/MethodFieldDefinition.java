package generator;

import java.util.StringTokenizer;

public class MethodFieldDefinition {
    private String className;
    private String name;
    String type;
    boolean required;
    private TypeDefinition typeDefinition;
    
    public MethodFieldDefinition(String className, String name, String type, boolean required) {
        this.className = className;
        this.name = name;
        this.type = type;
        this.required = required;
    }

    public TypeDefinition getType() {
        if (null == typeDefinition) {
            typeDefinition = SpecParser.getTypeDefinition(className, type);
        }
            
        return typeDefinition;
    }
    
    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getName() {
        return this.name;
    }

    public String getJavaType() {
        return typeDefinition.getClassName();
    }

    public String getCamelCased() {
        StringTokenizer st = new StringTokenizer(name, "_-");
        String s = "";
        
        if (st.hasMoreTokens()) {
            boolean firstWord = true;
            while(st.hasMoreTokens()) {
                String token = st.nextToken();
                if(firstWord) {
                    firstWord = false;
                    s += token;
                    continue;
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
