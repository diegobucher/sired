package br.gov.caixa.gitecsa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.Query;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.repository.DataRepository;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;

@Stateless
public class RequisicaoService extends AbstractService<RequisicaoVO> {

    private static final long serialVersionUID = 1L;

    @Inject
    @DataRepository
    private EntityManager entityManagerSistema;

    @Inject
    private EmpresaContratoService empresaContratoService;

    @Inject
    private TramiteRequisicaoService tramiteRequisicaoService;

    @Inject
    private FeriadoService feriadoService;

    @Inject
    private TramiteRequisicaoDocumentoService tramiteRequisicaoDocumentoService;

    @Inject
    private ParametroSistemaService parametroSistemaService;

    @Override
    protected void validaCampos(RequisicaoVO entity) {

    }

    @Override
    protected void validaRegras(RequisicaoVO entity) {
        regraValidarContratoVigente(entity);
    }

    @Override
    protected void validaRegrasExcluir(RequisicaoVO entity) {

    }

    private void regraValidarContratoVigente(RequisicaoVO vo) {
        try {
            EmpresaContratoVO retorno = empresaContratoService.buscarContratoVigente(vo.getUnidadeSolicitante());
            if (retorno == null) {
                List<BaseVO> baseVOs = tramiteRequisicaoDocumentoService
                        .consultaBasePorIdUnidade(Long.parseLong(vo.getUnidadeSolicitante().getId().toString()));
                BaseVO baseVO = baseVOs.get(0);
                String nomeBase = Util.isNullOuVazio(baseVO) ? "" : baseVO.getNome();

                mensagens.add(MensagemUtils.obterMensagem("MA060", nomeBase));
            } else {
                vo.setEmpresaContrato(retorno);
            }
        } catch (AppException e) {
            mensagens.add(MensagemUtils.obterMensagem("MA060"));
            logger.error(LogUtils.getMensagemPadraoLog(LogUtils.getDescricaoPadraoPilhaExcecaoLog(e), "RequisicaoService", "regraValidarContratoVigente"));
        }

    }

    @Override
    public void delete(RequisicaoVO entity) throws AppException {
        tramiteRequisicaoService.excluirByRequisicao(entity);
        super.delete(entity);
    }

    @SuppressWarnings("unchecked")
    public void regraVerificarRequisicaoComSitRascunho(RequisicaoVO reqVo) {
        String sHql = "select req from RequisicaoVO as req " + " join req.documento as doc " + " join req.tramiteRequisicoes tra "
                + " join tra.situacaoRequisicao st " + " where tra.dataHora = ( " + " 	select max(tra1.dataHora) from TramiteRequisicaoVO tra1 "
                + " 	where tra1.requisicao.id = req.id ) " + " and st.nome = :pSituacao " + " and doc.id = :pDocumento" + " and req.unidade.id = :pUnidade "
                + " and req.id != :pId ";

        Query query = getSession().createQuery(sHql);
        // TODO: A propriedade situação poderia ser mapeado como enum e assim
        // evitar
        // o uso do valor literal hardcoded. Como não há fk de requisição para
        // situação requisição,
        // a tabela poderia também ser eliminada.
        query.setParameter("pSituacao", "RASCUNHO");
        query.setParameter("pDocumento", reqVo.getDocumento().getId());
        query.setParameter("pUnidade", reqVo.getUnidadeSolicitante().getId());
        query.setParameter("pId", reqVo.getId());

        List<RequisicaoVO> lista = (List<RequisicaoVO>) query.list();
        if (lista.size() == 0)
            return;

        mensagens.add(MensagemUtils.obterMensagem("MA007", lista.get(0).getId().toString()));

    }

    @SuppressWarnings("unchecked")
    public List<RequisicaoVO> pesquisarRequisicaoRascunhos(UsuarioLdap usuarioLdap) {
        String sHql = "select distinct req.id, doc.nome, tra.dataHora " + " from RequisicaoVO as req " + " join req.documento as doc "
                + " join req.tramiteRequisicoes tra " + " join tra.situacaoRequisicao st " + " where tra.dataHora = ( "
                + " 	select max(tra1.dataHora) from TramiteRequisicaoVO tra1 " + " 	where tra1.requisicao.id = req.id " + ") and st.nome = :pSituacao"
                + " and tra.codigoUsuario = :pUsuario";

        Query query = getSession().createQuery(sHql);
        query.setParameter("pSituacao", SituacaoRequisicaoEnum.RASCUNHO.toString());
        query.setParameter("pUsuario", usuarioLdap.getNuMatricula());

        Iterator<?> result = query.list().iterator();

        List<RequisicaoVO> lista = new ArrayList<RequisicaoVO>();

        while (result.hasNext()) {
            Object[] row = (Object[]) result.next();
            RequisicaoVO requisicaoVO = new RequisicaoVO();
            requisicaoVO.setId(row[0]);
            requisicaoVO.setDocumento(new DocumentoVO());
            requisicaoVO.getDocumento().setNome(row[1].toString());
            List<TramiteRequisicaoVO> lstTram = new ArrayList<TramiteRequisicaoVO>();
            lstTram.add(new TramiteRequisicaoVO());
            lstTram.get(0).setDataHora((Date) row[2]);
            requisicaoVO.setTramiteRequisicoes((HashSet<TramiteRequisicaoVO>) lstTram);
            lista.add(requisicaoVO);
        }

        return lista;
    }
}
