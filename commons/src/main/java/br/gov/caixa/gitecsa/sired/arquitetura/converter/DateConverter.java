package br.gov.caixa.gitecsa.sired.arquitetura.converter;

import java.text.SimpleDateFormat;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.convert.DateTimeConverter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.util.MensagemUtils;

@FacesConverter("br.gov.caixa.gitecsa.DateConverter")
public class DateConverter extends DateTimeConverter {

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    private static final String DEFAULT_ERROR_MESSAGE = "O campo Data deve conter uma data válida.";

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {

        Object obj = null;
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        format.setLenient(false);

        try {

            if (StringUtils.isNotBlank(value.toString())) {
                obj = format.parse(value.toString());
            }

        } catch (Exception e) {

            FacesMessage msg = null;
            if (component.getAttributes().get("field") != null) {
                msg = new FacesMessage(MensagemUtils.obterMensagem("MI014", component.getAttributes().get("field")));
            } else {
                msg = new FacesMessage(DEFAULT_ERROR_MESSAGE);
            }

            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }

        return obj;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {

        if (value == null) {
            return StringUtils.EMPTY;
        }

        if (value instanceof String) {
            return (String) value;
        }

        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        formatter.setLenient(false);

        return (formatter.format(value));
    }
}
