package br.gov.caixa.gitecsa.sired.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class DateUtils {

    public static final String SHORT_YEAR = "yy";

    public static final int MILLISECONDS_IN_DAY = 86400000;

    public static final String DEFAULT_FORMAT = "dd/MM/yyyy";

    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm";

    public static String format(Date date, String format) {
        if (!ObjectUtils.isNullOrEmpty(date)) {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            return formatter.format(date);
        }

        return StringUtils.EMPTY;
    }

    public static int diff(Date d1, Date d2) {

        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        c1.set(Calendar.MILLISECOND, 0);
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.HOUR_OF_DAY, 0);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        c2.set(Calendar.MILLISECOND, 0);
        c2.set(Calendar.SECOND, 0);
        c2.set(Calendar.MINUTE, 0);
        c2.set(Calendar.HOUR_OF_DAY, 0);

        return (int) ((c1.getTimeInMillis() - c2.getTimeInMillis()) / MILLISECONDS_IN_DAY);
    }
    
    public static boolean ehDataMaior(Date d1, Date d2){
        return diff(d1, d2) > 0;
    }

    public static Date tryParse(final String date, final Date defaultDate) {
        return DateUtils.tryParse(date, defaultDate, DEFAULT_FORMAT);
    }

    public static Date tryParse(final String date, final Date defaultDate, final String format) {
        
    	SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setLenient(false);

        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return defaultDate;
        }
    }

    public static Integer getShortYear(Calendar date) {

        try {
            SimpleDateFormat format = new SimpleDateFormat(SHORT_YEAR);
            return Integer.valueOf(format.format(date.getTime()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Retorna uma data com o ultimo hora/minuto/segundo da data informada (23:59:59).
     * 
     * @param date
     * @return
     */
    public static Date fitAtEnd(Date date) {

        Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);

        return c.getTime();
    }

    /**
     * Retorna uma data com a primeira hora/minuto/segundo da data informada. Não há necessidade de definir a data, hora e segundo para 0, pois é o padrão.
     * 
     * @param date
     * @return
     */
    public static Date fitAtStart(Date date) {
        Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.setTime(date);
        return c.getTime();
    }
}
