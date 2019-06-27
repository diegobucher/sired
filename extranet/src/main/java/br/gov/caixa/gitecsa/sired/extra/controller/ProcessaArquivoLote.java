package br.gov.caixa.gitecsa.sired.extra.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.UploadedFile;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.enumerator.OcorrenciaAtendimentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSuporteEnum;
import br.gov.caixa.gitecsa.sired.extra.service.ArquivoLoteService;
import br.gov.caixa.gitecsa.sired.extra.service.RequisicaoService;
import br.gov.caixa.gitecsa.sired.extra.util.ConstsSiredExtra;
import br.gov.caixa.gitecsa.sired.extra.util.JSFUtil;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.ArquivoLoteVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.OcorrenciaAtendimentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.SuporteVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;

public class ProcessaArquivoLote {

    private static final int NUM_COLUNAS_ARQUIVO_LOTE = 4;

    private static final String MENSAGENS_ITENS_REJEITADOS = " itens rejeitados.";

    private static final String MENSAGENS_FINAL_LOG_GERADO_PELA_IMPORTAÇÃO = "Final do log gerado pela importação do arquivo ";

    private ArquivoLoteService arquivoLoteService;

    private RequisicaoService requisicaoService;

    private List<String> linhas = new ArrayList<String>();

    private int numLinha = 0;

    private int quantidadeLinhas = 0;

    private int quantidadeRejeitados = 0;

    private byte[] arquivoDownload = null;

    private String nomeArquivoLogLote;

    public ProcessaArquivoLote(ArquivoLoteService arquivoLoteService, RequisicaoService requisicaoService) {
        this.arquivoLoteService = arquivoLoteService;
        this.requisicaoService = requisicaoService;
    }

