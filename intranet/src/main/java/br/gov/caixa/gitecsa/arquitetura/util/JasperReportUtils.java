package br.gov.caixa.gitecsa.arquitetura.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.primefaces.model.StreamedContent;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class JasperReportUtils {
  
  /**
   * Executa a geração do relatório.
   * 
   * @param jasper Input stream para o jrxml.
   * @param filename Nome do arquivo de saída.
   * @param parameters Parâmetros de entrada
   * @return O conteúdo em stream do relatório.
   * @throws JRException
   * @throws IOException 
   * @throws BusinessException 
   */
  public static StreamedContent run(final InputStream jasper, final String filename, final Map<String, Object> parameters) throws JRException, BusinessException, IOException {
    parameters.put("REPORT_LOCALE", new Locale("pt", "BR"));
    return run(jasper, filename, parameters, null);
  }

  
  /**
   * Executa a geração do relatório.
   * 
   * @param jasper Input stream para o jrxml.
   * @param filename Nome do arquivo de saída.
   * @param parameters Parâmetros de entrada
   * @param dataSource Datasource.
   * @return O conteúdo em stream do relatório.
   * @throws JRException
   * @throws IOException 
   * @throws BusinessException 
   */
  public static StreamedContent run(final InputStream jasper, final String filename, final Map<String, Object> parameters,
      List<?> dataSource) throws JRException, IOException, BusinessException {
    parameters.put("REPORT_LOCALE", new Locale("pt", "BR"));
    JRDataSource ds = null;
    if (dataSource != null && !dataSource.isEmpty()) {
      ds = new JRBeanCollectionDataSource(dataSource);
    } else {
      ds = new JREmptyDataSource();
    }

    File report = new File(filename);
    byte[] out = JasperRunManager.runReportToPdf(jasper, parameters, ds);

    FileOutputStream outputStream = new FileOutputStream(report);
    outputStream.write(out);
    outputStream.close();

    return RequestUtils.download(report, filename);
  }

}
