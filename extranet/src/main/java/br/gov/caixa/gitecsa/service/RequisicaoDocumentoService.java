package br.gov.caixa.gitecsa.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;

// @Stateless
@Deprecated
public class RequisicaoDocumentoService extends AbstractService<RequisicaoDocumentoVO> {

    private static final long serialVersionUID = 1L;

    @Override
    protected void validaCampos(RequisicaoDocumentoVO entity) {

    }

    @Override
    protected void validaRegras(RequisicaoDocumentoVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(RequisicaoDocumentoVO entity) {

    }

    @Override
    public RequisicaoDocumentoVO getById(Object id) {
        RequisicaoDocumentoVO vo = super.getById(id);
        vo.getUnidadeGeradora();
        vo.getTipoDemanda();
        return vo;
    }

    protected RequisicaoDocumentoVO saveImpl(RequisicaoDocumentoVO requisicaoDocumento) throws AppException {
        try {
            entityManager.persist(requisicaoDocumento);
        } catch (Exception e) {
            mensagens.add(e.getMessage());
            if (!Util.isNullOuVazio(mensagens))
                throw new BusinessException(e.getMessage());
        }

        return requisicaoDocumento;
    }

    /**
     * Metodo que lê o arquivo cujo nome está no tramite da
     * requisição do documento atual e transforma em array
     * de bytes.
     * 
     * @param requisicaoDocumentoVO
     * @return
     * @throws Exception
     */
    public byte[] downloadDoArquivoDigitalizado(RequisicaoVO requisicaoVO) throws Exception {
        String path = System.getProperty(Constantes.PATH_ARQUIVOS_SERVIDOR_EXTRA);
        path = path + requisicaoVO.getTramiteRequisicaoAtual().getArquivoDisponibilizado();
        File arquivo = new File(path);

        if (arquivo.exists()) {
            return getFileBytes(arquivo);
        }
        return null;
    }

    public static byte[] getFileBytes(File file) throws Exception {
        int len = (int) file.length();
        byte[] sendBuf = new byte[len];
        FileInputStream inFile = null;
        try {
            inFile = new FileInputStream(file);
            inFile.read(sendBuf, 0, len);
            inFile.close();

        } catch (FileNotFoundException fnfex) {
            throw fnfex;
        } catch (IOException ioex) {
            throw ioex;
        }
        return sendBuf;
    }

    public boolean existeArquivoParaDownload(RequisicaoVO requisicaoVO) {
        String path = System.getProperty(Constantes.PATH_ARQUIVOS_SERVIDOR_EXTRA);
        if (!Util.isNullOuVazio(requisicaoVO.getTramiteRequisicaoAtual())
                && !Util.isNullOuVazio(requisicaoVO.getTramiteRequisicaoAtual().getArquivoDisponibilizado())) {
            path = path + requisicaoVO.getTramiteRequisicaoAtual().getArquivoDisponibilizado();
            File arquivo = new File(path);
            if (arquivo.exists()) {
                return true;
            }
        }
        return false;
    }

}
