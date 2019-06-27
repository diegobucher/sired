package br.gov.caixa.gitecsa.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.BooleanUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSolicitacaoEnum;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.CampoVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoPK;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeGrupoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Stateless
public class GrupoService extends AbstractService<GrupoVO> {

    private static final long serialVersionUID = 1L;

    @Inject
    private UnidadeService unidadeService;

    @Inject
    private DocumentoService serviceDocumento;

    @Override
    protected void validaCampos(GrupoVO entity) {
    }

    @Override
    protected void validaRegras(GrupoVO entity) {
        duplicidadeGrupoByNome(entity);

        garanteUnicidadeDocumentoByGrupo(entity);
    }

    private void garanteUnicidadeDocumentoByGrupo(GrupoVO entity) {
        List<DocumentoVO> documentosAssociadoSomeGroup = serviceDocumento.findAssociadoGrupo();
        for (DocumentoVO documentoEntity : entity.getDocumentos()) {

            for (DocumentoVO documentoAssociadoSomeGroup : documentosAssociadoSomeGroup) {
                if (documentoAssociadoSomeGroup.getId().equals(documentoEntity.getId())) {
                    if (entity.getId() == null) {
                        mensagens.add(MensagemUtils.obterMensagem("MI024", documentoAssociadoSomeGroup.getGrupo().getNome()));
                        break;
                    } else {
                        GrupoVO grupoAssociado = null;
                        if ((!documentoAssociadoSomeGroup.getGrupo().getId().equals(entity.getId()))) {
                            grupoAssociado = documentoAssociadoSomeGroup.getGrupo();
                        }

                        if (grupoAssociado != null) {
                            mensagens.add(MensagemUtils.obterMensagem("MI024", grupoAssociado.getNome()));
                            break;
                        }
                    }
                }
            }
        }
    }

    public List<GrupoVO> findByNomeETipoSolicitacao(String nome, TipoSolicitacaoEnum tipoSolicitacao) {
        StringBuilder hql = new StringBuilder("");
        hql.append(" SELECT g ");
        hql.append(" FROM GrupoVO g ");
        hql.append(" Where g.nome = :nome ");
        hql.append(" AND g.tipoSolicitacao = :tipoSolicitacao ");

        TypedQuery<GrupoVO> query = getEntityManager().createQuery(hql.toString(), GrupoVO.class);
        query.setParameter("nome", nome);
        query.setParameter("tipoSolicitacao", tipoSolicitacao);

        return query.getResultList();
    }

    /**
     * Grupos são considerados iguais caso tenham o mesmo nome para o mesmo tipo de solicitação.
     * 
     * @param entity
     *            entidade sendo validada.
     */
    private void duplicidadeGrupoByNome(GrupoVO entity) {
        List<GrupoVO> lista = findByNomeETipoSolicitacao(entity.getNome(), entity.getTipoSolicitacao());
        for (GrupoVO grupoVO : lista) {
            if (entity.getId() == null) {
                mensagens.add(MensagemUtils.obterMensagem("MI018"));
                break;
            } else if (!grupoVO.getId().equals(entity.getId())) {
                mensagens.add(MensagemUtils.obterMensagem("MI018"));
                break;
            }
        }
    }

    @Override
    protected void validaRegrasExcluir(GrupoVO entity) throws AppException {
        List<UnidadeVO> unidades = relacionamentoAreaMeio(entity);

        if (unidades != null && unidades.size() > 0) {
            mensagens.add(MensagemUtils.obterMensagem("MA006"));
        }
    }

