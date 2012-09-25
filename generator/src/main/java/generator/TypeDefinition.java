package generator;

public abstract class TypeDefinition {
    String name;
    String code;
    
    public TypeDefinition(String name, String code) {
        this.name = name;
        this.code = code;
    }
    
    public String getType() {
        return name.replaceAll("-", "_").toUpperCase();
    }
    
    public String getName() {
        return name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getCode2Digits() {
        int i = Integer.decode(code);
        String s = Integer.toHexString(i);
        if (s.length() == 1) {
            s = "0" + s;
        }
        return s;
    }
    
    public abstract String getClassName();
}
