package net.dynart.neonsignal.core.controller;

public class AxisData {

    private int code;
    private int sign;

    public AxisData(int code, int sign) {
        this.code = code;
        this.sign = sign;
    }

    public AxisData(String data) {
        String[] parts = data.split(",");
        if (parts.length != 2) {
            throw new RuntimeException("AxisData couldn't be created from string: " + data);
        }
        code = Integer.parseInt(parts[0]);
        sign = Integer.parseInt(parts[1]);
    }

    public String toString() {
        return code + "," + sign;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int value) {
        code = value;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int value) {
        sign = value;
    }
}
