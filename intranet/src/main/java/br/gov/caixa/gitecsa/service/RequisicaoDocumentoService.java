package br.gov.caixa.gitecsa.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.LoteSequenciaVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;

@Stateless
public class RequisicaoDocumentoService extends AbstractService<RequisicaoDocumentoVO> {

    private static final long serialVersionUID = 1L;

    private static final String CHEQUE_COMPENSADO = "CHEQUE COMPENSADO";
    private static final String CHEQUE_BOCA_DE_CAIXA = "CHEQUE BOCA DE CAIXA";
    private static final String EXTRATO = "EXTRATO";

    private List<GrupoVO> grupos;

    @Inject
    private TramiteRequisicaoDocumentoService tramiteRequisicaoDocumentoService;

    @Inject
    private BaseService baseService;

    @Inject
    private GrupoService grupoService;

    @Inject
    private FeriadoService feriadoService;

    @Override
    protected void validaCampos(RequisicaoDocumentoVO entity) {

    }

    /**
     * Método que calcula o prazo de atendimento do documento da requisição de acordo com o tipo de documento e o tipo de demanda. Refere-se
     * as regras RN009 E RN010.
     * 
     * @param entity
     * @throws AppException
     */
    private void calculaDataPrazoDeAtendimento(RequisicaoDocumentoVO entity) throws AppException {
        /*
         * if (entity.getRequisicao().getDocumento().getIcSetorial().getDescricao
         * ().toUpperCase().equals(TipoDocumentoEnum.SETORIAL.getDescricao(). toUpperCase())) {
         * 
         * entity.setDataPrazoAtendimento(feriadoService.proximaDataUtil(new Date(), entity.getTipoDemanda().getPrazoDias().intValue(),
         * entity.getUnidadeGeradora()));
         * 
         * } else { boolean tipoC = false; if (entity.getDataAutenticacao() != null) { int diferenca =
         * Util.calculaDiferencaEntreDatasEmDias(Calendar .getInstance().getTime(), entity.getDataAutenticacao()); if (diferenca < 0 ) {
         * diferenca = diferenca * (-1); } if (diferenca <= 90) { tipoC = true; } } if (tipoC) {
         * entity.setDataPrazoAtendimento(feriadoService.proximaDataUtil(new Date(), 1, entity.getUnidadeGeradora())); } else {
         * entity.setDataPrazoAtendimento(feriadoService.proximaDataUtil(new Date(), entity.getTipoDemanda().getPrazoDias().intValue(),
         * entity.getUnidadeGeradora())); } }
         */

    }

    /**
     * Calcula a quantidade de documentos solicitados de acordo com o tipo de documento da requisição e com o período solicitado. Referente
     * a RN008
     * 
     * @param reqdoc
     * @throws AppException
     */
    public void calculaQtdeDocumentosSolicitados(RequisicaoDocumentoVO reqdoc) throws AppException {

        /*
         * if (reqdoc.getRequisicao().getDocumento().getTipoAgrupamento().getValor
         * ().equals(TipoAgrupamentoDocumentoEnum.MENSAL.getValor())) { if (!Util.isNullOuVazio(reqdoc.getNuMesAnoInicio()) &&
         * !Util.isNullOuVazio(reqdoc.getNuMesAnoFim())) { reqdoc.setQtSolicitadaDocumento(calculaDiferencaEmMeses(reqdoc)); } else {
         * reqdoc.setQtSolicitadaDocumento(1); }
         * 
         * } else if (reqdoc.getRequisicao().getDocumento().getTipoAgrupamento().
         * getValor().equals(TipoAgrupamentoDocumentoEnum.ANUAL.getValor())) { if (!Util.isNullOuVazio(reqdoc.getNuMesAnoInicio()) &&
         * !Util.isNullOuVazio(reqdoc.getNuMesAnoFim())) { int qtdeMeses = calculaDiferencaEmMeses(reqdoc); int qtdeDocs = qtdeMeses / 12;
         * if (qtdeMeses % 12 > 0) { qtdeDocs += 1; } reqdoc.setQtSolicitadaDocumento(qtdeDocs); } else {
         * reqdoc.setQtSolicitadaDocumento(1); } } else { reqdoc.setQtSolicitadaDocumento(1); }
         */

    }

