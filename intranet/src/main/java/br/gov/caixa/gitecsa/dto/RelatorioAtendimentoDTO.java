package br.gov.caixa.gitecsa.dto;

import java.util.Date;

import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;

public class RelatorioAtendimentoDTO {

    private Date dataInicio = Util.getDataMesPassado();
    private Date DataFim;
    private BaseVO base;
    private TipoDemandaVO tipoDemanada;

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

    public TipoDemandaVO getTipoDemanada() {
        return tipoDemanada;
    }

    public void setTipoDemanada(TipoDemandaVO tipoDemanada) {
        this.tipoDemanada = tipoDemanada;
    }

}
