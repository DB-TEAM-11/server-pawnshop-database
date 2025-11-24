package phase3.constants;

public enum ItemState {
    CREATED(0),
    DISPLAYING(1),
    RECOVERING(2),
    IN_AUCTION(3),
    SOLD(4),
    RECORVERED(5);

    private final int value;

    ItemState(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static ItemState fromValue(int value) {
        switch (value) {
            case 0:
                return CREATED;
            case 1:
                return DISPLAYING;
            case 2:
                return RECOVERING;
            case 3:
                return IN_AUCTION;
            case 4:
                return SOLD;
            case 5:
                return RECORVERED;
            default:
                throw new IllegalArgumentException("Invalid value");
        }
    }
}
