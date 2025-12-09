package phase4.constants;

public enum AffectedPrice {
    ASKING(0),
    PURCHASE(1),
    APPRAISED(2),
    SOLD(3);

    private final int value;

    AffectedPrice(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static AffectedPrice fromValue(int value) {
        switch (value) {
            case 0:
                return ASKING;
            case 1:
                return PURCHASE;
            case 2:
                return APPRAISED;
            case 3:
                return SOLD;
            default:
                throw new IllegalArgumentException("Invalid value");
        }
    }
}
