package br.gov.caixa.gitecsa.sired.dao;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.AuditoriaVO;
import br.gov.caixa.gitecsa.sired.vo.TipoAuditoriaVO;

public interface AuditoriaDAO extends GenericDAO<AuditoriaVO> {

	TipoAuditoriaVO findTipoAuditoriaById(Integer id);

	List<AuditoriaVO> findAllByIdentificador(Integer identificador);
    
}
