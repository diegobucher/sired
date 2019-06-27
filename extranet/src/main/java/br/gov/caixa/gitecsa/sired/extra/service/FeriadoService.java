package br.gov.caixa.gitecsa.sired.extra.service;

import java.util.Calendar;
import java.util.Date;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.extra.dao.FeriadoDAO;
import br.gov.caixa.gitecsa.sired.vo.FeriadoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Stateless(name = "ExtraFeriadoService")
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
    
    public Boolean isDataUtil(final Date data, final UnidadeVO unidadeVO) {
        
        Calendar dia = Calendar.getInstance();
        dia.setTime(data);
        int diaSemana = dia.get(Calendar.DAY_OF_WEEK);
        
        if (diaSemana == Calendar.SATURDAY || diaSemana == Calendar.SUNDAY || this.dao.isFeriado(data, unidadeVO)) {
            return false;
        }
        
        return true;
    }
}