    /**
     * Calcula a diferença em meses de um periodo com mês/ano de início e mês/ano fim. O mês início e o fim estão inclusos no período.
     * 
     * @param reqdoc
     * @return
     */
    public int calculaDiferencaEmMeses(RequisicaoDocumentoVO reqdoc) {

        int mesInicio = Integer.parseInt(reqdoc.getNuMesAnoInicio().substring(0, 2));
        int anoInicio = Integer.parseInt(reqdoc.getNuMesAnoInicio().substring(2, 6));
        int mesFim = Integer.parseInt(reqdoc.getNuMesAnoFim().substring(0, 2));
        int anoFim = Integer.parseInt(reqdoc.getNuMesAnoFim().substring(2, 6));
        int qtdeMeses = 0;
        if (anoFim > anoInicio) {
            qtdeMeses = 13 - mesInicio;
            qtdeMeses += mesFim;
            anoInicio += 1;
        } else {
            qtdeMeses += mesFim - mesInicio + 1;
        }
        while (anoInicio < anoFim) {
            qtdeMeses += 12;
            anoInicio += 1;
        }
        return qtdeMeses;
    }

    public void validaDados(RequisicaoDocumentoVO entity) throws BusinessException {
        mensagens = new ArrayList<String>();
        validaRegras(entity);
        if (!Util.isNullOuVazio(mensagens)) {
            throw new BusinessException(mensagens);
        }
    }

    /*
     * public void verificaDuplicidadeDocumento(RequisicaoDocumentoVO doc) throws BusinessException { mensagens = new ArrayList<String>();
     * validaRequisicaoJaCadastrada(doc); if (!Util.isNullOuVazio(mensagens)) throw new BusinessException(mensagens); }
     */

