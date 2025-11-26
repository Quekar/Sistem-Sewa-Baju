package com.mycompany.sewabaju.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class DateUtil {
    public static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public static final DateTimeFormatter DATETIME_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public static final DateTimeFormatter DATETIME_FULL_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    public static final DateTimeFormatter DATE_DISPLAY_FORMATTER = 
            DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
    
    public static final DateTimeFormatter DATETIME_DISPLAY_FORMATTER = 
            DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", new Locale("id", "ID"));
    
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }
    
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATETIME_FORMATTER);
    }
    
    public static String formatDateDisplay(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_DISPLAY_FORMATTER);
    }
    
    public static String formatDateTimeDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATETIME_DISPLAY_FORMATTER);
    }
    
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }
    
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.HOURS.between(start, end);
    }
    
    public static boolean isToday(LocalDate date) {
        if (date == null) return false;
        return date.equals(LocalDate.now());
    }
    
    public static boolean isPast(LocalDate date) {
        if (date == null) return false;
        return date.isBefore(LocalDate.now());
    }
    
    public static boolean isFuture(LocalDate date) {
        if (date == null) return false;
        return date.isAfter(LocalDate.now());
    }
    
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        
        if (minutes < 1) return "Baru saja";
        if (minutes < 60) return minutes + " menit yang lalu";
        
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) return hours + " jam yang lalu";
        
        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 7) return days + " hari yang lalu";
        
        long weeks = days / 7;
        if (weeks < 4) return weeks + " minggu yang lalu";
        
        long months = ChronoUnit.MONTHS.between(dateTime, now);
        if (months < 12) return months + " bulan yang lalu";
        
        long years = ChronoUnit.YEARS.between(dateTime, now);
        return years + " tahun yang lalu";
    }
    
    public static LocalDate addDays(LocalDate date, int days) {
        if (date == null) return null;
        return date.plusDays(days);
    }
    
    public static LocalDate subtractDays(LocalDate date, int days) {
        if (date == null) return null;
        return date.minusDays(days);
    }
    
    public static LocalDate getStartOfMonth(LocalDate date) {
        if (date == null) return null;
        return date.withDayOfMonth(1);
    }
    
    public static LocalDate getEndOfMonth(LocalDate date) {
        if (date == null) return null;
        return date.withDayOfMonth(date.lengthOfMonth());
    }
    
    public static LocalDate today() {
        return LocalDate.now();
    }
    
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}