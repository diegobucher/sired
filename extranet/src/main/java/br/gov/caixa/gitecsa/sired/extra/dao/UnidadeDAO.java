package br.gov.caixa.gitecsa.sired.extra.dao;

import java.util.List;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public interface UnidadeDAO extends GenericDAO<UnidadeVO> {

    UnidadeVO findByIdEager(Long id);

    List<UnidadeVO> findAllByPerfil(UsuarioLdap usuario);

    List<UnidadeVO> findAllComBase();

    List<UnidadeVO> findAllByUnidadeVinculadora(UnidadeVO unidade);

    UnidadeVO findByUnidadeVinculadora(UnidadeVO unidade, UnidadeVO unidadeUsuario);

    UnidadeVO findByAbrangenciaCtda(UnidadeVO unidade, UnidadeVO unidadeUsuario);

    UnidadeVO findByAbrangenciaUf(UnidadeVO unidadeUsuario);

}
