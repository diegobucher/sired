package br.gov.caixa.gitecsa.sired.extra.report;

public interface Reportable {

    public void createHeader();

    public void addDetail(String data);

    public void addDetail(Reportable report);

    public void createFooter();

    public void save(String name, String outputDir);

}
