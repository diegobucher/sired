package br.gov.caixa.gitecsa.sired.dao;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.MunicipioVO;

public interface MunicipioDAO extends GenericDAO<MunicipioVO> {
    
    void update(List<MunicipioVO> listMunicipio);

    void persist(List<MunicipioVO> listMunicipio);
}
