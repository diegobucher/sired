package br.gov.caixa.gitecsa.sired.dao;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public interface UnidadeDAO extends GenericDAO<UnidadeVO> {
    UnidadeVO findByIdEager(Long id) throws DataBaseException;

    UnidadeVO findByUnidadeVinculadora(UnidadeVO unidade, UnidadeVO unidadeVinculadora) throws DataBaseException;

    UnidadeVO findByAbrangenciaUf(UnidadeVO unidadeAreaMeio) throws DataBaseException;

    UnidadeVO findByAbrangenciaCtda(UnidadeVO unidade, UnidadeVO unidadeCtda) throws DataBaseException;

    List<UnidadeVO> findAllComBase() throws DataBaseException;

    List<UnidadeVO> findAllByUnidadeVinculadora(UnidadeVO unidade) throws DataBaseException;

    void update(List<UnidadeVO> listUnidade);

    void persist(List<UnidadeVO> listUnidade);

    List<UnidadeVO> findAllByEager() throws DataBaseException;

    Boolean hasRestricaoUnidadeGrupoConfigurada(UnidadeVO unidade) throws DataBaseException;

    Boolean hasRestricaoUnidadeConfigurada(UnidadeVO unidade) throws DataBaseException;

    Boolean hasPermissaoDocumentoUnidade(DocumentoVO documento, UnidadeVO unidade) throws DataBaseException;
}
