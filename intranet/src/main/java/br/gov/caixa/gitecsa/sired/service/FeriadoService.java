package br.gov.caixa.gitecsa.sired.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.FeriadoDAO;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.FeriadoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Stateless(name = "feriadoSistemaService")
public class FeriadoService extends AbstractService<FeriadoVO> {

    private static final long serialVersionUID = -1134934438141116962L;

    @Inject
    private FeriadoDAO dao;

    @Override
    protected void validaCamposObrigatorios(FeriadoVO entity) {

    }

    @Override
    protected void validaRegras(FeriadoVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(FeriadoVO entity) {

    }

    @Override
    protected GenericDAO<FeriadoVO> getDAO() {
        return this.dao;
    }

    public Date findProximaDataUtil(Date data, Integer dias, UnidadeVO unidadeVO) throws DataBaseException {
        return this.dao.findProximaDataUtil(data, dias, unidadeVO);
    }

    public Integer getNumeroDiasUteis(Date d1, Date d2, UnidadeVO unidadeVO) throws DataBaseException {

        int dias = DateUtils.diff(d1, d2);

        List<FeriadoVO> listFeriados = this.dao.findByPeriodo(d1, d2, unidadeVO);

        if (!ObjectUtils.isNullOrEmpty(listFeriados)) {
            return dias - listFeriados.size();
        }

        return dias;
    }
    
    public Boolean isDataUtil(final Date data, final UnidadeVO unidadeVO) throws DataBaseException {
        
        Calendar dia = Calendar.getInstance();
        dia.setTime(data);
        int diaSemana = dia.get(Calendar.DAY_OF_WEEK);
        
        if (diaSemana == Calendar.SATURDAY || diaSemana == Calendar.SUNDAY || this.dao.isFeriado(data, unidadeVO)) {
            return false;
        }
        
        return true;
    }
}
