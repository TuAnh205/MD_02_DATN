package com.anhnvt_ph55017.md_02_datn.models;

import java.io.Serializable;

public class Voucher implements Serializable {
    private String _id;
    private String code;
    private String name;
    private String description;
    private String type;
    private double value;
    private double minOrderValue;
    private double maxDiscount;
    private int usageLimit;
    private int usedCount;
    private int userLimit;
    private boolean isActive;
    private String startDate;
    private String endDate;

    public String get_id() { return _id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public double getValue() { return value; }
    public double getMinOrderValue() { return minOrderValue; }
    public double getMaxDiscount() { return maxDiscount; }
    public int getUsageLimit() { return usageLimit; }
    public int getUsedCount() { return usedCount; }
    public int getUserLimit() { return userLimit; }
    public boolean isActive() { return isActive; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }

    // Setters omitted for brevity (add if needed)
}
