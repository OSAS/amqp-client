package generator;

import java.util.HashMap;
import java.util.Map;

public class SimpleTypeDefinition extends TypeDefinition {
    Integer fixedBits = 0;
    Integer variableBits = 0;
    
    static Map<String, String> javaTypeMappings = new HashMap<String, String>();
    
    static {
        javaTypeMappings.put("int8", "Byte");
        javaTypeMappings.put("uint8", "Short");
        javaTypeMappings.put("char", "Character");
        javaTypeMappings.put("boolean", "boolean");
        javaTypeMappings.put("int16", "Short");
        javaTypeMappings.put("uint16", "Integer");
        javaTypeMappings.put("int32", "Integer");
        javaTypeMappings.put("uint32", "Long");
        javaTypeMappings.put("float", "Float");
        javaTypeMappings.put("char-utf32", "Long");
        javaTypeMappings.put("sequence-no", "Long");
        javaTypeMappings.put("int64", "Long");
        javaTypeMappings.put("uint64", "Long");
        javaTypeMappings.put("double", "Double");
        javaTypeMappings.put("datetime", "Long");
        javaTypeMappings.put("uuid", "java.util.UUID");
        javaTypeMappings.put("str8-latin", "String");
        javaTypeMappings.put("str8", "String");
        javaTypeMappings.put("str8-utf16", "String");
        javaTypeMappings.put("str16-latin", "String");
        javaTypeMappings.put("str16", "String");
        javaTypeMappings.put("str16-utf16", "String");
        javaTypeMappings.put("byte-ranges", "com.redhat.osas.amqp.client.ByteRanges");
        javaTypeMappings.put("sequence-set", "com.redhat.osas.amqp.client.SequenceSet");
        javaTypeMappings.put("map", "java.util.Map");
        javaTypeMappings.put("list", "java.util.List");
        javaTypeMappings.put("array", "java.util.List");
        javaTypeMappings.put("struct32", "com.redhat.osas.amqp.client.Struct");
        javaTypeMappings.put("dec32", "java.math.BigDecimal");
        javaTypeMappings.put("dec64", "java.math.BigDecimal");
        javaTypeMappings.put("bit", "boolean");
    }

    public SimpleTypeDefinition(String name, String code, Integer fixedBits, Integer variableBits) {
        super(name, code);
        this.fixedBits = fixedBits;
        this.variableBits = variableBits;
    }
    
    @Override
    public String getClassName() {
        if (name.startsWith("bin") || name.startsWith("vbin")) {
            return "Object";
        }
        
        String javaType = javaTypeMappings.get(name);
        if(null == javaType) {
            throw new RuntimeException("Bad type: " + name);
        }
        return javaType;
    }
    
    public Integer getFixedBits() {
        return fixedBits;
    }

    public void setFixedBits(Integer fixedBits) {
        this.fixedBits = fixedBits;
    }

    public Integer getVariableBits() {
        return variableBits;
    }

    public void setVariableBits(Integer variableBits) {
        this.variableBits = variableBits;
    }
}
