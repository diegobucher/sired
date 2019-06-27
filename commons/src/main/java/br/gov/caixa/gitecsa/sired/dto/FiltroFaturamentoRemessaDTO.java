package br.gov.caixa.gitecsa.sired.dto;

import java.util.Date;

import br.gov.caixa.gitecsa.sired.enumerator.TipoRelatorioFaturamentoEnum;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;


public class FiltroFaturamentoRemessaDTO {

    private Date dataInicio;

    private Date dataFim;
    
    private TipoRelatorioFaturamentoEnum tipo;

    private BaseVO base;
    
    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(final Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(final Date dataFim) {
        this.dataFim = dataFim;
    }
    
    public TipoRelatorioFaturamentoEnum getTipo() {
        return tipo;
    }

    public void setTipo(TipoRelatorioFaturamentoEnum tipo) {
        this.tipo = tipo;
    }

    public BaseVO getBase() {
        return base;
    }

    public void setBase(final BaseVO base) {
        this.base = base;
    }

}
