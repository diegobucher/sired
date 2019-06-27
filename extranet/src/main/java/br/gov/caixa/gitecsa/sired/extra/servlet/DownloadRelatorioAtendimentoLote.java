package br.gov.caixa.gitecsa.sired.extra.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.FileUtils;

@WebServlet(urlPatterns = "/download/relatorio-atendimento-em-lote", asyncSupported = true)
public class DownloadRelatorioAtendimentoLote extends HttpServlet {

    private static final long serialVersionUID = -2238526780493802230L;

    private static final int BUFFER_SIZE = 4096;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String reportName = request.getParameter("report");
        response.setHeader("Content-Disposition", "attachment; filename=" + reportName);
        response.setCharacterEncoding("UTF-8");

        try {
            this.doDonwload(response, reportName);
        } catch (IOException e) {
            response.sendRedirect(request.getContextPath() + "/errorpages/404");
        }
    }

    /**
     * Efetua o download do relatório de atendimento
     * 
     * @param response
     * @param reportName
     *            Nome do arquivo de relatório
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void doDonwload(HttpServletResponse response, String reportName) throws IOException, FileNotFoundException {

        File loteDir = FileUtils.createDirIfNotExists(System.getProperty(Constantes.EXTRANET_DIRETORIO_RELATORIOS));
        File reportFile = new File(loteDir.getCanonicalPath() + FileUtils.SYSTEM_FILE_SEPARATOR + reportName);

        ServletOutputStream out = response.getOutputStream();
        FileInputStream in = new FileInputStream(reportFile);

        int size;
        byte[] buffer = new byte[BUFFER_SIZE];

        while ((size = in.read(buffer)) > 0) {
            out.write(buffer, 0, size);
        }

        in.close();
        out.flush();
    }
}
