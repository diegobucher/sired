package br.gov.caixa.gitecsa.sired.dao.impl;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.dao.MunicipioDAO;
import br.gov.caixa.gitecsa.sired.vo.MunicipioVO;

public class MunicipioDAOImpl extends GenericDAOImpl<MunicipioVO> implements MunicipioDAO {
    
    @Override
    public void update(List<MunicipioVO> listMunicipio) {

        int contador = 1;
        for (MunicipioVO entity : listMunicipio) {
            this.getEntityManager().merge(entity);

            if (contador % 100 == 0) {
                this.getEntityManager().flush();
                this.getEntityManager().clear();
            }
            contador++;
        }
    }
    
    @Override
    public void persist(List<MunicipioVO> listMunicipio) {

        int contador = 1;
        for (MunicipioVO entity : listMunicipio) {
            this.getEntityManager().persist(entity);

            if (contador % 100 == 0) {
                this.getEntityManager().flush();
                this.getEntityManager().clear();
            }
            contador++;
        }
    }
}