    @Override
    protected void validaRegras(RequisicaoDocumentoVO entity) {
        /*
         * try { grupos = grupoService.findGrupoAssociadoDocumento(entity.getRequisicao ().getDocumento()); } catch (AppException e1) {
         * mensagens.add(e1.getMessage()); }
         * 
         * try { // Só calcula o prazo de atendimento na atualização da requisição de documento. if (!Util.isNullOuVazio(entity.getId())) {
         * calculaDataPrazoDeAtendimento(entity); }
         * 
         * } catch (AppException e1) { mensagens.add(e1.getMessage()); }
         * 
         * if (!Util.isNullOuVazio(entity.getNuLoteSequencia())) { try { validarNumeroLoteSequencia(entity); } catch (RequiredException e) {
         * mensagens.add(e.getMessage()); return; } catch (AppException e) { mensagens.add(e.getMessage()); return; } }
         * 
         * if (ehGrupoDocumento(CHEQUE_COMPENSADO)) { if (!Util.isNullOuVazio(entity.getDataAutenticacao())) { Calendar calendar =
         * Calendar.getInstance(); calendar.set(2011, 05, 20); if (entity.getDataAutenticacao().after(calendar.getTime())) { mensagens.add
         * (MensagemUtil.obterMensagem("MA016" )); return; } } }
         * 
         * if (ehGrupoDocumento(CHEQUE_COMPENSADO) || ehGrupoDocumento(CHEQUE_BOCA_DE_CAIXA)) { if
         * (!Util.isNullOuVazio(entity.getObservacao())) { if (entity.getObservacao().toUpperCase().contains("DEV") ||
         * entity.getObservacao().toUpperCase().contains("DEVOLVIDO") || entity.getObservacao().toUpperCase().contains("DEVOLUÇÃO")) {
         * mensagens .add(MensagemUtil.obterMensagem("MA015" )); return; } } }
         * 
         * if (ehGrupoDocumento(EXTRATO) && !Util.isNullOuVazio(entity.getOperacao()) && entity.getOperacao().getIcAtivo() == 1) {
         * 
         * if (!Util.isNullOuVazio(entity.getNuMesAnoFim())) { String str = entity.getNuMesAnoFim().substring(2, 6) +
         * entity.getNuMesAnoFim().substring(0, 2); str = Util.acrescentaZeroEsquerda(str, 6); if (validarAnoMesReferencia(str)) {
         * mensagens.add(MensagemUtil.obterMensagem ("MA014")); return; } }
         * 
         * if (!Util.isNullOuVazio(entity.getNuMesAnoInicio())) { String str = entity.getNuMesAnoInicio().substring(2, 6) +
         * entity.getNuMesAnoInicio().substring(0, 2); str = Util.acrescentaZeroEsquerda(str, 6); if (validarAnoMesReferencia(str)) {
         * mensagens.add(MensagemUtil.obterMensagem ("MA014")); return; } } }
         * 
         * if (!Util.isNullOuVazio(entity.getNuDv()) && !Util.isNullOuVazio(entity.getNumeroConta()) &&
         * !Util.isNullOuVazio(entity.getOperacao())) { if (!Util.validaDvCaixa("" + Util.acrescentaZeroEsquerda(entity.getUnidadeGeradora
         * ().getId().toString(), 4) + entity.getOperacao().getId() + Util.acrescentaZeroEsquerda(entity.getNumeroConta().replaceAll("_",
         * ""), 8) ,entity.getNuDv())) {
         * 
         * mensagens.add(MensagemUtil.obterMensagem( "MA034"));
         * 
         * return; } }
         * 
         * if (!Util.isNullOuVazio(entity.getNuMesAnoInicio())) { String valor = entity.getNuMesAnoInicio().toString(); int mes =
         * Integer.parseInt(valor.substring(0, 2)); if (mes < 1 || mes > 12) { mensagens.add(MensagemUtil.obterMensagem(
         * "requisicao.mensagem.mesEAnoInicioPesquisa")); return; } }
         * 
         * if (!Util.isNullOuVazio(entity.getNuMesAnoFim())) { String valor = entity.getNuMesAnoFim().toString(); int mes =
         * Integer.parseInt(valor.substring(0, 2)); if (mes < 1 || mes > 12) { mensagens
         * .add(MensagemUtil.obterMensagem("requisicao.mensagem.mesEAnoFimPesquisa" )); return; } }
         * 
         * if (!Util.isNullOuVazio(entity.getNumeroCpf())) { if (!Util.validaCPF(entity.getNumeroCpf())) { mensagens.add(MensagemUtil.
         * obterMensagem("MA035")); return; } }
         * 
         * if (!Util.isNullOuVazio(entity.getCnpj())) { if (!Util.validaCNPJ(entity.getCnpj())) { mensagens.add(MensagemUtil.obterMensagem
         * ("MA036")); return; } } try { if (Util.isNullOuVazio(mensagens)) {
         * calculaQtdeDocumentosSolicitados(entity); validaRequisicaoJaCadastrada(entity); } } catch (AppException e) {
         * mensagens.add(e.getMessage()); }
         */
    }

    /**
     * Verifica se o anoMês informado é maior que o anoMes atual.
     * 
     * @param anoMes
     * @return
     */
    private boolean validarAnoMesReferencia(String anoMes) {

        SimpleDateFormat formatAno = new SimpleDateFormat("dd/MM/yyyy");
        String data = formatAno.format(Calendar.getInstance().getTime()).replaceAll("/", "");
        int mes = Integer.parseInt(data.substring(2, 4)) - 1;
        int ano = Integer.parseInt(data.substring(4, 8)) - 1;

        if (mes == 0) {
            mes = 12;
            ano = ano - 1;
        }

        int anoMesReferencia = Integer.parseInt(Integer.toString(ano).concat(Util.acrescentaZeroEsquerda(Integer.toString(mes), 2)));

        return Integer.parseInt(anoMes) > anoMesReferencia;
    }

