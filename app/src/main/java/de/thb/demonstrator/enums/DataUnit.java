package de.thb.demonstrator.enums;

import org.jetbrains.annotations.NotNull;

public enum DataUnit {
    GB("GB", 1024 * 1024 * 1024),
    MB("MB", 1024 * 1024),
    KB("KB", 1024),
    B("B", 1);

    private final String unit;
    private final int multiplier;

    DataUnit(String unit, int multiplier) {
        this.unit = unit;
        this.multiplier = multiplier;
    }

    public static DataUnit fromString(String value) {
        for (DataUnit unit : DataUnit.values()) {
            if (unit.unit.equalsIgnoreCase(value)) {
                return unit;
            }
        }
        return values()[0];
    }

    public int getMultiplier() {
        return multiplier;
    }

    public @NotNull String toString(){
        return unit;
    }
}


