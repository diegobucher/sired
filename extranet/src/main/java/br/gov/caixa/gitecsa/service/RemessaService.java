package br.gov.caixa.gitecsa.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.repository.DataRepository;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.dto.MovimentoDiarioRemessaCDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.extra.dao.RemessaDAO;
import br.gov.caixa.gitecsa.sired.extra.service.SequencialRemessaService;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Stateless
public class RemessaService extends AbstractService<RemessaVO> {

    private static final long serialVersionUID = 1L;

    @Inject
    @DataRepository
    private EntityManager entityManagerSistema;
    
    @Inject
    private RemessaDAO remessaDAO;
    
    @Inject
    private br.gov.caixa.gitecsa.sired.extra.service.TramiteRemessaService tramiteRemessaService;
    
    @Inject
    private SequencialRemessaService sequencialRemessaService;

    @Override
    protected void validaCampos(RemessaVO entity) {

    }

    @Override
    protected void validaRegras(RemessaVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(RemessaVO entity) {

    }

    public List<RemessaVO> consultarRemessa(Long numeroRemessa, Long numeroLacre, Date dataInicio, Date dataTermino, String emailUsuario, UnidadeVO unidade,
            SituacaoRemessaEnum situacao) {
        return consultarRemessa(numeroRemessa, numeroLacre, dataInicio, dataTermino, emailUsuario, unidade, situacao, -1, -1);
    }

    @SuppressWarnings({ "unchecked" })
    public List<RemessaVO> consultarRemessa(Long numeroRemessa, Long numeroLacre, Date dataInicio, Date dataTermino, String emailUsuario, UnidadeVO unidade,
            SituacaoRemessaEnum situacao, int inicio, int fim) {

        Criteria criteria = getSession().createCriteria(RemessaVO.class, "this");
        criteria.createAlias("unidadeSolicitante", "unidade", JoinType.INNER_JOIN);
        criteria.createAlias("empresaContrato", "contrato", JoinType.LEFT_OUTER_JOIN);
        criteria.createAlias("contrato.base", "base", JoinType.LEFT_OUTER_JOIN);
        criteria.createAlias("contrato.empresa", "empresa");
        criteria.createAlias("empresa.empresaContratos", "empresaContratos", JoinType.NONE);
        criteria.createAlias("tramiteRemessaAtual", "tramite", JoinType.INNER_JOIN);
        criteria.createAlias("tramite.situacao", "situacao", JoinType.INNER_JOIN);

        if (!Util.isNullOuVazio(JSFUtil.getUsuario().getNuCnpj())) {
            criteria.add(Restrictions.eq("empresa.cnpj", JSFUtil.getUsuario().getNuCnpj()));
        }

        if (!Util.isNullOuVazio(numeroRemessa)) {
            criteria.add(Restrictions.or(Restrictions.eq("id", numeroRemessa), Restrictions.eq("codigoRemessaTipoC", numeroRemessa)));
        }
        
        if (!Util.isNullOuVazio(numeroLacre)) {
            criteria.add(Restrictions.eq("lacre", numeroLacre));
        }

        if (!Util.isNullOuVazio(emailUsuario)) {
            criteria.add(Restrictions.eq("codigoUsuarioAbertura", emailUsuario).ignoreCase());
        }

        if (!Util.isNullOuVazio(unidade)) {
            criteria.add(Restrictions.eq("unidadeSolicitante", unidade));
        }

        if (!Util.isNullOuVazio(situacao)) {
            // Se a situação for diferente de TODAS, então efetua o filtro da consulta
            if (!situacao.getId().equals(SituacaoRemessaEnum.TODAS.getId())) {
                criteria.add(Restrictions.eq("tramite.situacao.id", situacao.getId()));
            } else {

                List<Long> listSituacoes = new ArrayList<Long>();
                for (SituacaoRemessaEnum s : SituacaoRemessaEnum.valuesExtranet()) {
                    listSituacoes.add(s.getId());
                }

                criteria.add(Restrictions.in("tramite.situacao.id", listSituacoes));
            }
        } else if (Util.isNullOuVazio(numeroRemessa)) {
            // A situação PENDENTES envolve as Remessas que estão na situação ABERTA, AGENDADA, RECEBIDA, CONFERIDA e CORRIGIDA.

            List<Long> listSituacoes = new ArrayList<Long>();
            for (SituacaoRemessaEnum s : SituacaoRemessaEnum.valuesPendenteExtranet()) {
                listSituacoes.add(s.getId());
            }

            criteria.add(Restrictions.in("tramite.situacao.id", listSituacoes));
        }

        if (!Util.isNullOuVazio(dataInicio) && Util.isNullOuVazio(dataTermino)) {
            dataTermino = Calendar.getInstance().getTime();
        }

        if (!Util.isNullOuVazio(dataInicio) && !Util.isNullOuVazio(dataTermino)) {
            Calendar dateStart = Calendar.getInstance();
            dateStart.setTime(dataInicio);
            dateStart.set(Calendar.HOUR, 0);
            dateStart.set(Calendar.MINUTE, 0);
            dateStart.set(Calendar.SECOND, 0);

            Calendar dateEnd = Calendar.getInstance();
            dateEnd.setTime(dataTermino);
            dateEnd.set(Calendar.HOUR, 23);
            dateEnd.set(Calendar.MINUTE, 59);
            dateEnd.set(Calendar.SECOND, 59);
            dataTermino = dateEnd.getTime();
            criteria.add(Restrictions.between("dataHoraAbertura", dateStart.getTime(), dateEnd.getTime()));

        }
        
        criteria.addOrder(Order.asc("id"));
        
        List<RemessaVO> list = new ArrayList<RemessaVO>();
        
        if (inicio == -1 && fim == -1) {
        	list = (List<RemessaVO>) criteria.list();
        } else {
        	list = (List<RemessaVO>) criteria.setFirstResult(inicio).setMaxResults(fim).list();
        }
        
        for (RemessaVO remessa : list) {
            Hibernate.initialize(remessa.getRemessaDocumentos());
            Hibernate.initialize(remessa.getMovimentosDiarioList());
            
            for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVo : remessa.getMovimentosDiarioList()) {
              Hibernate.initialize(remessaMovimentoDiarioVo.getUnidadeGeradora());
              Hibernate.initialize(remessaMovimentoDiarioVo.getUnidadeGeradora().getUf());
              Hibernate.initialize(remessaMovimentoDiarioVo.getIcAlteracaoValida());
            }
            
            for (RemessaDocumentoVO remessaDocumentoVO : remessa.getRemessaDocumentos()) {
              Hibernate.initialize(remessaDocumentoVO.getUnidadeGeradora().getUf());
              Hibernate.initialize(remessaDocumentoVO.getDocumento());
            }
        }
        return list;
    }
    