    private boolean ehGrupoDocumento(String str) {
        for (GrupoVO grupoVO : grupos) {
            if (grupoVO.getNome().toUpperCase().equals(str)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void delete(RequisicaoDocumentoVO entity) throws AppException {
        /*
         * Integer numeroItem = entity.getNuItem();
         * 
         * super.delete(entity);
         * 
         * Criteria criteria = getSession().createCriteria(RequisicaoDocumentoVO.class, "this");
         * criteria.add(Restrictions.eq("requisicao.id", entity.getRequisicao().getId())); criteria.add(Restrictions.gt("nuItem",
         * numeroItem)); criteria.addOrder(Order.asc("nuItem")); List<RequisicaoDocumentoVO> listRequisicaoDocumento =
         * (List<RequisicaoDocumentoVO>) criteria.list();
         * 
         * for (RequisicaoDocumentoVO item : listRequisicaoDocumento){ item.setNuItem(numeroItem++); super.update(item); }
         */
    }

    @Override
    protected void validaRegrasExcluir(RequisicaoDocumentoVO entity) {

    }

    private void validarNumeroLoteSequencia(RequisicaoDocumentoVO vo) throws AppException, RequiredException {
        /*
         * String tmp = vo.getNuLoteSequencia().replaceAll("/", ""); if (tmp.length() != 15) tmp = Util.acrescentaZeroEsquerda(tmp, 15);
         * 
         * List<LoteSequenciaVO> retorno = baseService.loteSequenciaVinculado(tmp.substring(0, 4), vo.getUnidadeGeradora());
         * 
         * if (retorno == null || retorno.size() == 0) { List<BaseVO> bases = tramiteRequisicaoDocumentoService
         * .consultaBasePorIdUnidade(Long.parseLong (vo.getUnidadeGeradora().getId().toString())); BaseVO baseVO = bases.get(0);
         * 
         * if (Util.isNull(baseVO)) { mensagens.add(MensagemUtil .obterMensagem("MA012")); } else { String lstSeq
         * = montarStrLoteSequencia(baseVO); String base = ""; if (baseVO != null) base = baseVO.getNome(); throw new
         * RequiredException(MensagemUtil.obterMensagem( "MI023", base, lstSeq)); }
         * 
         * }
         */

    }

    public String validaRequisicaoJaCadastrada(RequisicaoDocumentoVO vo) {
        /*
         * DetachedCriteria maxSituacao = DetachedCriteria.forClass(TramiteRequisicaoVO.class, "tramite2");
         * maxSituacao.setProjection(Property.forName("dataHora").max()); maxSituacao
         * .add(Property.forName("tramite2.requisicao").eqProperty("this.requisicao" ));
         * 
         * Criteria criteria = getSession().createCriteria(RequisicaoDocumentoVO.class, "this"); criteria.createAlias("unidadeGeradora",
         * "unidade", JoinType.INNER_JOIN); criteria.createAlias("operacao", "operacao", JoinType.LEFT_OUTER_JOIN);
         * criteria.createAlias("tipoDemanda", "tipoDemanda", JoinType.LEFT_OUTER_JOIN); criteria.createAlias("requisicao", "requisicao",
         * JoinType.INNER_JOIN); criteria.createAlias("requisicao.tramiteRequisicoes", "tramite",JoinType.INNER_JOIN);
         * criteria.createAlias("requisicao.tramiteRequisicoes.situacaoRequisicao" , "sr",JoinType.INNER_JOIN);
         * criteria.add(Restrictions.eq("unidadeGeradora", vo.getUnidadeGeradora())); criteria.add(Restrictions.eq("requisicao.documento",
         * vo.getRequisicao().getDocumento())); criteria.add(Restrictions.ne("sr.id" ,SituacaoRequisicaoEnum.RASCUNHO.getId()));
         * criteria.add(Property.forName("tramite.dataHora").eq(maxSituacao));
         * 
         * // Applicando os criterias a cada campo if (!Util.isNullOuVazio(vo.getId())) criteria.add(Restrictions.ne("id", vo.getId()));
         * 
         * if (vo.getAnoEncerramentoLiq() != null) criteria.add(Restrictions.eq("anoEncerramentoLiq", vo.getAnoEncerramentoLiq())); else
         * criteria.add(Restrictions.isNull("anoEncerramentoLiq"));
         * 
         * if (vo.getCnpj() != null) criteria.add(Restrictions.eq("cnpj", vo.getCnpj())); else criteria.add(Restrictions.isNull("cnpj"));
         * 
         * if (vo.getCodCentroCusto() != null) criteria.add(Restrictions.eq("CodCentroCusto", vo.getCodCentroCusto())); else
         * criteria.add(Restrictions.isNull("CodCentroCusto"));
         * 
         * if (vo.getCodigoEvento() != null) criteria.add(Restrictions.eq("CodigoEvento", vo.getCodigoEvento())); else
         * criteria.add(Restrictions.isNull("CodigoEvento"));
         * 
         * if (vo.getDataAutenticacao() != null) criteria.add(Restrictions.eq("dataAutenticacao", vo.getDataAutenticacao())); else
         * criteria.add(Restrictions.isNull("dataAutenticacao"));
         * 
         * if (vo.getFormato() != null) criteria.add(Restrictions.eq("formato", vo.getFormato())); else
         * criteria.add(Restrictions.isNull("formato"));
         * 
         * if (vo.getNome() != null) criteria.add(Restrictions.eq("nome", vo.getNome())); else criteria.add(Restrictions.isNull("nome"));
         * 
         * if (vo.getNomeConvenente() != null) criteria.add(Restrictions.eq("nomeConvenente", vo.getNomeConvenente())); else
         * criteria.add(Restrictions.isNull("nomeConvenente"));
         * 
         * if (vo.getNuDocumentoExigido() != null) criteria.add(Restrictions.eq("nuDocumentoExigido", vo.getNuDocumentoExigido())); else
         * criteria.add(Restrictions.isNull("nuDocumentoExigido"));
         * 
         * if (vo.getNumeroChequeFim() != null) criteria.add(Restrictions.eq("numeroChequeFim", vo.getNumeroChequeFim())); else
         * criteria.add(Restrictions.isNull("numeroChequeFim"));
         * 
         * if (vo.getNumeroChequeInicio() != null) criteria.add(Restrictions.eq("numeroChequeInicio", vo.getNumeroChequeInicio())); else
         * criteria.add(Restrictions.isNull("numeroChequeInicio"));
         * 
         * if (vo.getNumeroConta() != null) criteria.add(Restrictions.eq("numeroConta", vo.getNumeroConta())); else
         * criteria.add(Restrictions.isNull("numeroConta"));
         * 
         * if (vo.getNumeroContrato() != null) criteria.add(Restrictions.eq("numeroContrato", vo.getNumeroContrato())); else
         * criteria.add(Restrictions.isNull("numeroContrato"));
         * 
         * if (vo.getNumeroCpf() != null) criteria.add(Restrictions.eq("numeroCpf", vo.getNumeroCpf())); else
         * criteria.add(Restrictions.isNull("numeroCpf"));
         * 
         * if (vo.getNumeroDocumento() != null) criteria.add(Restrictions.eq("numeroDocumento", vo.getNumeroDocumento())); else
         * criteria.add(Restrictions.isNull("numeroDocumento"));
         * 
         * if (vo.getNuMesAnoFim() != null) criteria.add(Restrictions.eq("nuMesAnoFim", vo.getNuMesAnoFim())); else
         * criteria.add(Restrictions.isNull("nuMesAnoFim"));
         * 
         * if (vo.getNuMesAnoInicio() != null) criteria.add(Restrictions.eq("nuMesAnoInicio", vo.getNuMesAnoInicio())); else
         * criteria.add(Restrictions.isNull("nuMesAnoInicio"));
         * 
         * if (vo.getNuPis() != null) criteria.add(Restrictions.eq("nuPis", vo.getNuPis())); else
         * criteria.add(Restrictions.isNull("nuPis"));
         * 
         * if (vo.getNuTerminalFinanceiro() != null) criteria.add(Restrictions.eq("nuTerminalFinanceiro", vo.getNuTerminalFinanceiro()));
         * else criteria.add(Restrictions.isNull("nuTerminalFinanceiro"));
         * 
         * if (vo.getNuValor() != null) criteria.add(Restrictions.eq("nuValor", vo.getNuValor())); else
         * criteria.add(Restrictions.isNull("nuValor"));
         * 
         * if (vo.getOperacao() != null) criteria.add(Restrictions.eq("operacao.id", vo.getOperacao().getId())); if (vo.getTipoDemanda() !=
         * null) criteria.add(Restrictions.eq("tipoDemanda.id", vo.getTipoDemanda().getId()));
         * 
         * @SuppressWarnings("unchecked") List<RequisicaoDocumentoVO> list = (List<RequisicaoDocumentoVO>) criteria.list();
         * 
         * String unidade = null;
         * 
         * // Caso contenha qualquer registro emite a mensagem. if (list.size() > 0){ mensagens.add(MensagemUtil.obterMensagem("MA007",
         * list.get(0).getRequisicao().getId().toString())); unidade = list.get(0).getRequisicao().getId().toString(); }
         * 
         * return unidade;
         */
        return "";
    }

    private String montarStrLoteSequencia(BaseVO vo) {
        String retorno = "";
        for (LoteSequenciaVO elm : vo.getLoteSequencia()) {
            retorno += elm.getId().toString() + " ";
        }
        return retorno;
    }

    @Override
    public RequisicaoDocumentoVO getById(Object id) {
        RequisicaoDocumentoVO vo = super.getById(id);
        vo.getUnidadeGeradora();
        vo.getTipoDemanda();
        return vo;
    }

    @SuppressWarnings("unchecked")
    public List<RequisicaoDocumentoVO> findPorRequisicao(RequisicaoVO vo) {
        Criteria criteria = getSession().createCriteria(RequisicaoDocumentoVO.class);
        criteria.createAlias("tipoDemanda", "tipoDemanda", JoinType.INNER_JOIN);
        criteria.add(Restrictions.eq("requisicao.id", vo.getId()));

        return (List<RequisicaoDocumentoVO>) criteria.list();
    }

    protected RequisicaoDocumentoVO saveImpl(RequisicaoDocumentoVO requisicaoDocumento) throws AppException {
        try {
            entityManager.persist(requisicaoDocumento);
        } catch (Exception e) {
            mensagens.add(e.getMessage());
            if (!Util.isNullOuVazio(mensagens))
                throw new BusinessException(e.getMessage());
        }

        return requisicaoDocumento;
    }

    /**
     * Metodo que lê o arquivo cujo nome está no tramite da requisição do documento atual e transforma em array de bytes.
     * 
     * @param requisicaoDocumentoVO
     * @return
     * @throws Exception
     */
    public byte[] downloadDoArquivoDigitalizado(RequisicaoDocumentoVO requisicaoDocumentoVO) throws Exception {
        /*
         * String path = PropertyUtil.getProperty(Constantes.CAMINHO_UPLOAD); path = path +
         * requisicaoDocumentoVO.getTramiteRequisicaoDocumentoAtual ().getNomeArquivoDisponibilizado(); File arquivo = new File(path);
         * 
         * if (arquivo.exists()) { return getFileBytes(arquivo); }
         */
        return null;
    }

    public static byte[] getFileBytes(File file) throws Exception {
        int len = (int) file.length();
        byte[] sendBuf = new byte[len];
        FileInputStream inFile = null;
        try {
            inFile = new FileInputStream(file);
            inFile.read(sendBuf, 0, len);
            inFile.close();

        } catch (FileNotFoundException fnfex) {
            throw fnfex;
        } catch (IOException ioex) {
            throw ioex;
        }
        return sendBuf;
    }

    public boolean existeArquivoParaDownload(RequisicaoDocumentoVO requisicaoDocumentoVO) {
        /*
         * String path = PropertyUtil.getProperty(Constantes.CAMINHO_UPLOAD); if (!Util.isNullOuVazio(requisicaoDocumentoVO.
         * getTramiteRequisicaoDocumentoAtual()) && !Util.isNullOuVazio(requisicaoDocumentoVO .getTramiteRequisicaoDocumentoAtual
         * ().getNomeArquivoDisponibilizado())) { path = path + requisicaoDocumentoVO
         * .getTramiteRequisicaoDocumentoAtual().getNomeArquivoDisponibilizado (); File arquivo = new File(path); if (arquivo.exists()) {
         * return true; } }
         */
        return false;
    }

}
