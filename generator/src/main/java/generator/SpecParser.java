package generator;

import com.redhat.osas.amqp.client.MessageType;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

public class SpecParser extends DefaultHandler {
    private static final String TRUE = "true";

    private static final String ELEMENT_RESULT = "result";
    private static final String ELEMENT_ENTRY = "entry";
    private static final String ELEMENT_BODY = "body";
    private static final String ELEMENT_HEADER = "header";
    private static final String ELEMENT_SEGMENTS = "segments";
    private static final String ELEMENT_STRUCT = "struct";
    private static final String ELEMENT_DOMAIN = "domain";
    private static final String ELEMENT_FIELD = "field";
    private static final String ELEMENT_COMMAND = "command";
    private static final String ELEMENT_CONTROL = "control";
    private static final String ELEMENT_CLASS = "class";
    private static final String ELEMENT_TYPE = "type";
    private static final String ELEMENT_CHOICE = "choice";

    private static final String ATTRIBUTE_PACK = "pack";
    private static final String ATTRIBUTE_SIZE = "size";
    private static final String ATTRIBUTE_REQUIRED = "required";
    private static final String ATTRIBUTE_VARIABLE_WIDTH = "variable-width";
    private static final String ATTRIBUTE_FIXED_WIDTH = "fixed-width";
    private static final String ATTRIBUTE_CODE = "code";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_VALUE = "value";

    private static String TEMPLATE_DIRECTORY = "generator/src/main/resources";
    private static String PACKAGE_DIRECTORY = "com/redhat/osas/amqp/client";
    private static String GENERATION_TARGET = "java-amqp-client/src/generated/java/";

    Map<String, ClassDefinition> classDefinitions = new HashMap<String, ClassDefinition>();
    String currentClass;
    String currentControl;
    String currentStruct;
    String currentCommand;
    String currentDomain;
    boolean inSegments = false;
    boolean inHeader = false;
    boolean inResult = false;
    static Map<String, Map<String, String>> domainsByClass = new HashMap<String, Map<String, String>>();
    static Map<String, List<Object[]>> enums = new HashMap<String, List<Object[]>>();
    static Map<String, String> globalDomainToTypeMappings = new HashMap<String, String>();
    static Map<String, TypeDefinition> typesMap = new HashMap<String, TypeDefinition>();
    List<TypeDefinition> typesList = new ArrayList<TypeDefinition>();
    Set<StructDefinition> structs = new HashSet<StructDefinition>();
    private Configuration freemarkerConfiguration;

