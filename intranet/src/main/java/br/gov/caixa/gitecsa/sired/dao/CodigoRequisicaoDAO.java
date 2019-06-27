package br.gov.caixa.gitecsa.sired.dao;

import javax.persistence.EntityManager;

import org.hibernate.Session;

import br.gov.caixa.gitecsa.sired.dto.CodigoRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public interface CodigoRequisicaoDAO {
    Session getSession();

    EntityManager getEntityManager();

    CodigoRequisicaoDTO findByUnidadeAno(UnidadeVO unidade, Integer ano);

    void insert(CodigoRequisicaoDTO codigoRequisicao);

    void update(CodigoRequisicaoDTO codigoRequisicao);
}