    public int consultarRemessaTotalRegistros(Long numeroRemessa, Long numeroLacre, Date dateInicio, Date dateTermino, String emailUsuario, UnidadeVO unidade,
            SituacaoRemessaEnum situacao) throws AppException {

        Criteria criteria = getSession().createCriteria(RemessaVO.class, "this");
        criteria.createAlias("unidadeSolicitante", "unidade", JoinType.INNER_JOIN);
        criteria.createAlias("empresaContrato", "contrato", JoinType.LEFT_OUTER_JOIN);
        criteria.createAlias("contrato.base", "base", JoinType.LEFT_OUTER_JOIN);
        criteria.createAlias("contrato.empresa", "empresa");
        criteria.createAlias("empresa.empresaContratos", "empresaContratos", JoinType.NONE);
        criteria.createAlias("tramiteRemessaAtual", "tramite", JoinType.INNER_JOIN);
        criteria.createAlias("tramite.situacao", "situacao", JoinType.INNER_JOIN);

        if (!Util.isNullOuVazio(JSFUtil.getUsuario().getNuCnpj())) {
            criteria.add(Restrictions.eq("empresa.cnpj", JSFUtil.getUsuario().getNuCnpj()));
        }

        if (!Util.isNullOuVazio(numeroRemessa)) {
        	criteria.add(Restrictions.or(Restrictions.eq("id", numeroRemessa), Restrictions.eq("codigoRemessaTipoC", numeroRemessa)));
        }
        
        if (!Util.isNullOuVazio(numeroLacre)) {
            criteria.add(Restrictions.eq("lacre", numeroLacre));
        }

        if (!Util.isNullOuVazio(emailUsuario)) {
            criteria.add(Restrictions.eq("codigoUsuarioAbertura", emailUsuario).ignoreCase());
        }

        if (!Util.isNullOuVazio(unidade)) {
            criteria.add(Restrictions.eq("unidadeSolicitante", unidade));
        }

        if (!Util.isNullOuVazio(situacao)) {
            // Se a situação for diferente de TODAS, então efetua o filtro da consulta
            if (!situacao.getId().equals(SituacaoRemessaEnum.TODAS.getId())) {
                criteria.add(Restrictions.eq("tramite.situacao.id", situacao.getId()));
            } else {

                List<Long> listSituacoes = new ArrayList<Long>();
                for (SituacaoRemessaEnum s : SituacaoRemessaEnum.valuesExtranet()) {
                    listSituacoes.add(s.getId());
                }

                criteria.add(Restrictions.in("tramite.situacao.id", listSituacoes));
            }
        } else if (Util.isNullOuVazio(numeroRemessa)) {
            // A situação PENDENTES envolve as Remessas que estão na situação ABERTA, AGENDADA, RECEBIDA, CONFERIDA e CORRIGIDA.

            List<Long> listSituacoes = new ArrayList<Long>();
            for (SituacaoRemessaEnum s : SituacaoRemessaEnum.valuesPendenteExtranet()) {
                listSituacoes.add(s.getId());
            }

            criteria.add(Restrictions.in("tramite.situacao.id", listSituacoes));
        }

        if (!Util.isNullOuVazio(dateInicio) && Util.isNullOuVazio(dateTermino)) {
            dateTermino = Calendar.getInstance().getTime();
        }

        if (!Util.isNullOuVazio(dateInicio) && !Util.isNullOuVazio(dateTermino)) {
            Calendar dateStart = Calendar.getInstance();
            dateStart.setTime(dateInicio);
            dateStart.set(Calendar.HOUR, 0);
            dateStart.set(Calendar.MINUTE, 0);
            dateStart.set(Calendar.SECOND, 0);

            Calendar dateEnd = Calendar.getInstance();
            dateEnd.setTime(dateTermino);
            dateEnd.set(Calendar.HOUR, 23);
            dateEnd.set(Calendar.MINUTE, 59);
            dateEnd.set(Calendar.SECOND, 59);

            criteria.add(Restrictions.between("dataHoraAbertura", dateStart.getTime(), dateEnd.getTime()));

        }
        criteria.addOrder(Order.asc("id"));
        return criteria.list().size();
    }
    
