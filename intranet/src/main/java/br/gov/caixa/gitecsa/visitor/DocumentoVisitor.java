package br.gov.caixa.gitecsa.visitor;

import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import br.gov.caixa.gitecsa.sired.util.FilterVisitor;

public class DocumentoVisitor extends FilterVisitor {

    private Date periodoInicial;
    private Date periodofinal;

    @Override
    public void visitCriteria(Criteria criteria) {

        criteria.add(Restrictions.between("dataPrazo", periodoInicial, periodofinal));
    }

    public Date getPeriodoInicial() {
        return periodoInicial;
    }

    public void setPeriodoInicial(Date periodoInicial) {
        this.periodoInicial = periodoInicial;
    }

    public Date getPeriodofinal() {
        return periodofinal;
    }

    public void setPeriodofinal(Date periodofinal) {
        this.periodofinal = periodofinal;
    }

}
