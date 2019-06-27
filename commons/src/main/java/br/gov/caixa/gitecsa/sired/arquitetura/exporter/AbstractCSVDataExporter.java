package br.gov.caixa.gitecsa.sired.arquitetura.exporter;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;

public abstract class AbstractCSVDataExporter<T extends BaseEntity> implements DataExportable<T> {
    
    private List<T> data;
    
    public abstract void createContent() throws AppException;
    
    @Override
    public List<T> getData() {
        return data;
    }
    
    @Override
    public void setData(List<T> data) {
        this.data = data;
    }
    
}
