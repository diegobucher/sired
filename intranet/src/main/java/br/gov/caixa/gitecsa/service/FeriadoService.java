package br.gov.caixa.gitecsa.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Hours;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.FeriadoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@TransactionManagement(TransactionManagementType.BEAN)
@Stateless
public class FeriadoService extends AbstractService<FeriadoVO> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    @Override
    protected void validaCampos(FeriadoVO entity) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void validaRegras(FeriadoVO entity) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void validaRegrasExcluir(FeriadoVO entity) throws AppException {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("unchecked")
    public int diffHorasUteisNoPeriodo(Date date1, Date date2, UnidadeVO unidadeVO) throws AppException {
        Criteria criteria = getSession().createCriteria(FeriadoVO.class);
        criteria.add(Restrictions.between("data", date1, date2));
        criteria.add(Restrictions.or(
                Restrictions.isNull("uf"),
                Restrictions.or(Restrictions.and(Restrictions.eq("uf", unidadeVO.getUf()), Restrictions.isNull("municipio")),
                        Restrictions.eq("municipio", unidadeVO.getMunicipio()))));

        List<FeriadoVO> listFeriados = (List<FeriadoVO>) criteria.list();

        Calendar calInicio = Calendar.getInstance();
        calInicio.setTime(date1);

        Calendar calTermino = Calendar.getInstance();
        calTermino.setTime(date2);

        int diferenca = Hours.hoursBetween(new DateTime(date1), new DateTime(date2)).getHours();

        while (calInicio.before(calTermino)) {
            int diaDaSemana = calInicio.get(Calendar.DAY_OF_WEEK);
            if (diaDaSemana == 7 || diaDaSemana == 1) {
                diferenca = diferenca - 24;
            } else {
                for (FeriadoVO feriadoVO : listFeriados) {
                    Calendar tmp = Calendar.getInstance();
                    tmp.set(Calendar.YEAR, calInicio.get(Calendar.YEAR));
                    tmp.set(Calendar.MONTH, calInicio.get(Calendar.MONTH));
                    tmp.set(Calendar.DAY_OF_YEAR, calInicio.get(Calendar.DAY_OF_YEAR));

                    Calendar tmp2 = Calendar.getInstance();
                    tmp2.setTime(feriadoVO.getData());
                    if (DateTimeComparator.getDateOnlyInstance().compare(tmp, tmp2) == 0) {
                        diferenca = diferenca - 24;
                    }

                }
            }
            calInicio.add(Calendar.DAY_OF_MONTH, 1);
        }

        return diferenca;
    }

    @SuppressWarnings("unchecked")
    public Date proximaDataUtil(Date date1, int qtdeDias, UnidadeVO unidadeVO) throws AppException {

        int qtdeDiasUteis = 0;
        Calendar calInicio = Calendar.getInstance();
        calInicio.setTime(date1);

        while (qtdeDiasUteis < qtdeDias) {
            calInicio.add(Calendar.DAY_OF_MONTH, 1);
            int diaDaSemana = calInicio.get(Calendar.DAY_OF_WEEK);
            if (diaDaSemana != 7 && diaDaSemana != 1) {
                Criteria criteria = getSession().createCriteria(FeriadoVO.class);
                criteria.add(Restrictions.or(
                        Restrictions.isNull("uf"),
                        Restrictions.or(Restrictions.and(Restrictions.eq("uf", unidadeVO.getUf()), Restrictions.isNull("municipio")),
                                Restrictions.eq("municipio", unidadeVO.getMunicipio()))));
                criteria.add(Restrictions.eq("data", calInicio.getTime()));
                List<FeriadoVO> listFeriados = (List<FeriadoVO>) criteria.list();
                if (listFeriados.size() == 0) {
                    qtdeDiasUteis++;
                }
            }
        }

        return calInicio.getTime();
    }
}