    public RemessaVO obterRemessaComMovimentosDiarios(long id) {
      return remessaDAO.obterRemessaComMovimentosDiarios(id);
    }
    
    public List<MovimentoDiarioRemessaCDTO> obterAgrupamentoDeItensDeRemessaPorDiaUnidade(RemessaVO remessa) {

      List<MovimentoDiarioRemessaCDTO> dataMovimentosList = new ArrayList<>(); 
      List<RemessaMovimentoDiarioVO> listaC = new ArrayList<>(); 
      Map<String, List<RemessaMovimentoDiarioVO>> mapa = new HashMap<>();
      remessa = obterRemessaComMovimentosDiarios((Long) remessa.getId());
      
      String chave;
      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
      for (RemessaMovimentoDiarioVO item : remessa.getMovimentosDiarioList()) {
        chave = ( (Long) item.getUnidadeGeradora().getId()).toString() + "#" + sdf.format(item.getDataMovimento());
        listaC = mapa.get(chave);
        if (listaC == null) {
          listaC = new ArrayList<>(); 
        } 
        listaC.add(item);      
        mapa.put(chave, listaC);
      }

      //Transforma o map na lista esperada
      MovimentoDiarioRemessaCDTO mov;
      for (String key : mapa.keySet()) {
        mov = new MovimentoDiarioRemessaCDTO();
        mov.setChave(key);
        mov.setDataMovimento(obterDataPorChave(key));
        mov.setRemessaMovDiarioList(mapa.get(key));
        dataMovimentosList.add(mov);
      }
      /** Ordena por Data */
      Collections.sort(dataMovimentosList);
      
      return dataMovimentosList; 
      
    }
    
