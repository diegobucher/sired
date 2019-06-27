package br.gov.caixa.gitecsa.service;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.repository.DataRepository;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Deprecated
//@Stateless
public class TramiteRemessaService extends AbstractService<TramiteRemessaVO> {

    private static final long serialVersionUID = 1L;

    @Inject
    @DataRepository
    private EntityManager entityManagerSistema;

    @SuppressWarnings("unchecked")
    public void excluirByRemessa(RemessaVO remessaVO) throws AppException {
        Criteria criteria = getSession().createCriteria(TramiteRemessaVO.class, "tramite");
        criteria.add(Restrictions.eq("tramite.remessa.id", remessaVO.getId()));
        List<TramiteRemessaVO> list = (List<TramiteRemessaVO>) criteria.list();
        for (TramiteRemessaVO tramiteRemessaVO : list) {
            super.delete(tramiteRemessaVO);
        }
        remessaVO.setTramiteRemessas(null);

    }

    /**
     * Consulta os trâmites de determina remessa.
     * 
     * @param tramite
     * @return
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    public List<TramiteRemessaVO> findByRemessa(RemessaVO remessa) throws AppException {

        StringBuilder hql = new StringBuilder();
        hql.append("SELECT tramite FROM TramiteRemessaVO tramite ");
        hql.append("JOIN FETCH tramite.situacao ");
        hql.append("WHERE tramite.remessa.id = " + remessa.getId());
        hql.append(" ORDER BY tramite.dataTramiteRemessa");

        Query query = entityManagerSistema.createQuery(hql.toString(), TramiteRemessaVO.class);

        return query.getResultList();
    }

    @Override
    protected void validaCampos(TramiteRemessaVO entity) {

    }

    @Override
    protected void validaRegras(TramiteRemessaVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(TramiteRemessaVO entity) throws AppException {

    }

    public TramiteRemessaVO salvarTramiteRemessaAlterada(RemessaVO remessaVO) throws AppException {
      return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.ALTERADA.getId()));
    }
    
    public TramiteRemessaVO salvarTramiteRemessaEmAlteracao(RemessaVO remessaVO) throws AppException {
      return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.EM_ALTERACAO.getId()));
    }
    
    public TramiteRemessaVO salvarTramiteRemessaRecebida(RemessaVO remessaVO) throws AppException {
      return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.RECEBIDA.getId()));
    }
    
    public TramiteRemessaVO salvarTramiteRemessaConferida(RemessaVO remessaVO) throws AppException {
      return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.CONFERIDA.getId()));
    }
    
    private SituacaoRemessaVO buscarSituacaoRemessa(Long id) {
      Criteria criteria = getSession().createCriteria(SituacaoRemessaVO.class);
      criteria.add(Restrictions.eq("id", id));
      return (SituacaoRemessaVO) criteria.uniqueResult();
    }
    
    private TramiteRemessaVO salvarTramiteRemesssa(RemessaVO remessaVO, SituacaoRemessaVO situacao) throws AppException {
      TramiteRemessaVO vo = new TramiteRemessaVO();
      vo.setCodigoUsuario(JSFUtil.getUsuario().getEmail());
      vo.setDataTramiteRemessa(new Date());
      vo.setRemessa(remessaVO);
      vo.setSituacao(situacao);
      return this.save(vo);
    }
    
    public TramiteRemessaVO salvarTramiteRemessaRascunho(RemessaVO remessaVO) throws AppException {
      return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.RASCUNHO.getId()));
  }
}
