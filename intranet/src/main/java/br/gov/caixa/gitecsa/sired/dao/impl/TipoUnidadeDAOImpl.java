package br.gov.caixa.gitecsa.sired.dao.impl;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.dao.TipoUnidadeDAO;
import br.gov.caixa.gitecsa.sired.vo.TipoUnidadeVO;

public class TipoUnidadeDAOImpl extends GenericDAOImpl<TipoUnidadeVO> implements TipoUnidadeDAO {

    @Override
    public void update(List<TipoUnidadeVO> listTipoUnidade) {

        int contador = 1;
        for (TipoUnidadeVO entity : listTipoUnidade) {
            this.getEntityManager().merge(entity);

            if (contador % 100 == 0) {
                this.getEntityManager().flush();
                this.getEntityManager().clear();
            }
            contador++;
        }
    }
    
    @Override
    public void persist(List<TipoUnidadeVO> listTipoUnidade) {

        int contador = 1;
        for (TipoUnidadeVO entity : listTipoUnidade) {
            this.getEntityManager().persist(entity);

            if (contador % 100 == 0) {
                this.getEntityManager().flush();
                this.getEntityManager().clear();
            }
            contador++;
        }
    }
}
