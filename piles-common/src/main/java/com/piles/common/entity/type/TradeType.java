package com.piles.common.entity.type;

/**
 * 厂商类型
 */
public enum TradeType {

    TU_KE(1, "途客"),
    XUN_DAO(2, "寻道");

    private TradeType(int code, String value) {
        this.code = code;
        this.value = value;
    }

    private int code;
    private String value;

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static TradeType fromCode(int code) {
        for (TradeType item : TradeType.values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }
}
