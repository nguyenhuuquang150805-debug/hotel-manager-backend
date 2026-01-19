package com.nguyenhuuquang.hotelmanagement.dto;

import java.util.List;

public class PayosPaymentRequest {
    public Long orderCode;
    public Long amount;
    public String description;
    public String buyerName;
    public String buyerEmail;
    public String buyerPhone;
    public List<Item> items;
    public String cancelUrl;
    public String returnUrl;
    public Integer expiredAt;
    public String signature;

    public static class Item {
        public String name;
        public Integer quantity;
        public Long price;
        public String unit;
        public Integer taxPercentage;
    }
}
