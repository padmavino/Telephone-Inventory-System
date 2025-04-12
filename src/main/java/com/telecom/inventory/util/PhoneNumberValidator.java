package com.telecom.inventory.util;

import java.util.regex.Pattern;

public class PhoneNumberValidator {

    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");
    
    private PhoneNumberValidator() {
        // Private constructor to prevent instantiation
    }
    
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        
        return PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches();
    }
    
    public static String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return phoneNumber;
        }
        
        // Remove all non-digit characters except the leading '+'
        String normalized = phoneNumber.replaceAll("[^\\d+]", "");
        
        // Ensure it starts with '+'
        if (!normalized.startsWith("+")) {
            normalized = "+" + normalized;
        }
        
        return normalized;
    }
}