    public List<TramiteRequisicaoVO> processarArquivoCSV(UploadedFile arquivo) throws RequiredException, BusinessException, Exception {
        String path = System.getProperty(Constantes.DIRETORIO_ARQUIVOS_LOTES);
        if (!new File(path).exists()) {
            new File(path).mkdirs();
        }
        path = path + FilenameUtils.getName(arquivo.getFileName());
        File arq = new File(path);
        FileOutputStream fos = new FileOutputStream(arq);
        fos.write(arquivo.getContents());
        fos.flush();
        fos.close();

        ArquivoLoteVO arquivoLoteVO = new ArquivoLoteVO();
        arquivoLoteVO.setNome(FilenameUtils.getName(arquivo.getFileName()));
        arquivoLoteVO.setDataEnvioArquivo(Calendar.getInstance().getTime());
        arquivoLoteService.save(arquivoLoteVO);

        BufferedReader in = new BufferedReader(new InputStreamReader(arquivo.getInputstream()));

        List<String> requisicoesValidadas = new ArrayList<String>();
        List<TramiteRequisicaoVO> tramitesRequisicao = new ArrayList<TramiteRequisicaoVO>();
        String line = StringUtils.EMPTY;
        String linhaLog = StringUtils.EMPTY;

        while ((line = in.readLine()) != null) {
            if (!Util.isNullOuVazio(line)) {
                numLinha += 1;
                quantidadeLinhas += 1;

                String[] linha = line.split(",");
                RequisicaoVO requisicaoVO = null;
                SituacaoRequisicaoVO situacaoRequisicao = null;

                if (linha.length != NUM_COLUNAS_ARQUIVO_LOTE) {
                    linhas.clear();
                    arquivoDownload = null;
                    throw new IllegalArgumentException(MensagemUtils.obterMensagem("MI033"));
                }

                if (!Util.isNullOuVazio(linha[0])) {
                    linha[0] = FileUtils.removeUTF8BOM(linha[0]).replaceAll("/[^0-9]/", "");
                    requisicaoVO = requisicaoService.obterPorNumeroID(Long.valueOf(linha[0]));

                    if (requisicaoVO == null) {
                        linhaLog = "Linha " + numLinha + " - " + MensagemUtils.obterMensagem("ME016", linha[0]);
                        linhas.add(linhaLog);
                        quantidadeRejeitados += 1;
                        continue;
                    }

                    situacaoRequisicao = requisicaoVO.getTramiteRequisicaoAtual().getSituacaoRequisicao();
                    if (!situacaoRequisicao.getNome().equals(SituacaoRequisicaoEnum.ABERTA.name())
                            && !situacaoRequisicao.getNome().equals(SituacaoRequisicaoEnum.REABERTA.name())) {
                        linhaLog = "Linha " + numLinha + " - "
                                + MensagemUtils.obterMensagem("ME002", requisicaoVO.getCodigoRequisicao(), situacaoRequisicao.getNome());
                        linhas.add(linhaLog);
                        quantidadeRejeitados += 1;
                        continue;
                    }

                    EmpresaContratoVO empresaContrato = requisicaoVO.getEmpresaContrato();
                    if (!Util.isNullOuVazio(empresaContrato) && !Util.isNullOuVazio(empresaContrato.getEmpresa())
                            && !empresaContrato.getEmpresa().getCnpj().equals(JSFUtil.getUsuario().getNuCnpj())) {
                        linhaLog = "Linha " + numLinha + " - " + MensagemUtils.obterMensagem("ME003");
                        linhas.add(linhaLog);
                        quantidadeRejeitados += 1;
                        continue;
                    }
                } else {
                    linhaLog = "Linha " + numLinha + " " + MensagemUtils.obterMensagem("ME001");
                    linhas.add(linhaLog);
                    quantidadeRejeitados += 1;
                    continue;
                }

                if (linha.length > 1) {
                    if (!Util.isNullOuVazio(linha[0])) {
                        if (requisicoesValidadas.contains(linha[0])) {
                            linhaLog = "Linha " + numLinha + " " + MensagemUtils.obterMensagem(ConstsSiredExtra.ERRO_REQUISICAO_JA_TEVE_ATENDIMENTO);
                            linhas.add(linhaLog);
                            quantidadeRejeitados += 1;
                            continue;
                        }

                        requisicoesValidadas.add(linha[0]);
                    } else {
                        linhaLog = "Linha " + numLinha + " " + MensagemUtils.obterMensagem("ME004");
                        linhas.add(linhaLog);
                        quantidadeRejeitados += 1;
                        continue;

                    }
                }

                if (linha.length > 2) {
                    if (!Util.isNullOuVazio(linha[1])) {
                        OcorrenciaAtendimentoEnum[] atendimentoEnums = OcorrenciaAtendimentoEnum.values();
                        boolean existeIgual = false;
                        Metaphone metaphone = new Metaphone();

                        for (int i = 0; i < atendimentoEnums.length; i++) {
                            if (metaphone.isMetaphoneEqual(atendimentoEnums[i].getLabel(), linha[1].toUpperCase())) {
                                existeIgual = true;
                                break;
                            }
                        }

                        if (!existeIgual) {
                            linhaLog = "Linha " + numLinha + " - " + MensagemUtils.obterMensagem("ME009");
                            linhas.add(linhaLog);
                            quantidadeRejeitados += 1;
                            continue;
                        } else if (this.isOcorrenciasAutenticacao(linha[1]) && (linha.length == 3 || ObjectUtils.isNullOrEmpty(linha[2]))) {
                            linhaLog = "Linha " + numLinha + " - " + MensagemUtils.obterMensagem("ME017");
                            linhas.add(linhaLog);
                            quantidadeRejeitados += 1;
                            continue;
                        }

                    } else {
                        linhaLog = "Linha " + numLinha + " - " + MensagemUtils.obterMensagem(ConstsSiredExtra.ERRO_CAMPO_OCORRENCIA_ATENDIMENTO_VAZIO);
                        linhas.add(linhaLog);
                        quantidadeRejeitados += 1;
                        continue;
                    }
                }

                if (linha.length > 3) {
                    if (!Util.isNullOuVazio(linha[2])) {
                        TipoSuporteEnum[] tipoSuporteEnums = TipoSuporteEnum.values();

                        boolean existeSuporteIgual = false;

                        Metaphone metaTipoSuporte = new Metaphone();
                        for (int i = 0; i < tipoSuporteEnums.length; i++) {
                            if (metaTipoSuporte.isMetaphoneEqual(tipoSuporteEnums[i].getDescricao(), linha[2].toUpperCase())) {
                                if ((tipoSuporteEnums[i].name().equalsIgnoreCase(TipoSuporteEnum.MICROFICHA.name()) || tipoSuporteEnums[i].name()
                                        .equalsIgnoreCase(TipoSuporteEnum.MICROFILME.name())) && (linha[2].length() < 8)) {
                                    existeSuporteIgual = false;
                                    break;
                                }

                                existeSuporteIgual = true;
                                break;
                            }
                        }

                        if (!existeSuporteIgual) {
                            linhaLog = "Linha " + numLinha + " - " + MensagemUtils.obterMensagem(ConstsSiredExtra.ERRO_TIPO_SUPORTE_VAZIO);
                            linhas.add(linhaLog);
                            quantidadeRejeitados += 1;
                            continue;
                        }
                    }
                }

                if (linha.length > 3) {
                    if (!Util.isNullOuVazio(linha[3])) {
                        if (Util.isDigit(linha[3])) {
                            Integer qtdDisponibilizada = Integer.parseInt(linha[3]);

                            if (qtdDisponibilizada.intValue() <= 0) {
                                linhaLog = "Linha " + numLinha + " " + MensagemUtils.obterMensagem("ME018");
                                linhas.add(linhaLog);
                                quantidadeRejeitados += 1;
                                continue;
                            }
                        } else {
                            linhaLog = "Linha " + numLinha + " - " + MensagemUtils.obterMensagem(ConstsSiredExtra.ERRO_QTD_DISPONIBILIZADA_INVALIDA);
                            linhas.add(linhaLog);
                            quantidadeRejeitados += 1;
                            continue;
                        }
                    }
                } else {
                    linhaLog = "Linha " + numLinha + " - " + MensagemUtils.obterMensagem(ConstsSiredExtra.ERRO_QTD_DISPONIBILIZADA_INVALIDA);
                    linhas.add(linhaLog);
                    quantidadeRejeitados += 1;
                    continue;
                }

                TramiteRequisicaoVO tramiteRequisicao = new TramiteRequisicaoVO();
                tramiteRequisicao.setCodigoUsuario(JSFUtil.getUsuario().getEmail());
                tramiteRequisicao.setDataHora(Calendar.getInstance().getTime());
                tramiteRequisicao.setQtdDisponibilizadaDocumento(linha.length == 4 && Util.isDigit(linha[3]) ? Integer.parseInt(linha[3]) : 0);
                tramiteRequisicao.setArquivoLote(arquivoLoteVO);
                tramiteRequisicao.setRequisicao(requisicaoVO);
                tramiteRequisicao.setSituacaoRequisicao(situacaoRequisicao);

                OcorrenciaAtendimentoEnum[] atendimentoEnums = OcorrenciaAtendimentoEnum.values();

                Metaphone metaphone = new Metaphone();

                for (int i = 0; i < atendimentoEnums.length; i++) {
                    if (metaphone.isMetaphoneEqual(atendimentoEnums[i].getLabel(), linha[1])) {
                        OcorrenciaAtendimentoVO ocorrencia = new OcorrenciaAtendimentoVO();
                        ocorrencia.setId(atendimentoEnums[i].getValor());
                        tramiteRequisicao.setOcorrencia(ocorrencia);
                        break;
                    }
                }

                TipoSuporteEnum[] tipoSuporteEnums = TipoSuporteEnum.values();
                Metaphone metaTipoSuporte = new Metaphone();
                for (int i = 0; i < tipoSuporteEnums.length; i++) {
                    if (linha.length > 2) {
                        if (metaTipoSuporte.isMetaphoneEqual(tipoSuporteEnums[i].getDescricao(), linha[2])) {
                            SuporteVO suporte = new SuporteVO();
                            suporte.setId(tipoSuporteEnums[i].getValor());
                            tramiteRequisicao.setSuporte(suporte);
                            break;
                        }
                    }
                }

                tramitesRequisicao.add(tramiteRequisicao);
            }
        }

        return tramitesRequisicao;
    }

