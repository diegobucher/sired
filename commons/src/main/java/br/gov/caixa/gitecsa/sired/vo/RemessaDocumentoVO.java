package br.gov.caixa.gitecsa.sired.vo;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;

@Entity
@Table(name = "redtbc18_remessa_tipo_a_b", schema = Constantes.SCHEMADB_NAME)
public class RemessaDocumentoVO extends BaseEntity {

    private static final long serialVersionUID = -2879736529316573669L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_remessa_tipo_a_b", columnDefinition = "serial")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_remessa_c17", columnDefinition = "int4")
    private RemessaVO remessa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_documento_c01", columnDefinition = "int4")
    private DocumentoVO documento;
    
    @Column(name = "co_remessa", columnDefinition = "numeric", updatable = false)
    private Long codigoRemessa;

    @ManyToOne
    @JoinColumn(name = "nu_unidade_geradora_a02")
    private UnidadeVO unidadeGeradora;

    @Column(name = "co_usuario_ultima_alteracao", columnDefinition = "bpchar")
    private String codigoUsuarioUltimaAlteracao;

    @Column(name = "de_localizacao")
    private String descricaoLocalizacao;

    @Column(name = "nu_valor")
    private String nuValor;

    @Column(name = "no_nome")
    private String noNome;

    @Column(name = "nu_documento")
    private String nuDocumento;

    @Column(name = "nu_volume", columnDefinition = "int2")
    private Integer nuVolume;

    @Column(name = "nu_conta_inicio", columnDefinition = "bpchar")
    private String nuContaInicio;

    @Column(name = "nu_conta_fim", columnDefinition = "bpchar")
    private String nuContaFim;

    @Column(name = "nu_conta", columnDefinition = "bpchar")
    private String nuConta;

    @Column(name = "nu_operacao_a11_", columnDefinition = "bpchar")
    private String nuOperacao;

    @Column(name = "nu_digito_verificador", columnDefinition = "bpchar")
    private String nuDigitoVerificador;

    @Temporal(TemporalType.DATE)
    @Column(name = "dt_data_geracao")
    private Date dataGeracao;

    @Temporal(TemporalType.DATE)
    @Column(name = "dt_inicio")
    private Date dataInicio;

    @Temporal(TemporalType.DATE)
    @Column(name = "dt_fim")
    private Date dataFim;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_ultima_alteracao")
    private Date dataUltimaAlteracao;

    @Column(name = "nu_encerramento", columnDefinition = "int2")
    private Integer nuEncerramento;

    @Column(name = "nu_cnpj")
    private String numeroCnpj;

    @Column(name = "nu_cpf")
    private String numeroCpf;
    
    @Column(name = "no_nome_inicio", columnDefinition = "bpchar")
    private String nomeInicio;

    @Column(name = "no_nome_fim", columnDefinition = "bpchar")
    private String nomeFim;

    @Column(name = "nu_numero_inicio")
    private Integer numeroInicio;

    @Column(name = "nu_numero_fim")
    private Integer numeroFim;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_remessa_tipo_a_b_c18", columnDefinition = "int4")
    private RemessaDocumentoVO numeroRemessaTipoAB;
    
    @Column(name = "ic_alteracao_valida",  nullable = false, columnDefinition = "int2")
    private SituacaoAlteracaoRemessaEnum icAlteracaoValida;
    
    @Column(name = "nu_folder", columnDefinition = "numeric", length = 20, precision = 0)
    private BigDecimal numeroFolder;
    
    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (Long) id;

    }

    @Override
    public String getColumnOrderBy() {
        return null;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public RemessaVO getRemessa() {
        return remessa;
    }

    public void setRemessa(RemessaVO remessa) {
        this.remessa = remessa;
    }

    public UnidadeVO getUnidadeGeradora() {
        return unidadeGeradora;
    }

    public void setUnidadeGeradora(UnidadeVO unidadeGeradora) {
        this.unidadeGeradora = unidadeGeradora;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public String getDescricaoLocalizacao() {
        return descricaoLocalizacao;
    }

    public void setDescricaoLocalizacao(String descricaoLocalizacao) {
        this.descricaoLocalizacao = descricaoLocalizacao;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoUsuarioUltimaAlteracao() {
        return codigoUsuarioUltimaAlteracao;
    }

    public void setCodigoUsuarioUltimaAlteracao(String codigoUsuarioUltimaAlteracao) {
        this.codigoUsuarioUltimaAlteracao = codigoUsuarioUltimaAlteracao;
    }

    public String getNuValor() {
        return nuValor;
    }

    public void setNuValor(String nuValor) {
        this.nuValor = nuValor;
    }

    public String getNoNome() {
        return noNome;
    }

    public void setNoNome(String noNome) {
        this.noNome = noNome;
    }

    public String getNuDocumento() {
        return nuDocumento;
    }

    public void setNuDocumento(String nuDocumento) {
        this.nuDocumento = nuDocumento;
    }

    public Integer getNuVolume() {
        return nuVolume;
    }

    public void setNuVolume(Integer nuVolume) {
        this.nuVolume = nuVolume;
    }

    public String getNuContaInicio() {
        return nuContaInicio;
    }

    public void setNuContaInicio(String nuContaInicio) {
        this.nuContaInicio = nuContaInicio;
    }

    public String getNuContaFim() {
        return nuContaFim;
    }

    public void setNuContaFim(String nuContaFim) {
        this.nuContaFim = nuContaFim;
    }

    public String getNuConta() {
        return nuConta;
    }

    public void setNuConta(String nuConta) {
        this.nuConta = nuConta;
    }

    public String getNuOperacao() {
        return nuOperacao;
    }

    public void setNuOperacao(String nuOperacao) {
        this.nuOperacao = nuOperacao;
    }

    public String getNuDigitoVerificador() {
        return nuDigitoVerificador;
    }

    public void setNuDigitoVerificador(String nuDigitoVerificador) {
        this.nuDigitoVerificador = nuDigitoVerificador;
    }

    public Date getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(Date dataGeracao) {
        this.dataGeracao = dataGeracao;
    }

    public Integer getNuEncerramento() {
        return nuEncerramento;
    }

    public void setNuEncerramento(Integer nuEncerramento) {
        this.nuEncerramento = nuEncerramento;
    }

    public String getNumeroCnpj() {
        return numeroCnpj;
    }

    public void setNumeroCnpj(String cnpj) {
        if (!ObjectUtils.isNullOrEmpty(cnpj)) {
            cnpj = cnpj.replace(".", "").replace("/", "").replace("-", "");
        }
        this.numeroCnpj = cnpj;
    }

    public String getNumeroCpf() {
        return numeroCpf;
    }

    public void setNumeroCpf(String cpf) {
        if (!ObjectUtils.isNullOrEmpty(cpf)) {
            cpf = cpf.replace(".", "").replace("-", "");
        }
        this.numeroCpf = cpf;
    }
    
    public Long getCodigoRemessa() {
      return codigoRemessa;
    }

    public void setCodigoRemessa(Long codigoRemessa) {
      this.codigoRemessa = codigoRemessa;
    }

    public DocumentoVO getDocumento() {
      return documento;
    }

    public void setDocumento(DocumentoVO documento) {
      this.documento = documento;
    }
    
    public Date getDataUltimaAlteracao() {
      return dataUltimaAlteracao;
    }

    public void setDataUltimaAlteracao(Date dataUltimaAlteracao) {
      this.dataUltimaAlteracao = dataUltimaAlteracao;
    }

    public String getNomeInicio() {
      return nomeInicio;
    }

    public void setNomeInicio(String nomeInicio) {
      this.nomeInicio = nomeInicio;
    }

    public String getNomeFim() {
      return nomeFim;
    }

    public void setNomeFim(String nomeFim) {
      this.nomeFim = nomeFim;
    }

    public Integer getNumeroInicio() {
      return numeroInicio;
    }

    public void setNumeroInicio(Integer numeroInicio) {
      this.numeroInicio = numeroInicio;
    }

    public Integer getNumeroFim() {
      return numeroFim;
    }

    public void setNumeroFim(Integer numeroFim) {
      this.numeroFim = numeroFim;
    }
    
    @Transient
    public String getDataGeracaoFormatada() {
        return DateUtils.format(dataGeracao, DateUtils.DEFAULT_FORMAT);
    }

    @Transient
    public String getDataGeracaoPeriodoFormatado() {
        String dataGeracao = getDataGeracaoFormatada();
        String periodo = getPeriodoFormatado();
        if (!ObjectUtils.isNullOrEmpty(dataGeracao) && !ObjectUtils.isNullOrEmpty(periodo)) {
            return dataGeracao + ", " + periodo;
        }
        if (!ObjectUtils.isNullOrEmpty(dataGeracao)) {
            return dataGeracao;
        }
        return periodo;

    }

    @Transient
    public String getDataInicioFormatada() {
        return DateUtils.format(dataInicio, DateUtils.DEFAULT_FORMAT);
    }

    @Transient
    public String getDataFimFormatada() {
        return DateUtils.format(dataFim, DateUtils.DEFAULT_FORMAT);
    }

    @Transient
    public String getPeriodoFormatado() {
        if (dataInicio != null && dataFim != null) {
            return this.getDataInicioFormatada() + " - " + this.getDataFimFormatada();
        } else if (dataInicio != null) {
            return this.getDataInicioFormatada();
        } else if (dataFim != null) {
            return this.getDataFimFormatada();
        }

        return StringUtils.EMPTY;
    }

    @Transient
    public String getContasFormatada() {

        if (nuContaInicio != null && nuContaFim != null) {
            return nuContaInicio.trim() + " - " + nuContaFim.trim();
        } else if (nuContaInicio != null) {
            return nuContaInicio.trim();
        } else if (nuContaFim != null) {
            return nuContaFim.trim();
        }

        return StringUtils.EMPTY;
    }

    /**
     * @return the numeroRemessaTipoAB
     */
    public RemessaDocumentoVO getNumeroRemessaTipoAB() {
      return numeroRemessaTipoAB;
    }

    /**
     * @param numeroRemessaTipoAB the numeroRemessaTipoAB to set
     */
    public void setNumeroRemessaTipoAB(RemessaDocumentoVO numeroRemessaTipoAB) {
      this.numeroRemessaTipoAB = numeroRemessaTipoAB;
    }

    /**
     * @return the icAlteracaoValida
     */
    public SituacaoAlteracaoRemessaEnum getIcAlteracaoValida() {
      return icAlteracaoValida;
    }

    /**
     * @param alteracaoTerceirizada the icAlteracaoValida to set
     */
    public void setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum alteracaoTerceirizada) {
      this.icAlteracaoValida = alteracaoTerceirizada;
    }

    /**
     * @return the numeroFolder
     */
    public BigDecimal getNumeroFolder() {
      return numeroFolder;
    }

    /**
     * @param numeroFolder the numeroFolder to set
     */
    public void setNumeroFolder(BigDecimal numeroFolder) {
      this.numeroFolder = numeroFolder;
    }


}
