package br.gov.caixa.gitecsa.sired.extra.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.commons.lang.NotImplementedException;

import br.gov.caixa.gitecsa.sired.util.FileUtils;

public class RelatorioAtendimentoLoteCSV extends AbstractReportAtendimentoLote implements Reportable {

    private StringBuilder header;

    private StringBuilder detail;

    private StringBuilder footer;

    private String arquivoAtendimento;

    private String nome;

    public RelatorioAtendimentoLoteCSV(String arquivoAtendimento) {

        this.header = new StringBuilder();
        this.detail = new StringBuilder();
        this.footer = new StringBuilder();

        this.arquivoAtendimento = arquivoAtendimento;
    }

    @Override
    public void createHeader() {
        this.header = new StringBuilder();
        this.header.append(String.format("Arquivo \"%s\" enviado com %s itens.", this.arquivoAtendimento, this.getTotalAtendimentos()));
        this.header.append(FileUtils.SYSTEM_EOL);
        this.header.append(String.format("Atendimentos realizados com sucesso: %s.", this.getTotalAtendimentosRealizados()));
        this.header.append(FileUtils.SYSTEM_EOL + FileUtils.SYSTEM_EOL);
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
        this.footer = new StringBuilder();
        this.footer.append(FileUtils.SYSTEM_EOL);
        this.footer.append(String.format("Final do log gerado pela importação do arquivo \"%s\".", this.arquivoAtendimento));
        this.footer.append(FileUtils.SYSTEM_EOL + FileUtils.SYSTEM_EOL);
    }

    @Override
    public void save(String name, String outputDir) {

        FileUtils.createDirIfNotExists(outputDir);
        File file = new File(outputDir + FileUtils.SYSTEM_FILE_SEPARATOR + name);

        try {

            PrintWriter writer = new PrintWriter(file);
            writer.print(this.toString());
            writer.flush();
            writer.close();

            this.nome = name;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getNome() {
        return this.nome;
    }

    @Override
    public String toString() {
        return this.header.toString() + this.detail.toString() + this.footer.toString();
    }

}
