package br.gov.caixa.gitecsa.sired.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

public class FileUtils {

    public static final String UTF8_BOM = "\uFEFF";
    public static final String FILENAME_DATE_TIME_FORMAT = "dd_MM_yyyy_HH_mm_ss";
    public static final int DEFAULT_BUFFER_SIZE = 4096;

    public static final String SYSTEM_EOL = "\r\n";
    public static final String SYSTEM_FILE_SEPARATOR = System.getProperty("file.separator");

    public static byte[] readAll(File file) throws FileNotFoundException, IOException {

        Long length = file.length();
        byte[] buffer = new byte[length.intValue()];
        FileInputStream inputStream = new FileInputStream(file);

        inputStream.read(buffer, 0, length.intValue());
        inputStream.close();

        return buffer;
    }

    public static String getMimeType(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        String mime = URLConnection.guessContentTypeFromStream(stream);

        if (StringUtils.isBlank(mime)) {
            mime = URLConnection.guessContentTypeFromName(file.getAbsolutePath());
        }

        return mime;
    }

    public static String appendDateTimeToFileName(String filename, Date date) {
        return FileUtils.appendDateTimeToFileName(filename, date, FILENAME_DATE_TIME_FORMAT);
    }

    public static String appendDateTimeToFileName(String filename, Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return String.format("%s_%s.%s", FilenameUtils.getBaseName(filename), formatter.format(date), FilenameUtils.getExtension(filename));
    }

    public static void saveFile(File file, byte[] bytesFile) throws IOException {
        OutputStream out = new FileOutputStream(file);

        out.write(bytesFile);

        out.flush();
        out.close();
    }

    public static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }

    public static File createDirIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return directory;
    }

    public static void unzipFile(String zipFile, String outputDirectory) throws FileNotFoundException, IOException {

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        ZipInputStream zip = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry = zip.getNextEntry();

        while (!ObjectUtils.isNullOrEmpty(entry)) {

            File zipItem = new File(outputDirectory + SYSTEM_FILE_SEPARATOR + entry.getName());

            if (entry.isDirectory()) {
                new File(zipItem.getCanonicalPath()).mkdirs();
            } else {

                int readed;
                FileOutputStream out = new FileOutputStream(zipItem);

                while ((readed = zip.read(buffer)) > 0) {
                    out.write(buffer, 0, readed);
                }

                out.close();
            }

            zip.closeEntry();
            entry = zip.getNextEntry();
        }

        zip.closeEntry();
        zip.close();
    }

    public static File renameFile(File old, String newFileName) throws IOException {
        File file = new File(old.getParent() + FileUtils.SYSTEM_FILE_SEPARATOR + newFileName);
        if (old.renameTo(file)) {
            return file;
        }

        throw new IOException(String.format("You can't rename the '%s' file to '%s'.", old.getAbsolutePath(), newFileName));
    }
}
