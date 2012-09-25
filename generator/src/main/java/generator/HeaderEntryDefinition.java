package generator;

public class HeaderEntryDefinition {
    private String name;
    private StructDefinition struct;
    
    public HeaderEntryDefinition(String name, StructDefinition struct) {
        this.name = name;
        this.struct = struct;
    }

    public String getUnderscoredName() {
        String s = name.replaceAll("-", "_");
        return s;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StructDefinition getStruct() {
        return struct;
    }

    public void setStruct(StructDefinition struct) {
        this.struct = struct;
    }
}
