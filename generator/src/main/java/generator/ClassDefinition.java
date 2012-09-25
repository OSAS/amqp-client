package generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassDefinition {
    String name;
    String code;
    Map<String, MethodDefinition> methodMap = new HashMap<String, MethodDefinition>();
    List<MethodDefinition> methods = new ArrayList<MethodDefinition>();
    Map<String, StructDefinition> structMap = new HashMap<String, StructDefinition>();
    
    public ClassDefinition(String name, String code) {
        this.name = name;
        this.code = code;
    }
    
    public void addMethod(MethodDefinition methodDefinition) {
        methodMap.put(methodDefinition.name, methodDefinition);
        methods.add(methodDefinition);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getCapitalizedName() {
        return name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public String getCode2Digits() {
        int i = Integer.decode(code);
        String s = Integer.toHexString(i);
        if (s.length() == 1) {
            s = "0" + s;
        }
        return s;
    }

    public Map<String, MethodDefinition> getMethodMap() {
        return methodMap;
    }

    public void setMethodMap(Map<String, MethodDefinition> methods) {
        this.methodMap = methods;
    }

    public List<MethodDefinition> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodDefinition> methodList) {
        this.methods = methodList;
    }

    public void addStruct(StructDefinition sd) {
        structMap.put(sd.name, sd);
    }

    public Map<String, StructDefinition> getStructMap() {
        return structMap;
    }

    public void setStructMap(Map<String, StructDefinition> structMap) {
        this.structMap = structMap;
    }
}
