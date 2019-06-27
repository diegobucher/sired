package br.gov.caixa.gitecsa.sired.extra.report;

import java.util.HashMap;
import java.util.Map;

import br.gov.caixa.gitecsa.sired.enumerator.MimeTypeEnum;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;

public abstract class AbstractReportAtendimentoLote {

    private Map<String, Map<String, Boolean>> itens;

    private Long totalArquivosAtendimento = 0L;

    private Long totalDocumentosDigitais = 0L;

    private Long totalAtendimentos = 0L;

    private Long totalAtendimentosRealizados = 0L;

    private Long totalDocumentosDigitaisImportados = 0L;

    public void adicionar(String arquivo, String codigo, Boolean sucesso) {
        if (ObjectUtils.isNullOrEmpty(this.itens)) {
            this.itens = new HashMap<String, Map<String, Boolean>>();
        }

        if (!this.itens.containsKey(arquivo)) {
            this.itens.put(arquivo, new HashMap<String, Boolean>());
        }

        this.itens.get(arquivo).put(codigo, sucesso);
    }

    public void adicionar(String arquivo, Map<String, Boolean> itens) {
        if (ObjectUtils.isNullOrEmpty(this.itens)) {
            this.itens = new HashMap<String, Map<String, Boolean>>();
        }

        this.itens.put(arquivo, itens);
    }

    public Map<String, Map<String, Boolean>> getItens() {
        return this.itens;
    }

    public Map<String, Boolean> getItens(String arquivo) {
        if (!ObjectUtils.isNullOrEmpty(this.itens)) {
            return this.itens.get(arquivo);
        }
        return null;
    }

    public void buildSumario() {
        if (!ObjectUtils.isNullOrEmpty(this.itens)) {
            for (String nome : this.itens.keySet()) {
                if (nome.endsWith(MimeTypeEnum.CSV.getExtension()) || nome.endsWith(MimeTypeEnum.TXT.getExtension())) {
                    Map<String, Boolean> atendimentos = this.itens.get(nome);
                    if (!ObjectUtils.isNullOrEmpty(atendimentos)) {
                        for (String codigo : atendimentos.keySet()) {
                            if (atendimentos.get(codigo)) {
                                this.totalAtendimentosRealizados++;
                            }
                            this.totalAtendimentos++;
                        }
                    }
                    this.totalArquivosAtendimento++;
                } else if (nome.endsWith(MimeTypeEnum.ZIP_COMPRESSED.getExtension()) || nome.endsWith(MimeTypeEnum.PDF.getExtension())) {
                    Map<String, Boolean> docsDigitais = this.itens.get(nome);
                    if (!ObjectUtils.isNullOrEmpty(docsDigitais)) {
                        for (String codigo : docsDigitais.keySet()) {
                            if (docsDigitais.get(codigo)) {
                                this.totalDocumentosDigitaisImportados++;
                            }
                        }
                    }
                    this.totalDocumentosDigitais++;
                }
            }
        }
    }

    public Boolean hasCodigo(String arquivo, Long codigo) {
        if (!ObjectUtils.isNullOrEmpty(this.itens)) {
            for (String nome : this.itens.keySet()) {
                if (nome.endsWith(MimeTypeEnum.CSV.getExtension()) || nome.endsWith(MimeTypeEnum.TXT.getExtension())) {
                    Map<String, Boolean> atendimentos = this.itens.get(nome);
                    if (!ObjectUtils.isNullOrEmpty(atendimentos) && atendimentos.containsKey(codigo)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public Long getTotalArquivosAtendimento() {
        return this.totalArquivosAtendimento;
    }

    public Long getTotalDocumentosDigitais() {
        return this.totalDocumentosDigitais;
    }

    public Long getTotalAtendimentos() {
        return this.totalAtendimentos;
    }

    public Long getTotalAtendimentosRealizados() {
        return this.totalAtendimentosRealizados;
    }

    public Long getTotalDocumentosDigitaisImportados() {
        return this.totalDocumentosDigitaisImportados;
    }
}
