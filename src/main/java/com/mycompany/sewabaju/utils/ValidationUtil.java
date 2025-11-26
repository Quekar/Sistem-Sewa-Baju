package com.mycompany.sewabaju.utils;

import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+62|62|0)[0-9]{9,12}$"
    );
    
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    public static boolean hasMinLength(String str, int minLength) {
        if (str == null) return false;
        return str.length() >= minLength;
    }
    
    public static boolean hasMaxLength(String str, int maxLength) {
        if (str == null) return true;
        return str.length() <= maxLength;
    }
    
    public static boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        return NUMERIC_PATTERN.matcher(str.trim()).matches();
    }
    
    public static boolean isPositiveNumber(String str) {
        if (!isNumeric(str)) return false;
        try {
            return Integer.parseInt(str) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isValidPrice(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            double value = Double.parseDouble(str);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static String sanitize(String input) {
        if (input == null) return "";
        
        // Remove SQL injection attempts
        return input.replaceAll("[';\"\\\\]", "").trim();
    }
    
    public static boolean isValidName(String name) {
        if (isEmpty(name)) return false;
        return name.matches("^[a-zA-Z\\s]+$");
    }
    
    public static String getEmailErrorMessage(String email) {
        if (isEmpty(email)) {
            return "Email tidak boleh kosong";
        }
        if (!isValidEmail(email)) {
            return "Format email tidak valid";
        }
        return "";
    }
    
    public static String getPhoneErrorMessage(String phone) {
        if (isEmpty(phone)) {
            return "Nomor HP tidak boleh kosong";
        }
        if (!isValidPhone(phone)) {
            return "Format nomor HP tidak valid (contoh: 081234567890)";
        }
        return "";
    }
    
    public static boolean areAllFieldsFilled(String... fields) {
        for (String field : fields) {
            if (isEmpty(field)) {
                return false;
            }
        }
        return true;
    }
}