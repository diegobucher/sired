package br.gov.caixa.gitecsa.sired.extra.dao;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;

public interface GrupoDAO extends GenericDAO<GrupoVO> {

    GrupoVO getById(Object id);

    GrupoVO obterGrupo(DocumentoVO documento);

    List<TipoDemandaVO> obterDemandas();

}
