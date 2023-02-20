package i5.las2peer.apiTestModel;

public enum OperatorInput {
    NO_INPUT(0, "No input", ""), INPUT_FIELD(1, "Input field", ""), JSON_OBJECT(2, "JSON Object", "JSONObject"),
    JSON_ARRAY(3, "JSON Array", "JSONArray"), STRING(4, "String", "String"), NUMBER(5, "Number", "Number"), BOOLEAN(6, "Boolean", "Boolean");

    private final int id;
    private final String value;
    private final String javaClassName;

    private OperatorInput(int id, String value, String javaClassName) {
        this.id = id;
        this.value = value;
        this.javaClassName = javaClassName;
    }

    public static OperatorInput fromId(int id) {
        for (OperatorInput o : values()) {
            if (o.getId() == id) return o;
        }
        return null;
    }

    public int getId() {
        return this.id;
    }

    public String getValue() {
        return this.value;
    }

    public String getJavaClassName() {
        return this.javaClassName;
    }
}
