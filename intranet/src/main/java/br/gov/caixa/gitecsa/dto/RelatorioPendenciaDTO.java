package br.gov.caixa.gitecsa.dto;

import java.util.Date;

import br.gov.caixa.gitecsa.sired.enumerator.TipoDemandaEnum;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;

public class RelatorioPendenciaDTO {

    private Date dataInicio;
    private Date DataFim;
    private BaseVO base;
    private TipoDemandaEnum tipoDemanada;

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return DataFim;
    }

    public void setDataFim(Date dataFim) {
        DataFim = dataFim;
    }

    public BaseVO getBase() {
        return base;
    }

    public void setBase(BaseVO base) {
        this.base = base;
    }

    public TipoDemandaEnum getTipoDemanada() {
        return tipoDemanada;
    }

    public void setTipoDemanada(TipoDemandaEnum tipoDemanada) {
        this.tipoDemanada = tipoDemanada;
    }

}
