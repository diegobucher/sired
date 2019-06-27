package br.gov.caixa.gitecsa.sired.arquitetura.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import br.gov.caixa.gitecsa.sired.arquitetura.service.PaginatorService;
import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;

/**
 * Realiza a consulta utilizando paginação real.
 * 
 * @param <T>
 */
public class PaginatorModel<T extends BaseEntity> {
    
    private LazyDataModel<T> listModel;
    
    private PaginatorService<T> service;
    
    private Map<String, Object> customFilters;
    
    private List<T> list;
    
    private transient Logger logger = LogUtils.getLogger(getClass().getName()); 
    
    public PaginatorModel(PaginatorService<T> paginatorService, Map<String, Object> filters) {
        this.service = paginatorService;
        this.customFilters = filters;
        
        listModel = new LazyDataModel<T>() {

            private static final long serialVersionUID = -9078638200886309082L;
            
            @Override
            public List<T> load(int offset, int limit, String sortField, SortOrder sortOrder, Map<String, String> filters) {
                
                list = null;
                
                if (limit != 0) {
                    // O atributo customFilters foi utilizado para contornar a limitação do Map utilizado pelo LazyDataModel, que permite apenas
                    // um Map de Strings. Essa limitação foi solucionada no PrimeFaces 5.0, quando a assinatura do método load foi alterado
                    // para aceitar Map<String, Object>.
                    try {
                        list = service.pesquisar(Integer.valueOf(offset), Integer.valueOf(limit), customFilters);
                        setPageSize(limit);
                        setRowCount(service.count(customFilters));
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, MensagemUtils.obterMensagem("MA012"), null));
                    }
                    
                } else if (offset == 0 && limit == 0) {
                    // Workaround para bug no LazyDataModel do Primefaces 4. O método load é chamado duas vezes na primeira chamada da consulta.
                    // Isso evita que ele faça uma consulta desnecessária, 
                    list = new ArrayList<T>();
                    setRowCount(1);
                }
                
                return list;
            }
        };
    }
   
    public LazyDataModel<T> getListModel() {
        return listModel;
    }
    
    public List<T> getList() {
        return list;
    }

}
