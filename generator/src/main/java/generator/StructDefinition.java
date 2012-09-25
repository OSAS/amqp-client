package generator;

import java.util.ArrayList;
import java.util.List;

public class StructDefinition extends TypeDefinition {
    int size;
    int pack;
    List<MethodFieldDefinition> fields = new ArrayList<MethodFieldDefinition>();
    int classCode = 0;
    
    public StructDefinition(String name, String code, int size, int pack) {
        super(name, code);
        this.size = size;
        this.pack = pack;
    }
    
    public String getClassName() {
        return GeneratorUtils.camelCaseAll(name);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPack() {
        return pack;
    }

    public void setPack(int pack) {
        this.pack = pack;
    }

    public List<MethodFieldDefinition> getFields() {
        return fields;
    }

    public void setFields(List<MethodFieldDefinition> fields) {
        this.fields = fields;
    }

    public int getClassCode() {
        return classCode;
    }

    public void setClassCode(int classCode) {
        this.classCode = classCode;
    }
}
