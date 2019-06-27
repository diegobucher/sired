package br.gov.caixa.gitecsa.sired.extra.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.vo.ArquivoLoteVO;

public class RelatorioAtendimentoLoteBatch extends AbstractReportAtendimentoLote implements Reportable {

    private StringBuilder header;

    private StringBuilder detail;

    private StringBuilder footer;

    private ArquivoLoteVO arquivoLote;

    private File file;

    public RelatorioAtendimentoLoteBatch(ArquivoLoteVO entity) {

        this.header = new StringBuilder();
        this.detail = new StringBuilder();
        this.footer = new StringBuilder();

        this.arquivoLote = entity;
    }

    @Override
    public void createHeader() {
        this.header.append(Constantes.LOG_MARCADOR);
        this.header.append("SUMÁRIO:");
        this.header.append(FileUtils.SYSTEM_EOL);
        this.header.append(String.format("Arquivo %s enviado por %s às %s.", this.arquivoLote.getNome(), this.arquivoLote.getCodigoUsuario(),
                DateUtils.format(this.arquivoLote.getDataEnvioArquivo(), DateUtils.DATETIME_FORMAT)));
        this.header.append(FileUtils.SYSTEM_EOL + FileUtils.SYSTEM_EOL);
        this.header.append(String.format("Total de arquivos de atendimento: %s.", this.getTotalArquivosAtendimento()));
        this.header.append(FileUtils.SYSTEM_EOL);
        this.header.append(String.format("Total de documentos digitais: %s.", this.getTotalDocumentosDigitais()));
        this.header.append(Constantes.LOG_MARCADOR);
    }

    @Override
    public void addDetail(String data) {
        this.detail.append(data + FileUtils.SYSTEM_EOL);
    }

    @Override
    public void addDetail(Reportable report) {
        this.detail.append(report.toString());
    }

    @Override
    public void createFooter() {
        this.footer.append(Constantes.LOG_MARCADOR);
        this.footer.append(String.format("Total de Requisições atendidas com sucesso: %s.", this.getTotalAtendimentosRealizados()));
        this.footer.append(FileUtils.SYSTEM_EOL);
        this.footer.append(String.format("Total de documentos digitais importados com sucesso: %s.", this.getTotalDocumentosDigitaisImportados()));
        this.footer.append(FileUtils.SYSTEM_EOL + FileUtils.SYSTEM_EOL);
        this.footer.append(String.format("Final do log gerado pela importação do arquivo %s.", this.arquivoLote.getNome()));
        this.footer.append(FileUtils.SYSTEM_EOL);
        this.footer.append(Constantes.LOG_MARCADOR);
    }

    @Override
    public void save(String name, String outputDir) {

        FileUtils.createDirIfNotExists(outputDir);
        this.file = new File(outputDir + FileUtils.SYSTEM_FILE_SEPARATOR + name);

        try {

            PrintWriter writer = new PrintWriter(file);
            writer.print(this.header);
            writer.print(this.detail);
            writer.print(this.footer);
            writer.flush();
            writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        return this.file;
    }

}