    private Date obterDataPorChave(String key) {
      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
      Date data = null;
      try {
        data = sdf.parse(StringUtils.substringAfter(key, "#"));
      } catch (ParseException e) {
        return null;
      }
      return data;
    }
    
    public RemessaVO obterRemessaComListaDocumentos(Long id) {
      return this.remessaDAO.obterRemessaComListaDocumentos(id);
    }
    
    public RemessaVO alterarRemessaDocumento(RemessaVO remessa) throws Exception {
    TramiteRemessaVO tramite = null;
    tramite = tramiteRemessaService.salvarTramiteRemessaAlterada(remessa);
    remessa.setTramiteRemessaAtual(tramite);
    remessa = this.remessaDAO.update(remessa);
    
    return remessa;
    }
    
    public RemessaVO emAlteracaoRemessaDocumento(RemessaVO remessa) throws Exception {
    TramiteRemessaVO tramite = null;
    tramite = tramiteRemessaService.salvarTramiteRemessaEmAlteracao(remessa);
    remessa.setTramiteRemessaAtual(tramite);
    remessa = this.remessaDAO.update(remessa);
    
    return remessa;
    }
    
    public RemessaVO recebidaRemessaDocumento(RemessaVO remessa) throws Exception {
      TramiteRemessaVO tramite = null;
      tramite = tramiteRemessaService.salvarTramiteRemessaRecebida(remessa);
      remessa.setTramiteRemessaAtual(tramite);
      remessa = this.remessaDAO.update(remessa);
      
      return remessa;
      }
    
    public RemessaVO salvarRascunhoRemessaAB(RemessaVO remessa) throws Exception {
      if (remessa.getId() == null) {
        remessa = this.remessaDAO.save(remessa);
      } else {
        remessa = this.remessaDAO.update(remessa);
      }
      
      /** Inserir o Tramite após persistir a Remessa */
      remessa.setTramiteRemessaAtual(tramiteRemessaService.salvarTramiteRemessaRascunho(remessa));
      remessa = this.remessaDAO.update(remessa);
      return remessa;
    }

    public RemessaVO salvarRascunho(RemessaVO remessa) throws Exception {
      if (remessa.getId() == null) {
        remessa.setCodigoRemessaTipoC(sequencialRemessaService.generate(remessa.getUnidadeSolicitante()));
        remessa = this.remessaDAO.save(remessa);
      } else {
        remessa = this.remessaDAO.update(remessa);
      }

      /** Inserir o Tramite após persistir a Remessa */
      remessa.setTramiteRemessaAtual(tramiteRemessaService.salvarTramiteRemessaRascunho(remessa));
      // remessa = consultarContratoVigenteUnidadeGeradora(remessa);
      remessa = this.remessaDAO.update(remessa);
      return remessa;
    }

    public RemessaVO concluirRemessaMovimentoDiarioTipoC(RemessaVO remessa) throws Exception {
      TramiteRemessaVO tramite = null;
      tramite = tramiteRemessaService.salvarTramiteRemessaAlterada(remessa);
      remessa.setTramiteRemessaAtual(tramite);
      remessa = this.remessaDAO.update(remessa);
      
      return remessa;
    }

    
}
