package cl.cc.utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author CyberCastle
 */
public final class Util {
    
    public final static String DEFAULT_DATE_FORMAT = "dd-MM-yyyy";

    private Util() {
    }

    public static Date parseDate(String date, String format) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.parse(date);
    }

    public static String formatDate(Date date, String format) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static Date getBeforeDay() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.
        c.add(Calendar.DATE, -1); // Adding -1 days
        return c.getTime();
    }

    public static String ExceptionTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static void deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    } else {
                        f.delete();
                    }
                }
            }
            path.delete();
        }
    }

    public static String generateRandomText(String prefix, int len) {
        final SecureRandom rnd = new SecureRandom();
        rnd.setSeed(System.currentTimeMillis());
        byte[] buf = new byte[len * 2];
        char[] text = new char[len];
        int c = 0;
        while (c < text.length) {
            rnd.nextBytes(buf);
            for (byte b : buf) {
                if (b >= 'a' && b <= 'z') {
                    text[c++] = (char) b;
                    if (c >= text.length) {
                        break;
                    }
                }
            }
        }
        return prefix + new String(text);
    }
}
