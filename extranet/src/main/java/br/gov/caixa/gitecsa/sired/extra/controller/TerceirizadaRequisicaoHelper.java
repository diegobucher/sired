package br.gov.caixa.gitecsa.sired.extra.controller;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.Util;

public class TerceirizadaRequisicaoHelper {

    private static final String CONTENT_TYPE_PDF = "application/txt";

    private static final String PONTO_EXTENSAO = ".";

    private static final String SUBLINHADO = "_";

    private static final String FORMATO_DATAHORA_YYMMDDHHMM = "yyMMddHHmm";

    private static final String SIGLA_SISTEMA = "RED";

    private static final String SIGLA_DEMANDA_REQ = "REQ";

    public static String gerarNomeArquivo(String arquivoDisponibilizado, String numeroIdentificacao) {
        String result = StringUtils.EMPTY;

        if ((arquivoDisponibilizado != null) && (!arquivoDisponibilizado.isEmpty())) {
            int inicio = arquivoDisponibilizado.trim().length() - 3;
            String extensao = arquivoDisponibilizado.trim().substring(inicio);
            String data = Util.formatData(Calendar.getInstance().getTime(), FORMATO_DATAHORA_YYMMDDHHMM);

            StringBuilder sb = new StringBuilder();
            sb.append(SIGLA_SISTEMA);
            sb.append(SUBLINHADO);
            sb.append(SIGLA_DEMANDA_REQ);
            sb.append(SUBLINHADO);
            sb.append(numeroIdentificacao);
            sb.append(SUBLINHADO);
            sb.append(data);
            sb.append(PONTO_EXTENSAO);
            sb.append(extensao);

            result = sb.toString();
        }

        return result;
    }

    public static void downloadArquivoLog(String nomeArquivoLogLote, byte[] arquivoDownload) throws IOException {
        HttpServletResponse res = RequestUtils.getServletResponse();
        res.setContentType(CONTENT_TYPE_PDF);
        res.setHeader("Content-Disposition", "attachment; filename=\"" + nomeArquivoLogLote + "\"");
        ServletOutputStream outputStream = res.getOutputStream();
        outputStream.write(arquivoDownload, 0, arquivoDownload.length);
        outputStream.flush();
        outputStream.close();
    }

}
