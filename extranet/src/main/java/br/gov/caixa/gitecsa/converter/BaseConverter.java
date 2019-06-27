package br.gov.caixa.gitecsa.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.service.BaseService;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;

@FacesConverter("BaseConverter")
public class BaseConverter implements Converter{
	
	@Inject
	private BaseService service;

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		
		try {
			return service.getById(Long.parseLong(arg2));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		if (arg2 instanceof BaseVO) {
			BaseVO obj = (BaseVO) arg2;
			return obj.getId().toString();
		}
		return null;
	}


}
