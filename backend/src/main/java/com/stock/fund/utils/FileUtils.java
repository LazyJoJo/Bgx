package com.stock.fund.utils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 文件操作工具类 提供文件读写、复制、删除等常用文件操作功能
 */
@Slf4j
public class FileUtils {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * 读取文件内容为字符串
     */
    public static String readFileToString(String filePath) {
        return readFileToString(filePath, "UTF-8");
    }

    /**
     * 读取文件内容为字符串（指定编码）
     */
    public static String readFileToString(String filePath, String charset) {
        if (filePath == null) {
            return null;
        }

        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            return new String(bytes, charset);
        } catch (IOException e) {
            log.error("Failed to read file: {}", filePath, e);
            return null;
        }
    }

    /**
     * 读取文件内容为字节数组
     */
    public static byte[] readFileToByteArray(String filePath) {
        if (filePath == null) {
            return null;
        }

        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Failed to read file: {}", filePath, e);
            return null;
        }
    }

    /**
     * 读取文件内容为行列表
     */
    public static List<String> readLines(String filePath) {
        return readLines(filePath, "UTF-8");
    }

    /**
     * 读取文件内容为行列表（指定编码）
     */
    public static List<String> readLines(String filePath, String charset) {
        if (filePath == null) {
            return null;
        }

        try {
            return Files.readAllLines(Paths.get(filePath), java.nio.charset.Charset.forName(charset));
        } catch (IOException e) {
            log.error("Failed to read file lines: {}", filePath, e);
            return null;
        }
    }

    /**
     * 将字符串写入文件
     */
    public static boolean writeStringToFile(String filePath, String content) {
        return writeStringToFile(filePath, content, "UTF-8", false);
    }

    /**
     * 将字符串写入文件（指定编码和追加模式）
     */
    public static boolean writeStringToFile(String filePath, String content, String charset, boolean append) {
        if (filePath == null || content == null) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            // 创建父目录
            createParentDirectories(path);

            if (append) {
                Files.write(path, content.getBytes(charset), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                Files.write(path, content.getBytes(charset), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            log.error("Failed to write file: {}", filePath, e);
            return false;
        }
    }

    /**
     * Write byte array to file
     */
    public static boolean writeByteArrayToFile(String filePath, byte[] data) {
        if (filePath == null || data == null) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            createParentDirectories(path);
            Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            log.error("Failed to write file: {}", filePath, e);
            return false;
        }
    }

    /**
     * 将行列表写入文件
     */
    public static boolean writeLines(String filePath, List<String> lines) {
        return writeLines(filePath, lines, "UTF-8", false);
    }

    /**
     * 将行列表写入文件（指定编码和追加模式）
     */
    public static boolean writeLines(String filePath, List<String> lines, String charset, boolean append) {
        if (filePath == null || lines == null) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            createParentDirectories(path);

            if (append) {
                Files.write(path, lines, java.nio.charset.Charset.forName(charset), StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            } else {
                Files.write(path, lines, java.nio.charset.Charset.forName(charset), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            log.error("Failed to write file lines: {}", filePath, e);
            return false;
        }
    }

    /**
     * 复制文件
     */
    public static boolean copyFile(String sourcePath, String targetPath) {
        if (sourcePath == null || targetPath == null) {
            return false;
        }

        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);
            createParentDirectories(target);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            log.error("Failed to copy file: {} -> {}", sourcePath, targetPath, e);
            return false;
        }
    }

    /**
     * 移动文件
     */
    public static boolean moveFile(String sourcePath, String targetPath) {
        if (sourcePath == null || targetPath == null) {
            return false;
        }

        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);
            createParentDirectories(target);
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            log.error("Failed to move file: {} -> {}", sourcePath, targetPath, e);
            return false;
        }
    }

    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath) {
        if (filePath == null) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            return false;
        }
    }

    /**
     * 创建目录
     */
    public static boolean createDirectory(String directoryPath) {
        if (directoryPath == null) {
            return false;
        }

        try {
            Path path = Paths.get(directoryPath);
            Files.createDirectories(path);
            return true;
        } catch (IOException e) {
            log.error("Failed to create directory: {}", directoryPath, e);
            return false;
        }
    }

    /**
     * 创建父目录
     */
    private static void createParentDirectories(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    /**
     * 检查文件是否存在
     */
    public static boolean exists(String filePath) {
        if (filePath == null) {
            return false;
        }
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 检查是否为目录
     */
    public static boolean isDirectory(String path) {
        if (path == null) {
            return false;
        }
        return Files.isDirectory(Paths.get(path));
    }

    /**
     * 检查是否为文件
     */
    public static boolean isFile(String path) {
        if (path == null) {
            return false;
        }
        return Files.isRegularFile(Paths.get(path));
    }

    /**
     * 获取文件大小
     */
    public static long getFileSize(String filePath) {
        if (filePath == null || !exists(filePath)) {
            return -1;
        }

        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Failed to get file size: {}", filePath, e);
            return -1;
        }
    }

    /**
     * 获取文件最后修改时间
     */
    public static long getLastModified(String filePath) {
        if (filePath == null || !exists(filePath)) {
            return -1;
        }

        try {
            return Files.getLastModifiedTime(Paths.get(filePath)).toMillis();
        } catch (IOException e) {
            log.error("Failed to get file modification time: {}", filePath, e);
            return -1;
        }
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * 获取文件名（不包含路径）
     */
    public static String getFileName(String filePath) {
        if (filePath == null) {
            return null;
        }

        Path path = Paths.get(filePath);
        Path fileName = path.getFileName();
        return fileName != null ? fileName.toString() : null;
    }

    /**
     * 获取文件名（不包含扩展名）
     */
    public static String getBaseName(String fileName) {
        if (fileName == null) {
            return null;
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    /**
     * 获取文件路径（不包含文件名）
     */
    public static String getDirectoryPath(String filePath) {
        if (filePath == null) {
            return null;
        }

        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        return parent != null ? parent.toString() : "";
    }

    /**
     * 列出目录下的所有文件
     */
    public static List<Path> listFiles(String directoryPath) {
        if (directoryPath == null || !isDirectory(directoryPath)) {
            return null;
        }

        try {
            return Files.list(Paths.get(directoryPath)).toList();
        } catch (IOException e) {
            log.error("Failed to list directory files: {}", directoryPath, e);
            return null;
        }
    }

    /**
     * 递归列出目录下所有文件
     */
    public static List<Path> listAllFiles(String directoryPath) {
        if (directoryPath == null || !isDirectory(directoryPath)) {
            return null;
        }

        try {
            return Files.walk(Paths.get(directoryPath)).filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            log.error("Failed to recursively list directory files: {}", directoryPath, e);
            return null;
        }
    }

    /**
     * 安全关闭文件流
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                log.debug("Exception occurred while closing resources", e);
            }
        }
    }

    /**
     * 获取临时文件路径
     */
    public static String getTempFilePath(String prefix, String suffix) {
        try {
            Path tempFile = Files.createTempFile(prefix, suffix);
            return tempFile.toString();
        } catch (IOException e) {
            log.error("Failed to create temp file", e);
            return null;
        }
    }

    /**
     * 清理临时文件
     */
    public static boolean cleanTempFile(String tempFilePath) {
        return deleteFile(tempFilePath);
    }
}