package br.gov.caixa.gitecsa.visitor;

import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import br.gov.caixa.gitecsa.sired.util.FilterVisitor;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.SuporteVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;

public class RelatorioVisitor extends FilterVisitor {

    private Date dataInicio = Util.getDataMesPassado();
    private Date dataFim;
    private SuporteVO suporte;
    private BaseVO base;
    private TipoDemandaVO tipoDemanda;

    @Override
    public void visitCriteria(Criteria criteria) {
        criteria.add(Restrictions.between("dataHoraRegistro", dataInicio, dataFim));
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public BaseVO getBase() {
        return base;
    }

    public void setBase(BaseVO base) {
        this.base = base;
    }

    public SuporteVO getSuporte() {
        return suporte;
    }

    public void setSuporte(SuporteVO suporte) {
        this.suporte = suporte;
    }

    public TipoDemandaVO getTipoDemanda() {
        return tipoDemanda;
    }

    public void setTipoDemanda(TipoDemandaVO tipoDemanda) {
        this.tipoDemanda = tipoDemanda;
    }

}
