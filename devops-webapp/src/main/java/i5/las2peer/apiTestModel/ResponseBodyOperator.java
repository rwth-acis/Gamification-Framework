package i5.las2peer.apiTestModel;

public enum ResponseBodyOperator {
    HAS_TYPE(0), HAS_FIELD(1), HAS_LIST_ENTRY_THAT(2), ALL_LIST_ENTRIES(3);

    private final int id;

    private ResponseBodyOperator(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}