package br.gov.caixa.gitecsa.sired.arquitetura.service;

import java.util.List;
import java.util.Map;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;

public abstract class PaginatorService<T> extends AbstractService<T> {

    private static final long serialVersionUID = -8649152154540671461L;

    public abstract Integer count(Map<String, Object> filters) throws DataBaseException;
    
    public abstract List<T> pesquisar(Integer offset, Integer limit, Map<String, Object> filters) throws DataBaseException;
    
}
