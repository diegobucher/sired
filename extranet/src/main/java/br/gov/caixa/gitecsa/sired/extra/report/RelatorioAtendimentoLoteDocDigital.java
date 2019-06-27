package br.gov.caixa.gitecsa.sired.extra.report;

import org.apache.commons.lang.NotImplementedException;

import br.gov.caixa.gitecsa.sired.util.FileUtils;

public class RelatorioAtendimentoLoteDocDigital extends AbstractReportAtendimentoLote implements Reportable {

    public static final String SUFIXO_NOME_RELATORIO = "YYMMddHHmm";

    private StringBuilder detail = new StringBuilder();

    @Override
    public void createHeader() {
    }

    @Override
    public void addDetail(String data) {
        this.detail.append(data + FileUtils.SYSTEM_EOL);
    }

    @Override
    public void addDetail(Reportable report) {
        throw new NotImplementedException();
    }

    @Override
    public void createFooter() {
    }

    @Override
    public void save(String name, String outputDir) {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        return this.detail.toString();
    }
}
