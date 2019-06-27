package br.gov.caixa.gitecsa.sired.extra.dto;

import java.util.Date;

import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;

public class RelatorioSuporteDTO {

    private Date dataInicio;
    private Date DataFim;
    private BaseVO base;
    private TipoDemandaVO tipoDemanda;

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

    public TipoDemandaVO getTipoDemanda() {
        return tipoDemanda;
    }

    public void setTipoDemanda(TipoDemandaVO tipoDemanda) {
        this.tipoDemanda = tipoDemanda;
    }

}
