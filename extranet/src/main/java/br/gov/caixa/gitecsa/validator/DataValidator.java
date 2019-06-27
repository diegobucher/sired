package br.gov.caixa.gitecsa.validator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import br.gov.caixa.gitecsa.sired.util.Util;

@FacesValidator("validator.DataValidator")
public class DataValidator implements Validator {

    /*
     * @Override
     * public void validate(FacesContext arg0, UIComponent arg1, Object arg2) {
     * if (!Util.isNull(arg2) && !Util.isEmptyString(arg2.toString()) && !validaData(String.valueOf(arg2))) {
     * FacesMessage message = new FacesMessage();
     * message.setSeverity(FacesMessage.SEVERITY_ERROR);
     * message.setSummary("Intervalo de data inválido.");
     * refresh();
     * throw new ValidatorException(message);
     * }
     * }
     */

    public void refresh() {
        FacesContext context = FacesContext.getCurrentInstance();
        Application application = context.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot viewRoot = viewHandler.createView(context, context.getViewRoot().getViewId());
        context.setViewRoot(viewRoot);
        context.renderResponse();
    }

    /**
     * Valida Data digitada pelo usuario.
     * 
     * @param data
     *            String data com formato dd/MM/yyyy
     */
    public static boolean validaData(String data) {
        /*
         * if (data == null)
         * return false;
         * 
         * try {
         * 
         * return true;
         * } catch (SicppException e) {
         * return false;
         * }
         */

        return true;
    }

    private Matcher matcher;
    private Pattern pattern;
    private final int LEAP_STEP = 4;
    private String padrao = "dd/MM/yyyy";

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) {
        if (!Util.isNullOuVazio(value)) {
            String data = "";
            String[] lData = value.toString().split(" ");

            if (lData[lData.length - 1].length() <= 2) {
                padrao = "dd/MM/yy";
            }

            if (value instanceof Date) {
                SimpleDateFormat lSD = new SimpleDateFormat(padrao);
                data = lSD.format(value);
            }

            String DATE_REGEX = "([1-9]|0[1-9]|[1,2][0-9]|3[0,1])/(0[1-9]|1[0,1,2])/\\d{4}";

            /**
             * This date regex is similar to the first one, but with the
             * difference of matching only the whole string. So
             * "01-01-2000-12345" won't pass with a match. Keep in mind that
             * String.matches tries to match only the whole string.
             */
            String DATE_REGEX_ONLY_WHOLE_STRING = "^" + DATE_REGEX + "$";
            pattern = Pattern.compile(DATE_REGEX_ONLY_WHOLE_STRING);

            if (!check(data)) {
                FacesMessage message = new FacesMessage();
                message.setSeverity(FacesMessage.SEVERITY_ERROR);
                message.setSummary("O campo de Data deve ser um data válida.");
                throw new ValidatorException(message);
            }
        }
    }

    /**
     * Checks if the date format is a valid. Uses the regex pattern to match the
     * date first. Than additionally checks are performed on the boundaries of
     * the days taken the month into account (leap years are covered).
     * 
     * @param date
     *            the date that needs to be checked.
     * @return if the date is of an valid format or not.
     */
    public boolean check(final String date) {
        matcher = pattern.matcher(date);
        if (matcher.matches()) {
            matcher.reset();
            if (matcher.find()) {
                String[] dados = date.split("/");
                int day = Integer.parseInt(dados[0]);
                int month = Integer.parseInt(dados[1]);
                int year = Integer.parseInt(dados[2]);

                switch (month) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    return day < 32;
                case 4:
                case 6:
                case 9:
                case 11:
                    return day < 31;
                case 2:
                    int modulo100 = year % 100;
                    // http://science.howstuffworks.com/science-vs-myth/everyday-myths/question50.htm
                    if ((modulo100 == 0 && year % 400 == 0) || (modulo100 != 0 && year % LEAP_STEP == 0)) {
                        // its a leap year
                        return day < 30;
                    } else {
                        return day < 29;
                    }
                default:
                    break;
                }
            }
        }
        return false;
    }

    public String getRegex() {
        return pattern.pattern();
    }
}
