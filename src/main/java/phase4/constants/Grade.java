package phase4.constants;


public enum Grade {
    NORMAL(0),
    RARE(1),
    UNIQUE(2),
    LEGENDARY(3);

    private final int value;

    Grade(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static Grade fromValue(int value) {
        switch (value) {
            case 0:
                return NORMAL;
            case 1:
                return RARE;
            case 2:
                return UNIQUE;
            case 3:
                return LEGENDARY;
            default:
                throw new IllegalArgumentException("Invalid value");
        }
    }
}
