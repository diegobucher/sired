package br.gov.caixa.gitecsa.sired.arquitetura.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;

public interface DataExportable<T> {
    List<T> getData();
    void setData(List<T> data);
    File export(String filename) throws FileNotFoundException, IOException, AppException;
}
