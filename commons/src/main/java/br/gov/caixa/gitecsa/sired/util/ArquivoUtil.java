package br.gov.caixa.gitecsa.sired.util;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Classe utilitária para operações de I/O.
 * 
 * @author jloliveiras
 * 
 */
public class ArquivoUtil {
    /**
     * Método que lista os arquivos de um diretório.
     * 
     * @param pasta
     * @return File[]
     * @throws Exception
     */
    public static final File[] listarArquivos(String pasta) throws Exception {
        File diretorio;
        try {
            diretorio = new File(pasta);
        } catch (Exception e) {
            throw new Exception("Erro ao ler diretório");
        }
        if (!diretorio.isDirectory()) {
            throw new Exception("Diretório não encontrado");
        }
        File[] arquivos = diretorio.listFiles();
        Arrays.sort(arquivos, new Comparator<File>() {
            @Override
            public int compare(File arquivo1, File arquivo2) {
                return ArquivoUtil.getNomeArquivo(arquivo1).compareTo(ArquivoUtil.getNomeArquivo(arquivo2));
            }
        });
        return arquivos;
    }

    public static final String getNomeArquivo(File arquivo) {
        return arquivo.getName();
    }
}
