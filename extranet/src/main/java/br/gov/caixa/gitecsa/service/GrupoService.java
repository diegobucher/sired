package br.gov.caixa.gitecsa.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.extra.util.ConstsSiredExtra;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;

@Deprecated
// @Stateless
public class GrupoService extends AbstractService<GrupoVO> {

    private static final long serialVersionUID = 1L;

    @Override
    protected void validaCampos(GrupoVO entity) {
    }

    @Override
    protected void validaRegras(GrupoVO entity) {
        duplicidade(entity);
    }

    private void duplicidade(GrupoVO entity) {
        List<GrupoVO> lista = findAll();
        for (GrupoVO obj : lista) {
            if (entity.getId() == null) {
                if (obj.getNome().equalsIgnoreCase(entity.getNome().trim())) {
                    mensagens.add(MensagemUtils.obterMensagem(ConstsSiredExtra.INFORMACAO_CRUD_DUPLICIDADE));
                    break;
                }
            } else {
                if ((!obj.getId().equals(entity.getId())) && obj.getNome().trim().equalsIgnoreCase(entity.getNome().trim())) {
                    mensagens.add(MensagemUtils.obterMensagem(ConstsSiredExtra.INFORMACAO_CRUD_DUPLICIDADE));
                    break;
                }

            }
        }
    }

    @Override
    protected void validaRegrasExcluir(GrupoVO entity) {
    }

    public void salvarListaCampos(GrupoVO instance) throws AppException {
        super.updateImpl(instance);
    }

    @Override
    public GrupoVO getById(Object id) {
        try {
            Criteria criteria = getSession().createCriteria(GrupoVO.class).createAlias("grupoCampos", "campos", JoinType.LEFT_OUTER_JOIN);

            criteria.add(Restrictions.eq("id", id));

            criteria.addOrder(Order.desc("nome"));

            GrupoVO obj = (GrupoVO) criteria.uniqueResult();

            if (obj != null) {
                obj.getGrupoCampos().toArray();
            }

            return obj;

        } catch (Exception e) {
            mensagens.add(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "getById", "GrupoVO"));
        }

        return null;
    }

    public GrupoVO obterGrupo(DocumentoVO documento) {
        try {
            Criteria criteria = getSession().createCriteria(GrupoVO.class).createAlias("documentos", "docs", JoinType.INNER_JOIN);
            criteria.add(Restrictions.eq("docs.id", documento.getId()));

            GrupoVO obj = (GrupoVO) criteria.uniqueResult();

            if (obj != null) {
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
            }

            return obj;

        } catch (Exception e) {
            mensagens.add(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "findAll", "Grupo"));
        }
        return null;

    }
    
    public GrupoVO obterGrupoAlterados(DocumentoVO documento) {

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

    @SuppressWarnings("unchecked")
    public List<TipoDemandaVO> obterDemandas() {

        try {

            Criteria criteria = getSession().createCriteria(TipoDemandaVO.class);

            List<TipoDemandaVO> lista = new ArrayList<TipoDemandaVO>();

            lista = criteria.list();

            return lista;

        } catch (Exception e) {
            mensagens.add(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "TipoDemanda", "obterDemandas"));
        }
        return null;

    }

    protected EntityManager getEntityManager() {
      return this.entityManager;
  }
}
