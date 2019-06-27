package br.gov.caixa.gitecsa.sired.extra.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.MimeTypeEnum;
import br.gov.caixa.gitecsa.sired.extra.report.RelatorioAtendimentoLoteCSV;
import br.gov.caixa.gitecsa.sired.extra.schedule.AtendimentoLoteSchedule;
import br.gov.caixa.gitecsa.sired.extra.service.AtendimentoLoteService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;

@WebServlet(urlPatterns = "/upload/atendimento-em-lote", asyncSupported = true)
public class UploadAtendimentoLote extends HttpServlet {

    private static final long serialVersionUID = -5421914061093324290L;

    private static final int BUFFER_SIZE = 4096;

    private static final int THRESHOLD_SIZE = 5120;

    @EJB
    private AtendimentoLoteService atendimentoLoteService;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (isMultipart) {

            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(THRESHOLD_SIZE);
            factory.setRepository((File) getServletContext().getAttribute(ServletContext.TEMPDIR));

            ServletFileUpload upload = new ServletFileUpload(factory);
            response.setCharacterEncoding("UTF-8");

            try {

                UsuarioLdap usuario = (UsuarioLdap) request.getSession().getAttribute("usuario");
                FileItemIterator iterator = upload.getItemIterator(request);

                while (iterator.hasNext()) {
                    FileItemStream item = (FileItemStream) iterator.next();
                    if (!item.isFormField()) {
                        InputStream stream = item.openStream();
                        if (this.isArquivoZIP(item)) {
                            this.processamentoOffline(item, stream, usuario);
                            this.escreverMensagemSucesso(response, "MS041");
                        } else if (this.isArquivoCSV(item)) {
                            this.processamentoOnline(item, stream, usuario, response);
                        } else {

                            this.escreverMensagemErro(response, "ME025", item.getName());
                        }   
                        stream.close();
                        return;
                    }
                }

                this.escreverMensagemErro(response, "MA045");

            } catch (Exception e) {
                this.escreverMensagemErro(response, "MA049");
                Logger logger = LogUtils.getLogger(this.getClass().getName(), System.getProperty(Constantes.EXTRANET_LOG4J));
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Realiza o atendimento de modo online seguindo os passos abaixo:
     * 
     * <ul>
     * <li>O arquivo é gravado no diretório de documentos</li>
     * <li>Criar um arquivo no diretório de relatório para armazenar informações à respeito do processamento</li>
     * <li>Processar os arquivos individualmente</li>
     * <li>Disponibilizar o relatório dea atendimento para o usuário</li>
     * </ul>
     * 
     * @param item
     * @param stream
     * @param usuario
     * @param response
     * @throws IOException
     * @throws FileNotFoundException
     * @throws AppException
     */
    private void processamentoOnline(FileItemStream item, InputStream stream, UsuarioLdap usuario, HttpServletResponse response) throws IOException,
            FileNotFoundException, AppException {

        File loteDir = FileUtils.createDirIfNotExists(System.getProperty(Constantes.EXTRANET_DIRETORIO_ATENDIMENTOS));
        File file = new File(loteDir.getCanonicalPath() + FileUtils.SYSTEM_FILE_SEPARATOR + FilenameUtils.getName(item.getName()));

        FileOutputStream outputFile = new FileOutputStream(file);
        this.moverArquivo(outputFile, stream);
        outputFile.close();

        RelatorioAtendimentoLoteCSV relatorio = (RelatorioAtendimentoLoteCSV) this.atendimentoLoteService.atenderOnline(file, usuario);
        if (relatorio.getTotalAtendimentos() > relatorio.getTotalAtendimentosRealizados()) {
            response.getWriter().print("{\"hasErrors\": true, \"log\": \"" + relatorio.getNome() + "\"}");
        } else {
            response.getWriter().print("{\"hasErrors\": false, \"log\": \"\"}");
        }
    }

    /**
     * Realiza o atendimento em lote através de um processo em background seguindo os passos abaixo:
     * 
     * <ul>
     * <li>O arquivo compactado é enviado para o diretório de arquivos em lotes</li>
     * <li>É criada uma pasta temporária no padrão: temp_YYYY-MM-DD-HH-MM-SS, onde devem ser descompactados os arquivos para processamento</li>
     * <li>Criar um arquivo no diretório de relatório para armazenar informações à respeito do processamento</li>
     * <li>Processar os arquivos individualmente</li>
     * <li>Excluir diretório temporário e enviar o relatório ao usuário por e-mail</li>
     * </ul>
     * 
     * @param item
     * @param stream
     * @throws IOException
     * @throws FileNotFoundException
     * @throws AppException
     */
    private void processamentoOffline(FileItemStream item, InputStream stream, UsuarioLdap usuario) throws IOException, FileNotFoundException, AppException {

        File loteDir = FileUtils.createDirIfNotExists(System.getProperty(Constantes.EXTRANET_DIRETORIO_ARQUIVOS_LOTES));
        File file = new File(loteDir.getCanonicalPath() + FileUtils.SYSTEM_FILE_SEPARATOR + FilenameUtils.getName(item.getName()));

        FileOutputStream outputFile = new FileOutputStream(file);
        this.moverArquivo(outputFile, stream);
        outputFile.close();

        String tmpDir = System.getProperty(Constantes.EXTRANET_DIRETORIO_TEMP) + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());

        File outputDir = FileUtils.createDirIfNotExists(tmpDir);

        AtendimentoLoteSchedule tarefa = new AtendimentoLoteSchedule(file.getCanonicalPath(), outputDir.getCanonicalPath(), usuario);
        Timer timer = new Timer();
        timer.schedule(tarefa, new Date());
    }

    /**
     * Grava o arquivo enviado no disco
     * 
     * @param outputFile
     * @param stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void moverArquivo(FileOutputStream outputFile, InputStream stream) throws FileNotFoundException, IOException {

        BufferedInputStream in = null;
        BufferedOutputStream out = null;

        try {

            in = new BufferedInputStream(stream);
            out = new BufferedOutputStream(outputFile);

            int len;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            in.close();
            out.close();
        }
    }

    /**
     * Identifica se o arquivo enviado é um CSV.
     * 
     * Permite os mimetypes: text/plain, text/csv e application/vnd.ms-excel com extensão <b>.csv</b>.
     * 
     * @param arquivo
     * @return <b>True</b> se trata-se de um arquivo CSV e <b>False</b> caso contrário
     */
    private Boolean isArquivoCSV(FileItemStream arquivo) {
        return arquivo.getName().toLowerCase().endsWith(MimeTypeEnum.CSV.getExtension())
                || arquivo.getName().toLowerCase().endsWith(MimeTypeEnum.TXT.getExtension());
    }

    /**
     * Identifica se o arquivo é um ZIP.
     * 
     * @param arquivo
     * @return <b>True</b> se trata-se de um arquivo compactado no formato ZIP e <b>False</b> caso contrário
     */
    private Boolean isArquivoZIP(FileItemStream arquivo) {
        return arquivo.getName().toLowerCase().endsWith(MimeTypeEnum.ZIP_COMPRESSED.getExtension());
    }

    private void escreverMensagemErro(HttpServletResponse response, String chaveBundle) throws IOException {
        response.getWriter().print(String.format("{\"error\": \"%s\"}", MensagemUtils.obterMensagem(chaveBundle)));
    }
    
    private void escreverMensagemErro(HttpServletResponse response, String chaveBundle, String parametro) throws IOException {
        response.getWriter().print(String.format("{\"error\": \"%s\"}", MensagemUtils.obterMensagem(chaveBundle, parametro)));
    }

    private void escreverMensagemSucesso(HttpServletResponse response, String chaveBundle) throws IOException {
        response.getWriter().print(String.format("{\"message\": \"%s\"}", MensagemUtils.obterMensagem(chaveBundle)));
    }
}
