package br.gov.caixa.gitecsa.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.service.FuncionalidadeService;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.vo.FuncionalidadeVO;

@FacesConverter("FuncionalidadeConverter")
public class FuncionalidadeConverter implements Converter {

    @Inject
    private FuncionalidadeService service;

    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {

        try {
            
            return service.getById(Long.valueOf(arg2));
            
        } catch (Exception e) {
            
            Logger logger = LogUtils.getLogger(this.getClass().getName());
            logger.error(e.getMessage(), e);

            throw new ConverterException("Funcionalidade inválida");
        }
    }

    @Override
    public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
        if (arg2 instanceof FuncionalidadeVO) {
            FuncionalidadeVO obj = (FuncionalidadeVO) arg2;
            return obj.getId().toString();
        }
        return null;
    }

}
