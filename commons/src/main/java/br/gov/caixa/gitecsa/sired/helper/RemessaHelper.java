package br.gov.caixa.gitecsa.sired.helper;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class RemessaHelper {

  public static RemessaVO clonar(final RemessaVO remessa, final UsuarioLdap usuario, final UnidadeVO unidadeSolicitante) {

    RemessaVO clone = new RemessaVO();
    
    clone.setCodigoRemessaTipoC(remessa.getCodigoRemessaTipoC());
    clone.setCodigoUsuarioAbertura(usuario.getNuMatricula());
    clone.setDataHoraAbertura(remessa.getDataHoraAbertura());
    clone.setDocumento(remessa.getDocumento());
    clone.setLacre(remessa.getLacre());
    clone.setUnidadeSolicitante(unidadeSolicitante);
    
    return clone;
  }
}
