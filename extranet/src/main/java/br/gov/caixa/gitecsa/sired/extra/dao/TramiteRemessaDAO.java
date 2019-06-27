package br.gov.caixa.gitecsa.sired.extra.dao;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;

public interface TramiteRemessaDAO extends GenericDAO<TramiteRemessaVO>{

  public SituacaoRemessaVO buscarSituacaoRemessa(Long id);
  
  public  List<TramiteRemessaVO> findByRemessa(RemessaVO remessa) throws AppException;

}
