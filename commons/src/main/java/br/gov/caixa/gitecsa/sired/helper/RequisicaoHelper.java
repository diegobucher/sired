package br.gov.caixa.gitecsa.sired.helper;

import java.util.Date;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class RequisicaoHelper {

    public static RequisicaoVO clonar(final RequisicaoVO requisicao, final UsuarioLdap usuario, final UnidadeVO unidadeSolicitante) {

        RequisicaoVO clone = (RequisicaoVO) ObjectUtils.clone(requisicao);
        
        if (!ObjectUtils.isNullOrEmpty(clone)) {
            clone.setId(null);
            clone.setCodigoRequisicao(null);
            clone.setEmpresaContrato(null);
            clone.setBase(null);
            clone.setPrazoAtendimento(null);
            clone.setTramiteRequisicoes(null);
            clone.setTramiteRequisicaoAtual(null);
            clone.setArquivoJustificativa(null);
            
            clone.setUnidadeSolicitante(unidadeSolicitante);
            clone.setCodigoUsuarioAbertura(usuario.getNuMatricula());
            clone.setDataHoraAbertura(new Date());
            
            RequisicaoDocumentoVO reqDocClone = (RequisicaoDocumentoVO) ObjectUtils.clone(requisicao.getRequisicaoDocumento());
            if (!ObjectUtils.isNullOrEmpty(reqDocClone)) {
                reqDocClone.setId(null);
                clone.setRequisicaoDocumento(reqDocClone);
            }
        }

        return clone;
    }
}
