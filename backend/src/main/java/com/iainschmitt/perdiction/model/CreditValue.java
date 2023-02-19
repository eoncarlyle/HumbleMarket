package com.iainschmitt.perdiction.model;

import lombok.Getter;

/*
 * 200 Credits will be displayed as 2,
 TODO: Think about if you might want to change this 
 */
public class CreditValue {
    public static int MAX = 100;
    public static int MIN = 0;
    @Getter
    public int val;

    public CreditValue(int val) {
        checkVal(val);
        this.val = val;
    }

    public static CreditValue of(int val) {
        return new CreditValue(val);
    }

    public void setVal(int val) {
        checkVal(val);
        this.val = val;
    }

    public static void checkVal(int val) {
        if (val < MIN || val > MAX) {
            throw new IllegalArgumentException();
        }
    }
}
