package br.gov.caixa.gitecsa.sired.dao;

import javax.persistence.EntityManager;

import org.hibernate.Session;

import br.gov.caixa.gitecsa.sired.dto.CodigoRemessaDTO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public interface CodigoRemessaDAO {

  Session getSession();

  EntityManager getEntityManager();

  CodigoRemessaDTO findByUnidadeAno(UnidadeVO unidade, Integer ano);

  void insert(CodigoRemessaDTO codigoRemessa);
  void update(CodigoRemessaDTO codigoRemessa);
}
