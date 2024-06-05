package de.thb.demonstrator.enums;

public enum SendingType {
    FILE("file"),
    DUMMY("dummy");

    private final String type;

    SendingType(String type) {
        this.type = type;
    }

    public static SendingType fromString(String value) {
        for (SendingType type : SendingType.values()) {
            if (type.type.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return values()[values().length -1];
    }

    public String toString() {
        return type;
    }
}
