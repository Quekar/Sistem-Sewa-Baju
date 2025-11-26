package com.mycompany.sewabaju.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUtil {
    private static final String UPLOAD_DIR = "uploads/";
    private static final String BAJU_DIR = UPLOAD_DIR + "baju/";
    private static final String BUKTI_DIR = UPLOAD_DIR + "bukti_pembayaran/";
    private static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif"};
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    public static void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(BAJU_DIR));
            Files.createDirectories(Paths.get(BUKTI_DIR));
            System.out.println("Upload directories initialized successfully");
        } catch (IOException e) {
            System.err.println("Error creating upload directories: " + e.getMessage());
        }
    }
    
    public static String uploadBajuPhoto(File sourceFile) throws IOException {
        return uploadFile(sourceFile, BAJU_DIR);
    }
    
    public static String uploadBuktiPembayaran(File sourceFile) throws IOException {
        return uploadFile(sourceFile, BUKTI_DIR);
    }
    
    private static String uploadFile(File sourceFile, String targetDir) throws IOException {
        if (sourceFile == null || !sourceFile.exists()) {
            throw new IOException("File tidak ditemukan");
        }
        
        if (sourceFile.length() > MAX_FILE_SIZE) {
            throw new IOException("Ukuran file terlalu besar (max 5MB)");
        }
        
        String extension = FilenameUtils.getExtension(sourceFile.getName()).toLowerCase();
        if (!isValidImageExtension(extension)) {
            throw new IOException("Format file tidak didukung (hanya jpg, jpeg, png, gif)");
        }
        
        String uniqueFilename = generateUniqueFilename(extension);
        Path targetPath = Paths.get(targetDir + uniqueFilename);
        
        Files.createDirectories(targetPath.getParent());
        
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        
        return uniqueFilename;
    }
    
    public static boolean deleteBajuPhoto(String filename) {
        return deleteFile(BAJU_DIR + filename);
    }
    
    public static boolean deleteBuktiPembayaran(String filename) {
        return deleteFile(BUKTI_DIR + filename);
    }
    
    private static boolean deleteFile(String filepath) {
        try {
            Path path = Paths.get(filepath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Error deleting file: " + e.getMessage());
            return false;
        }
    }
    
    public static byte[] readFileAsBytes(String filepath) throws IOException {
        Path path = Paths.get(filepath);
        
        if (!Files.exists(path)) {
            throw new IOException("File tidak ditemukan: " + filepath);
        }
        
        try (FileInputStream fis = new FileInputStream(path.toFile());
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            
            return baos.toByteArray();
        }
    }
    
    public static String getBajuPhotoPath(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return BAJU_DIR + filename;
    }
    
    public static String getBuktiPembayaranPath(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return BUKTI_DIR + filename;
    }
    
    public static boolean fileExists(String filepath) {
        if (filepath == null || filepath.isEmpty()) {
            return false;
        }
        return Files.exists(Paths.get(filepath));
    }
    
    public static long getFileSize(String filepath) {
        try {
            return Files.size(Paths.get(filepath));
        } catch (IOException e) {
            return 0;
        }
    }
    
    public static String getFileSizeFormatted(String filepath) {
        long bytes = getFileSize(filepath);
        
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
    
    private static String generateUniqueFilename(String extension) {
        return UUID.randomUUID().toString() + "." + extension;
    }
    
    private static boolean isValidImageExtension(String extension) {
        for (String allowed : ALLOWED_IMAGE_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
    
    public static void copyFile(File source, File destination) throws IOException {
        Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    
    public static void moveFile(File source, File destination) throws IOException {
        Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}