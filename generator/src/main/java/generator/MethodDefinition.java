package generator;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.amentra.amqp.client.MessageType;

public class MethodDefinition {
    String name;
    String code;
    List<MethodFieldDefinition> fields = new ArrayList<MethodFieldDefinition>();
    MessageType messageType;
    boolean headerSegmentRequired = false;
    List<String> headerSegmentEntries = new ArrayList<String>();
    boolean hasBodySegment = false;
    boolean bodySegmentRequired = false;
    ClassDefinition parent;
    
    public MethodDefinition(String methodName, String code, MessageType messageType) {
        this.name = methodName;
        this.code = code;
        this.messageType = messageType;
    }
    
    public boolean getShouldEncodeHeader() {
        return messageType == MessageType.COMMAND;
    }
    
    public String getUnderscoredName() {
        String s = name.replaceAll("-", "_");
        if("return".equals(s)) {
            s = "return_";
        }
        
        return s;
    }
    
    public String getJavaName() {
        StringTokenizer st = new StringTokenizer(name, "_-");
        String s = "";
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            String firstLetter = token.substring(0, 1);
            s += firstLetter.toUpperCase() + token.substring(1, token.length());
        }
        
        return s;
    }
    
    
    public String getArgumentsClassName() {
        String s = getJavaName();
        s += "Arguments";
        return s;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<MethodFieldDefinition> getFields() {
        return fields;
    }

    public void setFields(List<MethodFieldDefinition> fields) {
        this.fields = fields;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public boolean isHeaderSegmentRequired() {
        return headerSegmentRequired;
    }

    public void setHeaderSegmentRequired(boolean headerSegmentRequired) {
        this.headerSegmentRequired = headerSegmentRequired;
    }

    public List<HeaderEntryDefinition> getHeaders() {
        List<HeaderEntryDefinition> r = new ArrayList<HeaderEntryDefinition>();
        
        for (String name : headerSegmentEntries) {
            StructDefinition sd = parent.structMap.get(name);
            HeaderEntryDefinition h = new HeaderEntryDefinition(name, sd);
            r.add(h);
        }
        
        return r;
    }
    
    public List<String> getHeaderSegmentEntries() {
        return headerSegmentEntries;
    }

    public void setHeaderSegmentEntries(List<String> headerSegmentEntries) {
        this.headerSegmentEntries = headerSegmentEntries;
    }

    public boolean isHasBodySegment() {
        return hasBodySegment;
    }

    public void setHasBodySegment(boolean hasBodySegment) {
        this.hasBodySegment = hasBodySegment;
    }

    public boolean isBodySegmentRequired() {
        return bodySegmentRequired;
    }

    public void setBodySegmentRequired(boolean bodySegmentRequired) {
        this.bodySegmentRequired = bodySegmentRequired;
    }

    public ClassDefinition getParent() {
        return parent;
    }

    public void setParent(ClassDefinition parent) {
        this.parent = parent;
    }
}
