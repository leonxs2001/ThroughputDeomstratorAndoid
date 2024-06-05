package de.thb.demonstrator.enums;

import org.jetbrains.annotations.NotNull;

public enum CommunicationType {
    UPLOAD("upload"),
    DOWNLOAD("download");

    private final String value;

    CommunicationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CommunicationType fromString(String value) {
        CommunicationType firstMember = null;
        for (CommunicationType type : CommunicationType.values()) {
            if (type.value.toLowerCase().equals(value.toLowerCase())) {
                return type;
            } else if (firstMember == null) {
                firstMember = type;
            }
        }
        return firstMember;
    }


    public @NotNull String toString(){
        return value;
    }
}
