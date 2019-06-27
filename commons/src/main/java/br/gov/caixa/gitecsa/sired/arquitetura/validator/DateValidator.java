package br.gov.caixa.gitecsa.sired.arquitetura.validator;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.util.MensagemUtils;

@FacesValidator(value = "dateValidator")
public class DateValidator implements Validator {

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    private static final String DEFAULT_ERROR_MESSAGE = "O campo Data deve conter uma data válida";

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        format.setLenient(false);

        try {
            if (value instanceof Date && value != null) {
                format.format(value);
            } else if (value instanceof String && StringUtils.isNotBlank(value.toString())) {
                format.parse(value.toString());
            }

        } catch (Exception e) {

            FacesMessage msg = null;
            if (component.getAttributes().get("field") != null) {
                msg = new FacesMessage(MensagemUtils.obterMensagem("MI014", component.getAttributes().get("field")));
            } else {
                msg = new FacesMessage(DEFAULT_ERROR_MESSAGE);
            }

            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }

}
