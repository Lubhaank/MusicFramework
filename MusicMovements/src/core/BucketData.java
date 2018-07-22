package core;

public enum BucketData {

    FIVE(5, 4.4653),
    NINE(9, 2),
    TEN(10, 1.83263),
    TWELVE(12, 1.61558),
    TWENTY(20, 1.28291),
    TWENTY_FIVE(25, 1.20507),
    FIFTY(50, 1.07672),
    ONE_HUNDRED(100, 1.02751);

    private int value;
    private double multiplier;

    BucketData(int value, double multiplier) {
        this.value = value;
        this.multiplier = multiplier;
    }

    public int getValue() { return value; }

    public double getMultiplier() { return multiplier; }
}