    public GrupoVO obterGrupo(DocumentoVO documento) {

        try {

            StringBuilder hql = new StringBuilder();
            hql.append(" SELECT g ");
            hql.append(" FROM GrupoVO g ");
            hql.append(" Join g.documentos doc ");
            hql.append(" Join Fetch g.grupoCampos gc ");
            hql.append(" Join Fetch gc.campo ");
            hql.append(" Where doc.id = :id ");

            Query query = getEntityManager().createQuery(hql.toString(), GrupoVO.class);
            query.setParameter("id", documento.getId());

            GrupoVO obj = null;

            try {
                obj = (GrupoVO) query.getSingleResult();
            } catch (NoResultException e) {
                Logger logger = LogUtils.getLogger(this.getClass().getName());
                logger.error(e.getMessage(), e);

                return null;
            }

            List<GrupoCampoVO> list = new ArrayList<GrupoCampoVO>(obj.getGrupoCampos());

            Collections.sort(list, new Comparator<GrupoCampoVO>() {

                @Override
                public int compare(GrupoCampoVO arquivo1, GrupoCampoVO arquivo2) {

                    if (Util.isNullOuVazio(arquivo1.getOrdem())) {
                        arquivo1.setOrdem(50);
                    }
                    if (Util.isNullOuVazio(arquivo2.getOrdem())) {
                        arquivo2.setOrdem(50);
                    }

                    return arquivo1.getOrdem().compareTo(arquivo2.getOrdem());
                }

            });

            obj.setGrupoCampos(new HashSet<GrupoCampoVO>(list));

            return obj;

        } catch (Exception e) {
            mensagens.add(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "findAll", "FormularioRequisicao"));

            return null;
        }
    }

    public List<GrupoVO> getGruposByUnidadeGrupo(Set<UnidadeGrupoVO> unidadeGrupos) {

        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT g ");
        hql.append(" FROM GrupoVO g ");
        hql.append(" JOIN g.unidadeGrupos ug ");
        hql.append(" Where ug in ( :unidadeGrupos ) ");

        TypedQuery<GrupoVO> query = getEntityManager().createQuery(hql.toString(), GrupoVO.class);

        query.setParameter("unidadeGrupos", unidadeGrupos);

        return query.getResultList();
    }

    private List<UnidadeVO> relacionamentoAreaMeio(GrupoVO entity) throws AppException {
        List<UnidadeVO> unidades = unidadeService.findByGrupo(entity);

        return unidades;
    }

    @SuppressWarnings("unchecked")
    public List<GrupoVO> findGrupoAssociadoDocumento(DocumentoVO entity) throws AppException {
        Criteria criteria = getSession().createCriteria(GrupoVO.class).createAlias("documentos", "documentos", JoinType.INNER_JOIN);
        criteria.add(Restrictions.eq("documentos.id", entity.getId()));
        return (List<GrupoVO>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<CampoVO> findTodosCampo(GrupoVO grupo) throws AppException {

        Criteria criteria = getSession().createCriteria(CampoVO.class);

        List<TipoSolicitacaoEnum> listTipoSolicitacao = new ArrayList<TipoSolicitacaoEnum>();
        listTipoSolicitacao.add(TipoSolicitacaoEnum.REQUISICAO_REMESSA);

        if (TipoSolicitacaoEnum.REQUISICAO.equals(grupo.getTipoSolicitacao())) {
            listTipoSolicitacao.add(TipoSolicitacaoEnum.REQUISICAO);
        } else {
            listTipoSolicitacao.add(TipoSolicitacaoEnum.REMESSA);
        }

        criteria.add(Restrictions.in("tipoSolicitacao", listTipoSolicitacao));
        List<CampoVO> listaCampos = criteria.list();

        criteria = getSession().createCriteria(GrupoCampoVO.class);
        criteria.add(Restrictions.eq("id.idGrupo", grupo.getId()));

        List<GrupoCampoVO> grupoCampos = criteria.list();

        for (GrupoCampoVO grupoCampo : grupoCampos) {
            for (CampoVO campo : listaCampos) {
                if (grupoCampo.getCampo().getId().equals(campo.getId())) {
                    campo.setCheckBox(true);
                    campo.setLegenda(grupoCampo.getLegenda());
                    campo.setMensagem(grupoCampo.getMensagem());
                    campo.setObrigatorio(SimNaoEnum.SIM.equals(grupoCampo.getCampoObrigatorio()));
                    campo.setOrdem(grupoCampo.getOrdem());
                }
            }
        }

        return listaCampos;
    }

    public void salvarCamposVinculados(GrupoVO grupo, List<CampoVO> listaCampos) {
        
        excluirCamposVinculados(grupo);

        for (CampoVO campo : listaCampos) {
            if (campo.isCheckBox()) {
                GrupoCampoPK pk = new GrupoCampoPK();
                pk.setIdCampo(campo.getId());
                pk.setIdGrupo(grupo.getId());

                GrupoCampoVO grupoCampo = new GrupoCampoVO();
                grupoCampo.setId(pk);
                grupoCampo.setCampo(campo);
                grupoCampo.setLegenda(campo.getLegenda());
                grupoCampo.setMensagem(campo.getMensagem());
                grupoCampo.setObrigatorio(BooleanUtils.toBooleanDefaultIfNull(campo.isObrigatorio(), false));
                grupoCampo.setCampoObrigatorio(campo.isObrigatorio() ? SimNaoEnum.SIM : SimNaoEnum.NAO);
                grupoCampo.setOrdem(campo.getOrdem());

                getSession().save(grupoCampo);
            }
        }
        
        atualizarVersaoFormulario(grupo);
    }
    
    private void atualizarVersaoFormulario(GrupoVO grupo) {
        
        StringBuilder hql = new StringBuilder("UPDATE GrupoVO SET");
        hql.append(" codigoUsuarioAlteracao = :matricula, ");
        hql.append(" dtUltimaAlteracao = :data, ");
        hql.append(" versaoFormulario = versaoFormulario + 1 ");
        hql.append(" WHERE id = :id AND versaoFormulario = :versao ");
        
        Query query = entityManager.createQuery(hql.toString());
                
		UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
		query.setParameter("matricula", usuario.getNuMatricula());
		query.setParameter("data", new Date());
		query.setParameter("id", grupo.getId());
		query.setParameter("versao", grupo.getVersaoFormulario());
        
        int retorno = query.executeUpdate();
        if (retorno == 0) {
            throw new OptimisticLockException();
        }
    }

    @SuppressWarnings("unchecked")
    public void excluirCamposVinculados(GrupoVO grupo) {
        Criteria criteria = getSession().createCriteria(GrupoCampoVO.class);
        criteria.add(Restrictions.eq("id.idGrupo", grupo.getId()));
        List<GrupoCampoVO> grupoCampos = criteria.list();

        for (GrupoCampoVO grupoCampo : grupoCampos) {
            getSession().delete(grupoCampo);
        }
    }

    public List<GrupoVO> findAllEager() {
        StringBuilder hql = new StringBuilder("");
        hql.append(" Select g From GrupoVO g ");
        hql.append(" Join Fetch g.grupoCampos gc ");
        hql.append(" Join Fetch gc.campo ");
        hql.append(" Join Fetch g.unidadeGrupos ug ");

        TypedQuery<GrupoVO> query = getEntityManager().createQuery(hql.toString(), GrupoVO.class);

        return query.getResultList();
    }

    public void saveGrupoDocumento(GrupoVO grupo) throws AppException {
        this.update(grupo);
    }

    public UnidadeService getUnidadeService() {
        return unidadeService;
    }

    public void setUnidadeService(UnidadeService unidadeService) {
        this.unidadeService = unidadeService;
    }

    public DocumentoService getServiceDocumento() {
        return serviceDocumento;
    }

    public void setServiceDocumento(DocumentoService serviceDocumento) {
        this.serviceDocumento = serviceDocumento;
    }

}
