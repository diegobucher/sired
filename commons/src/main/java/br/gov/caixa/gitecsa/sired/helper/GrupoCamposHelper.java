package br.gov.caixa.gitecsa.sired.helper;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoCampoEnum;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;

public class GrupoCamposHelper {
    
    /**
     * Seta os valores dos campos dinâmicos do formulário através de reflexão.
     * O método percorre todos os atributos declarados na entidade que possuem a anotação @column com o nome de coluna igual ao do
     * campo dinâmico. Ao encontra-lo, o método faz uma chamada ao set correspondente passando como parâmetro o valor contido em GrupoCampo.getValor().
     * 
     * @param entity
     *            Entidade que possua relação com grupo campos
     * @param grupoCampos
     *            Campos dinâmicos
     * @throws AppException
     */
    public static BaseEntity setValorCamposDinamicos(BaseEntity entity, final Set<GrupoCampoVO> grupoCampos) throws AppException {
        Class<?> objClass = entity.getClass();

        for (GrupoCampoVO grupoCampo : grupoCampos) {
            // Obtém os campos (fields) por reflexão
            for (Field field : objClass.getDeclaredFields()) {
                // Tenta ler a anotação "@Column(name=...)"
                Column annotation = field.getAnnotation(Column.class);
                if (!ObjectUtils.isNullOrEmpty(annotation) && grupoCampo.getCampo().getNome().equalsIgnoreCase(annotation.name())) {
                    if (grupoCampo.getCampo().getTipo().equals(TipoCampoEnum.DATA)) {
                        ObjectUtils.invokeSet(entity, field, grupoCampo.getValorData());
                    } else if (grupoCampo.getCampo().getTipo().equals(TipoCampoEnum.NUMERICO)) {
                        String valor = StringUtils.defaultIfEmpty(grupoCampo.getValor(), StringUtils.EMPTY).replaceAll("[^0-9,\\.]*", "");
                    		
                    	if (StringUtils.isNotBlank(valor)) {
                    		if (field.getType().equals(Integer.class)) {
                    			valor = valor.replaceAll("[^0-9]*", "");
                    			ObjectUtils.invokeSet(entity, field, Integer.valueOf(valor));
                    		} else if(field.getType().equals(BigDecimal.class)) {
                    		  valor = valor.replaceAll("[^0-9]*", "");
                          ObjectUtils.invokeSet(entity, field, new BigDecimal(valor));
                    		} else {
                    			ObjectUtils.invokeSet(entity, field, valor);
                    		}
                    	} else {
                    		ObjectUtils.invokeSet(entity, field, null);
                    	}
                    } else {
                        ObjectUtils.invokeSet(entity, field, grupoCampo.getValor());
                    }

                    break;
                }
            }
        }

        return entity;
    }
    
    /**
     * Obtém os valores dos campos dinâmicos do formulário através de reflexão.
     * O método percorre todos os atributos declarados na entidade que possuem a anotação @column com o nome de coluna igual ao do
     * campo dinâmico. Ao encontra-lo, o método faz uma chamada ao get correspondente.
     * 
     * @param entity
     *            Entidade que possua relação com grupo campos
     * @param grupoCampos
     *            Campos dinâmicos
     * @throws AppException
     */
    public static Set<GrupoCampoVO> getValorCamposDinamicos(final BaseEntity entity, Set<GrupoCampoVO> grupoCampos) throws AppException {

        Class<?> objClass = entity.getClass();

        for (GrupoCampoVO grupoCampo : grupoCampos) {
            // Obtém os campos (fields) por reflexão
            for (Field field : objClass.getDeclaredFields()) {
                // Tenta ler a anotação "@Column(name=...)"
                Column annotation = field.getAnnotation(Column.class);
                if (!ObjectUtils.isNullOrEmpty(annotation) && grupoCampo.getCampo().getNome().equalsIgnoreCase(annotation.name())) {
                    if (grupoCampo.getCampo().getTipo().equals(TipoCampoEnum.DATA)) {
                        grupoCampo.setValorData((Date) ObjectUtils.invokeGet(entity, field));
                    } else if (grupoCampo.getCampo().getTipo().equals(TipoCampoEnum.NUMERICO) && field.getType().equals(Integer.class)) {
                        Object valor = ObjectUtils.invokeGet(entity, field);
                        if (!ObjectUtils.isNullOrEmpty(valor)) {
                            grupoCampo.setValor(valor.toString());
                        }
                    }else if(grupoCampo.getCampo().getTipo().equals(TipoCampoEnum.NUMERICO) && field.getType().equals(BigDecimal.class)) {
                      Object valor = ObjectUtils.invokeGet(entity, field);
                      if (!ObjectUtils.isNullOrEmpty(valor)) {
                          grupoCampo.setValor(valor.toString());
                      }else {
                        grupoCampo.setValor(null);
                      }
                    } else {
                        grupoCampo.setValor((String) ObjectUtils.invokeGet(entity, field));
                    }

                    break;
                }
            }
        }

        return grupoCampos;
    }
    
