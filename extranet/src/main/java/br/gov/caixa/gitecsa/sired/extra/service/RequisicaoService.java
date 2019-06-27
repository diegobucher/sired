package br.gov.caixa.gitecsa.sired.extra.service;

import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.PaginatorService;
import br.gov.caixa.gitecsa.sired.dto.FiltroRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.extra.dao.BaseDAO;
import br.gov.caixa.gitecsa.sired.extra.dao.EmpresaContratoDAO;
import br.gov.caixa.gitecsa.sired.extra.dao.RequisicaoDAO;
import br.gov.caixa.gitecsa.sired.extra.dao.TramiteRequisicaoDAO;
import br.gov.caixa.gitecsa.sired.extra.util.ConstsSiredExtra;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Stateless
public class RequisicaoService extends PaginatorService<RequisicaoVO> {

    private static final String EVENTO_VALIDAR_CONTRATO_VIGENTE = "regraValidarContratoVigente";

    private static final String FUNCIONALIDADE_REQUISICAO_SERVICE = "RequisicaoService";

    private static final long serialVersionUID = 6189095199672482696L;

    @Inject
    private RequisicaoDAO requisicaoDAO;

    @Inject
    private TramiteRequisicaoDAO tramiteRequisicaoDAO;

    @Inject
    private BaseDAO baseDAO;

    @Inject
    private EmpresaContratoDAO empresaContratoDAO;

    @Override
    protected void validaRegras(RequisicaoVO requisicao) {
        regraValidarContratoVigente(requisicao);
    }

    @Override
    public Integer count(Map<String, Object> filters) {
        FiltroRequisicaoDTO filtro = (FiltroRequisicaoDTO) filters.get("filtroDTO");
        return requisicaoDAO.count(filtro);
    }

    public List<RequisicaoVO> pesquisar(Map<String, Object> filters) {
        return this.pesquisar(null, null, filters);
    }

    @Override
    public List<RequisicaoVO> pesquisar(Integer offset, Integer limit, Map<String, Object> filters) {
        FiltroRequisicaoDTO filtro = (FiltroRequisicaoDTO) filters.get("filtroDTO");

        return (!ObjectUtils.isNullOrEmpty(offset) && !ObjectUtils.isNullOrEmpty(limit)) ? requisicaoDAO.consultar(filtro, offset, limit) : requisicaoDAO
                .consultar(filtro);

    }

    private void regraValidarContratoVigente(RequisicaoVO requisicao) {
        try {
            EmpresaContratoVO retorno = empresaContratoDAO.buscarContratoVigente(requisicao);

            if (ObjectUtils.isNullOrEmpty(retorno)) {
                List<BaseVO> baseVOs = baseDAO.consultaBasePorIdUnidade(Long.parseLong(requisicao.getUnidadeSolicitante().getId().toString()));

                BaseVO baseVO = baseVOs.get(0);

                String nomeBase = ObjectUtils.isNullOrEmpty(baseVO) ? StringUtils.EMPTY : baseVO.getNome();

                mensagens.add(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_CONTRATO_NAOVIGENTE, nomeBase));
            } else {
                requisicao.setEmpresaContrato(retorno);
            }
        } catch (AppException e) {
            mensagens.add(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_CONTRATO_NAOVIGENTE));
            logger.error(LogUtils.getMensagemPadraoLog(LogUtils.getDescricaoPadraoPilhaExcecaoLog(e), FUNCIONALIDADE_REQUISICAO_SERVICE,
                    EVENTO_VALIDAR_CONTRATO_VIGENTE));
        }

    }

    public void delete(RequisicaoVO entity) throws AppException {
        tramiteRequisicaoDAO.excluirByRequisicao(entity);

        requisicaoDAO.delete(entity);
    }

    public List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas, Integer offset, Integer limit) {
        return requisicaoDAO.consultar(filtro, unidadesAutorizadas, offset, limit);
    }

    public RequisicaoVO obterPorNumeroID(Long numeroIdentificacao) {
        return requisicaoDAO.obterPorNumeroID(numeroIdentificacao);
    }

    public RequisicaoVO findByIdEager(Long id) {
        return this.requisicaoDAO.findByIdEager(id);
    }

    @Override
    protected void validaCamposObrigatorios(RequisicaoVO entity) throws BusinessException {
        // do nothing
    }

    @Override
    protected void validaRegrasExcluir(RequisicaoVO entity) {
        // do nothing
    }

    @Override
    protected GenericDAO<RequisicaoVO> getDAO() {
        return requisicaoDAO;
    }
}