    public void downloadArquivoLog(UploadedFile arquivo) throws IOException {
        if (!linhas.isEmpty()) {
            String pathLog = System.getProperty(Constantes.DIRETORIO_ARQUIVOS_LOTES);
            String nomeArquivo = FilenameUtils.getName(arquivo.getFileName());
            File logArq = new File(pathLog, nomeArquivo.substring(0, nomeArquivo.length() - 4) + "_rejeitados.txt");
            logArq.createNewFile();
            FileWriter fileWriter = new FileWriter(logArq, false);

            BufferedWriter writer = new BufferedWriter(fileWriter);

            StringBuilder builder = new StringBuilder();

            builder.append("Arquivo " + arquivo.getFileName() + " enviado com " + quantidadeLinhas + " itens." + FileUtils.SYSTEM_EOL);
            builder.append(quantidadeRejeitados + MENSAGENS_ITENS_REJEITADOS + FileUtils.SYSTEM_EOL);
            builder.append(FileUtils.SYSTEM_EOL);

            for (String string : linhas) {
                builder.append(string + FileUtils.SYSTEM_EOL);
            }

            builder.append(FileUtils.SYSTEM_EOL);
            builder.append(MENSAGENS_FINAL_LOG_GERADO_PELA_IMPORTAÇÃO + arquivo.getFileName() + FileUtils.SYSTEM_EOL);

            writer.write(builder.toString());
            writer.flush();
            writer.close();

            if (logArq.exists()) {
                arquivoDownload = builder.toString().getBytes();
                nomeArquivoLogLote = logArq.getName();
            }

            RequestContext.getCurrentInstance().execute("hideStatus();");
        }
    }

    private Boolean isOcorrenciasAutenticacao(String valor) {
        Metaphone m = new Metaphone();

        if ((m.isMetaphoneEqual(OcorrenciaAtendimentoEnum.DOC_DIGITAL.getDescricao(), valor.toUpperCase())
                || m.isMetaphoneEqual(OcorrenciaAtendimentoEnum.ORIGINAL_UNIDADE.getDescricao(), valor.toUpperCase()) || m.isMetaphoneEqual(
                OcorrenciaAtendimentoEnum.COPIA_AUTENTICADA.getDescricao(), valor.toUpperCase()))) {
            return true;
        }

        return false;
    }

    public byte[] getArquivoDownload() {
        return arquivoDownload;
    }

    public String getNomeArquivoLogLote() {
        return nomeArquivoLogLote;
    }

    public List<String> getLinhas() {
        return linhas;
    }

}