    /**
     * Verifica se todos os campos dinâmicos do formulário foram preenchidos
     * 
     * @param entity
     *            Entidade que possua relação com grupo campos
     * @param grupoCampos
     *            Campos dinâmicos
     * @throws AppException
     */
    public static void validarCamposDinamicosObrigatorios(BaseEntity entity, final Set<GrupoCampoVO> grupoCampos) throws RequiredException, AppException {
        
    	Class<?> objClass = entity.getClass();
        List<String> msgValidacao = new ArrayList<String>();
        
        for (GrupoCampoVO grupoCampo : grupoCampos) {
            // Obtém os campos (fields) por reflexão
            for (Field field : objClass.getDeclaredFields()) {
                // Tenta ler a anotação "@Column(name=...)"
                Column annotation = field.getAnnotation(Column.class);
                if (!ObjectUtils.isNullOrEmpty(annotation) && grupoCampo.getCampo().getNome().equalsIgnoreCase(annotation.name())) {
                	// Verifica se o campo é obrigatório e se está preenchido
                	if (SimNaoEnum.SIM.equals(grupoCampo.getCampoObrigatorio()) && ObjectUtils.isNullOrEmpty(ObjectUtils.invokeGet(entity, field))) {
                		String nomeCampo = (String)ObjectUtils.defaultIfNull(grupoCampo.getLegenda(), grupoCampo.getCampo().getDescricao());
                		msgValidacao.add(MensagemUtils.obterMensagem("MA001", nomeCampo.toUpperCase()));
                	}
                }
            }
        }
        
        if (!msgValidacao.isEmpty()) {
        	throw new RequiredException(msgValidacao);
        }
    }

    /**
     * Seta os valores dos campos dinâmicos do formulário através de reflexão.
     * O método percorre todos os atributos declarados na classe <b>RequisicaoDocumentoVO</b> que possuem a anotação @column com o nome de coluna igual ao do
     * campo dinâmico. Ao encontra-lo, o método faz uma chamada ao set correspondente passando como parâmetro o valor contido em GrupoCampo.getValor().
     * 
     * @param requisicao
     *            Requisição selecionada
     * @param grupoCampos
     *            Campos dinâmicos
     * @throws AppException
     */
    public static RequisicaoVO setValorCamposDinamicos(RequisicaoVO requisicao, final Set<GrupoCampoVO> grupoCampos) throws AppException {
        requisicao.setRequisicaoDocumento((RequisicaoDocumentoVO)setValorCamposDinamicos((BaseEntity)requisicao.getRequisicaoDocumento(), grupoCampos));
        return requisicao;
    }

    /**
     * Obtém os valores dos campos dinâmicos do formulário através de reflexão.
     * O método percorre todos os atributos declarados na classe <b>RequisicaoDocumentoVO</b> que possuem a anotação @column com o nome de coluna igual ao do
     * campo dinâmico. Ao encontra-lo, o método faz uma chamada ao get correspondente.
     * 
     * @param requisicao
     *            Requisição selecionada
     * @param grupoCampos
     *            Campos dinâmicos
     * @throws AppException
     */
    public static Set<GrupoCampoVO> getValorCamposDinamicos(final RequisicaoVO requisicao, Set<GrupoCampoVO> grupoCampos) throws AppException {
          return getValorCamposDinamicos((BaseEntity)requisicao.getRequisicaoDocumento(), grupoCampos);
    }
    
    /**
     * Seta os valores dos campos dinâmicos do formulário através de reflexão.
     * O método percorre todos os atributos declarados na classe <b>RemessaDocumentoVO</b> que possuem a anotação @column com o nome de coluna igual ao do
     * campo dinâmico. Ao encontra-lo, o método faz uma chamada ao set correspondente passando como parâmetro o valor contido em GrupoCampo.getValor().
     * 
     * @param remessaDoc
     *            Remessa documento selecionada
     * @param grupoCampos
     *            Campos dinâmicos
     * @throws AppException
     */
    public static RemessaDocumentoVO setValorCamposDinamicos(RemessaDocumentoVO remessaDoc, final Set<GrupoCampoVO> grupoCampos) throws AppException {
        return (RemessaDocumentoVO)setValorCamposDinamicos((BaseEntity)remessaDoc, grupoCampos);
    }

    /**
     * Obtém os valores dos campos dinâmicos do formulário através de reflexão.
     * O método percorre todos os atributos declarados na classe <b>RemessaDocumentoVO</b> que possuem a anotação @column com o nome de coluna igual ao do
     * campo dinâmico. Ao encontra-lo, o método faz uma chamada ao get correspondente.
     * 
     * @param remessaDoc
     *            Remessa documento selecionada
     * @param grupoCampos
     *            Campos dinâmicos
     * @throws AppException
     */
    public static Set<GrupoCampoVO> getValorCamposDinamicos(final RemessaDocumentoVO remessaDoc, Set<GrupoCampoVO> grupoCampos) throws AppException {
          return getValorCamposDinamicos((BaseEntity)remessaDoc, grupoCampos);
    }
}