    private String buildClassPath(String className) {
        return GENERATION_TARGET + PACKAGE_DIRECTORY + "/" + className;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (ELEMENT_TYPE.equals(qName)) {
            handleElement(attributes);
        } else if (ELEMENT_CLASS.equals(qName)) {
            handleClass(attributes);
        } else if (ELEMENT_CONTROL.equals(qName)) {
            handleControl(attributes);
        } else if (ELEMENT_COMMAND.equals(qName)) {
            handleCommand(attributes);
        } else if (ELEMENT_FIELD.equals(qName)) {
            handleField(attributes);
        } else if (ELEMENT_DOMAIN.equals(qName)) {
            handleDomain(attributes);
        } else if (ELEMENT_STRUCT.equals(qName)) {
            handleStruct(attributes);
        } else if (ELEMENT_SEGMENTS.equals(qName)) {
            handleSegments();
        } else if (ELEMENT_HEADER.equals(qName)) {
            handleHeader(attributes);
        } else if (ELEMENT_BODY.equals(qName)) {
            handleBody(attributes);
        } else if (ELEMENT_ENTRY.equals(qName)) {
            handleEntry(attributes);
        } else if (ELEMENT_RESULT.equals(qName)) {
            handleResult();
        } else if (ELEMENT_CHOICE.equals(qName)) {
            handleChoice(attributes);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (ELEMENT_CLASS.equals(qName)) {
            currentClass = null;
        } else if (ELEMENT_CONTROL.equals(qName)) {
            currentControl = null;
        } else if (ELEMENT_COMMAND.equals(qName)) {
            currentControl = null;
        } else if (ELEMENT_STRUCT.equals(qName)) {
            currentStruct = null;
        } else if (ELEMENT_SEGMENTS.equals(qName)) {
            inSegments = false;
        } else if (ELEMENT_HEADER.equals(qName)) {
            inHeader = false;
        } else if (ELEMENT_RESULT.equals(qName)) {
            inResult = false;
        } else if (ELEMENT_DOMAIN.equals(qName)) {
            currentDomain = null;
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        // deliberately left empty - not needed
    }

    private void handleChoice(Attributes attributes) {
        String name = attributes.getValue(ATTRIBUTE_NAME);
        int value = getNullableInt(ATTRIBUTE_VALUE, attributes);
        String mapKey = currentDomain;
        if (currentClass != null) {
            mapKey = currentClass + GeneratorUtils.camelCaseAll(currentDomain);
        }
        List<Object[]> choices = enums.get(mapKey);
        if (null == choices) {
            choices = new ArrayList<Object[]>();
            enums.put(mapKey, choices);
        }
        Object[] data = {name.toUpperCase().replaceAll("-", "_"), value};
        choices.add(data);
    }

    private void handleResult() {
        inResult = true;
    }

    private void handleEntry(Attributes attributes) {
        String type = attributes.getValue(ELEMENT_TYPE);
        getCurrentMethod().getHeaderSegmentEntries().add(type);
    }

    private void handleBody(Attributes attributes) {
        boolean required = TRUE.equals(attributes.getValue(ATTRIBUTE_REQUIRED));
        getCurrentMethod().setBodySegmentRequired(required);
        getCurrentMethod().setHasBodySegment(true);
    }

    private void handleHeader(Attributes attributes) {
        inHeader = true;
        boolean required = TRUE.equals(attributes.getValue(ATTRIBUTE_REQUIRED));
        getCurrentMethod().setHeaderSegmentRequired(required);
    }

    private void handleSegments() {
        inSegments = true;
    }

    private void handleStruct(Attributes attributes) {
        currentStruct = attributes.getValue(ATTRIBUTE_NAME);
        Integer sizeBytes = getNullableInt(ATTRIBUTE_SIZE, attributes);
        String code = attributes.getValue(ATTRIBUTE_CODE);
        Integer packBytes = getNullableInt(ATTRIBUTE_PACK, attributes);

        if (getCurrentMethod() != null) {
            currentStruct = currentClass + GeneratorUtils.camelCaseAll(currentControl + "Result");
        }

        StructDefinition sd = new StructDefinition(currentStruct, code, sizeBytes, packBytes);

        sd.setClassCode(Integer.decode(getCurrentClass().code));
        getCurrentClass().structMap.put(currentStruct, sd);
        structs.add(sd);

        if (currentClass != null) {
            typesMap.put(currentStruct, sd);
            typesMap.put(currentClass + "." + currentStruct, sd);
        }
    }

    private void handleDomain(Attributes attributes) {
        currentDomain = attributes.getValue(ATTRIBUTE_NAME);
        String type = attributes.getValue(ELEMENT_TYPE);

        if (currentClass != null) {
            Map<String, String> domainsForClass = domainsByClass.get(currentClass);

            if (domainsForClass == null) {
                domainsForClass = new HashMap<String, String>();
                domainsByClass.put(currentClass, domainsForClass);
            }

            domainsForClass.put(currentDomain, type);
            globalDomainToTypeMappings.put(currentClass + "." + currentDomain, type);
        } else {
            globalDomainToTypeMappings.put(currentDomain, type);
        }
    }

    private void handleField(Attributes attributes) {
        if (null == currentControl && null == currentStruct) {
            return;
        }

        String fieldName = attributes.getValue(ATTRIBUTE_NAME);
        String type = attributes.getValue(ELEMENT_TYPE);
        boolean required = TRUE.equals(attributes.getValue(ATTRIBUTE_REQUIRED));

        MethodFieldDefinition mfd = new MethodFieldDefinition(currentClass, fieldName, type, required);

        if (currentStruct != null) {
            getCurrentStruct().fields.add(mfd);
        } else {
            getCurrentMethod().fields.add(mfd);
        }
    }

    private void handleCommand(Attributes attributes) {
        String methodName = attributes.getValue(ATTRIBUTE_NAME);
        currentControl = methodName;
        String code = attributes.getValue(ATTRIBUTE_CODE);
        MethodDefinition md = new MethodDefinition(methodName, code, MessageType.COMMAND);
        getCurrentClass().addMethod(md);
        md.setParent(getCurrentClass());
    }

    private void handleControl(Attributes attributes) {
        String methodName = attributes.getValue(ATTRIBUTE_NAME);
        currentControl = methodName;
        String code = attributes.getValue(ATTRIBUTE_CODE);
        MethodDefinition md = new MethodDefinition(methodName, code, MessageType.CONTROL);
        getCurrentClass().addMethod(md);
        md.setParent(getCurrentClass());
    }

    private void handleClass(Attributes attributes) {
        String clazz = attributes.getValue(ATTRIBUTE_NAME);
        currentClass = clazz;
        String code = attributes.getValue(ATTRIBUTE_CODE);
        ClassDefinition cd = new ClassDefinition(clazz, code);
        classDefinitions.put(clazz, cd);
    }

    private void handleElement(Attributes attributes) {
        String name = attributes.getValue(ATTRIBUTE_NAME);
        String code = attributes.getValue(ATTRIBUTE_CODE);
        Integer fixedBits = getNullableInt(ATTRIBUTE_FIXED_WIDTH, attributes);
        Integer variableBits = getNullableInt(ATTRIBUTE_VARIABLE_WIDTH, attributes);
        TypeDefinition td = new SimpleTypeDefinition(name, code, fixedBits, variableBits);
        typesMap.put(name, td);
        typesList.add(td);
    }

    private Integer getNullableInt(String attributeName, Attributes attributes) {
        String value = attributes.getValue(attributeName);
        if (value != null) {
            return Integer.valueOf(value);
        }

        return 0;
    }

    private ClassDefinition getCurrentClass() {
        return classDefinitions.get(currentClass);
    }

    private MethodDefinition getCurrentMethod() {
        return getCurrentClass().methodMap.get(currentControl);
    }

    public Map<String, ClassDefinition> getClassDefinitions() {
        return classDefinitions;
    }

    private StructDefinition getCurrentStruct() {
        return getCurrentClass().structMap.get(currentStruct);
    }

    public static TypeDefinition getTypeDefinition(String className, String type) {
        TypeDefinition typeDefinition = typesMap.get(type);
        if (typeDefinition == null) {
            // try current scope
            Map<String, String> domainsForClass = domainsByClass.get(className);

            String realType = null;

            if (domainsForClass != null) {
                realType = domainsForClass.get(type);
            }

            if (null == realType) {
                realType = globalDomainToTypeMappings.get(type);
            }

            if (realType != null) {
                typeDefinition = typesMap.get(realType);
            }
        }

        if (null == typeDefinition) {
            throw new RuntimeException("Unable to find type definition for type " + type);
        }

        return typeDefinition;
    }

    private SAXParser setupSAXParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        return saxParser;
    }

    private Configuration setupFreemarker() throws IOException {
        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(new File(TEMPLATE_DIRECTORY));
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        cfg.setWhitespaceStripping(true);
        return cfg;
    }

    private void generateInternal(String templateName, String outputFile, Map<String, Object> templateData) throws IOException, TemplateException {
        Template template = freemarkerConfiguration.getTemplate(templateName);

        generateInternal(template, outputFile, templateData);
    }

    private void generateInternal(Template template, String outputFile, Map<String, Object> templateData) throws IOException, TemplateException {
        FileOutputStream fileOut = new FileOutputStream(outputFile);
        Writer out = new OutputStreamWriter(fileOut);

        String localPackageName = outputFile.substring(GENERATION_TARGET.length());

        localPackageName = localPackageName.substring(0, localPackageName.lastIndexOf("/"));
        localPackageName = localPackageName.replace('/', '.');
        System.out.println(localPackageName);
        templateData.put("packageName", localPackageName);

        template.process(templateData, out);

        out.close();
        fileOut.close();
    }

    private void generateTypeClass() throws IOException, TemplateException {
        Map<String, Object> typeRoot = new HashMap<String, Object>();
        typeRoot.put("types", typesList);
        generateInternal("Type.tmpl", buildClassPath("Type.java"), typeRoot);
    }


    private void generateStructs() throws IOException, TemplateException {
        Template structTemplate = freemarkerConfiguration.getTemplate("Struct.tmpl");
        for (StructDefinition sd : structs) {
            Map<String, Object> structRoot = new HashMap<String, Object>();
            structRoot.put(ELEMENT_STRUCT, sd);
            System.out.println("Generating struct " + sd.getClassName());
            generateInternal(structTemplate, buildClassPath("protocol/" + sd.getClassName() + ".java"), structRoot);
        }
    }

    private void generateProxies() throws IOException, TemplateException {
        Template classTemplate = freemarkerConfiguration.getTemplate("Proxy.tmpl");

        Set<String> classes = classDefinitions.keySet();
        for (String className : classes) {
            ClassDefinition cd = classDefinitions.get(className);
            Map<String, Object> classRoot = new HashMap<String, Object>();
            classRoot.put("className", className);
            classRoot.put("classCode", cd.code);
            classRoot.put("methods", cd.methods);
            classRoot.put("structs", cd.structMap);

            String capitalizedClassName = cd.getCapitalizedName();

            String outputFileName = buildClassPath("protocol/" + capitalizedClassName + "Proxy.java");
            generateInternal(classTemplate, outputFileName, classRoot);

            generateMethodArguments(cd);
        }
    }

    private void generateMethodArguments(ClassDefinition classDefinition) throws IOException, TemplateException {
        Template argumentsTemplate = freemarkerConfiguration.getTemplate("Arguments.tmpl");
        for (MethodDefinition method : classDefinition.methods) {
            Map<String, Object> argumentsRoot = new HashMap<String, Object>();

            argumentsRoot.put("className", classDefinition.getName());
            argumentsRoot.put("method", method);

            String className = classDefinition.getCapitalizedName();
            String outputFileName = buildClassPath("protocol/" + className + method.getArgumentsClassName() + ".java");

            generateInternal(argumentsTemplate, outputFileName, argumentsRoot);
        }
    }

    private void generateSessionBase() throws IOException, TemplateException {
        List<ClassDefinition> classDefinitions = new ArrayList<ClassDefinition>(this.classDefinitions.values());
        Map<String, Object> templateData = new HashMap<String, Object>();
        templateData.put("classDefinitions", classDefinitions);
        generateInternal("SessionBase.tmpl", buildClassPath("SessionBase.java"), templateData);
    }

    private void generateMethodArgumentsFactory() throws IOException, TemplateException {
        List<ClassDefinition> classDefinitions = new ArrayList<ClassDefinition>(this.classDefinitions.values());
        Map<String, Object> templateData = new HashMap<String, Object>();
        templateData.put("classDefinitions", classDefinitions);
        generateInternal("MethodArgumentsFactory.tmpl", buildClassPath("MethodArgumentsFactory.java"), templateData);
    }

    private void generateStructFactory() throws IOException, TemplateException {
        List<ClassDefinition> classDefinitions = new ArrayList<ClassDefinition>(this.classDefinitions.values());
        Map<String, Object> templateData = new HashMap<String, Object>();
        templateData.put("classDefinitions", classDefinitions);
        generateInternal("StructFactory.tmpl", buildClassPath("StructFactory.java"), templateData);
    }

    private void generateEnums() throws IOException, TemplateException {
        for (String domain : enums.keySet()) {
            List<Object[]> choices = enums.get(domain);

            String className = GeneratorUtils.camelCaseAll(domain);

            Map<String, Object> templateData = new HashMap<String, Object>();
            templateData.put("className", className);
            templateData.put("choices", choices);
            generateInternal("Enum.tmpl", buildClassPath(className + ".java"), templateData);
        }
    }

    public void execute() throws SAXException, IOException, ParserConfigurationException, TemplateException {
        SAXParser saxParser = setupSAXParser();

        saxParser.parse("generator/src/main/resources/amqp.0-10-qpid-errata.xml", this);

        File f = new File(buildClassPath(""));
        f.mkdirs();
        System.out.println(f);

        f = new File(buildClassPath("protocol"));
        f.mkdirs();
        System.out.println(f);

        freemarkerConfiguration = setupFreemarker();

        generateTypeClass();
        generateStructs();
        generateProxies();
        generateSessionBase();
        generateMethodArgumentsFactory();
        generateStructFactory();
        generateEnums();
    }

    public static void main(String[] args) throws Exception {
        SpecParser parser = new SpecParser();
        parser.execute();
    }
}
