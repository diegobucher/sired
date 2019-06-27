package br.gov.caixa.gitecsa.sired.vo;

import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import br.gov.caixa.gitecsa.sired.util.FilterVisitor;

public class TramiteAtendimentoRequisicaoVisitor extends FilterVisitor {
    private Date dataInicio;
    private Date dataFim;

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
}
