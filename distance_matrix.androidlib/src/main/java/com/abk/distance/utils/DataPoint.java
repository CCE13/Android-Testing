package com.abk.distance.utils;

import java.io.Serializable;
import java.util.Date;

public class DataPoint implements Serializable {
    private static final long serialVersionUID = 1428263322645L;

    public double x;
    public double y;

    public DataPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public DataPoint(Date x, double y) {
        this.x = x.getTime();
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "[" + x + "/" + y + "]";
    }
}
