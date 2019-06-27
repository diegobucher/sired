package br.gov.caixa.gitecsa.sired.vo;

import java.text.SimpleDateFormat;
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

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoLotericoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc21_remessa_tipo_c", schema = Constantes.SCHEMADB_NAME)
public class RemessaMovimentoDiarioVO extends BaseEntity {

	private static final long serialVersionUID = 106055423061389564L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "nu_remessa_tipo_c", columnDefinition = "serial")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nu_remessa_c17", columnDefinition = "int4")
	private RemessaVO remessa;

	@Column(name = "nu_item", columnDefinition = "int2")
	private Integer nuItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nu_unidade_geradora_a02")
	private UnidadeVO unidadeGeradora;

	@Column(name = "co_usuario_ultima_alteracao", columnDefinition = "bpchar")
	private String codigoUsuarioUltimaAlteracao;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dh_ultima_alteracao")
	private Date dataHoraUltimaAlteracao;

	@Temporal(TemporalType.DATE)
	@Column(name = "dt_movimento")
	private Date dataMovimento;

	@Column(name = "nu_terminal", columnDefinition = "numeric")
	private Long nuTerminal;

	@Column(name = "ic_loterico", columnDefinition = "int2")
	private TipoLotericoEnum icLoterico;

	@Column(name = "qt_grupo_1", columnDefinition = "int4")
	private Integer grupo1;

	@Column(name = "qt_grupo_2", columnDefinition = "int4")
	private Integer grupo2;

	@Column(name = "qt_grupo_3", columnDefinition = "int4")
	private Integer grupo3;

	@Column(name = "de_localizacao", columnDefinition = "bpchar")
	private String localizacao;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nu_remessa_tipo_c_c21", columnDefinition = "int4")
	private RemessaMovimentoDiarioVO numeroRemessaTipoC;

	@Column(name = "ic_alteracao_valida", nullable = false, columnDefinition = "int2")
	private SituacaoAlteracaoRemessaEnum icAlteracaoValida;

	@Transient
	private boolean flagValorDiferenteIcLoterico;

	@Transient
	private boolean flagValorDiferenteNuTerminal;

	@Transient
	private boolean flagValorDiferenteIcGrupo1;

	@Transient
	private boolean flagValorDiferenteIcGrupo2;

	@Transient
	private boolean flagValorDiferenteIcGrupo3;

	@Transient
	private String conteudo;

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

	public Integer getNuItem() {
		return nuItem;
	}

	public void setNuItem(Integer nuItem) {
		this.nuItem = nuItem;
	}

	public UnidadeVO getUnidadeGeradora() {
		return unidadeGeradora;
	}

	public void setUnidadeGeradora(UnidadeVO unidadeGeradora) {
		this.unidadeGeradora = unidadeGeradora;
	}

	public String getCodigoUsuarioUltimaAlteracao() {
		return codigoUsuarioUltimaAlteracao;
	}

	public void setCodigoUsuarioUltimaAlteracao(String codigoUsuarioUltimaAlteracao) {
		this.codigoUsuarioUltimaAlteracao = codigoUsuarioUltimaAlteracao;
	}

	public Date getDataHoraUltimaAlteracao() {
		return dataHoraUltimaAlteracao;
	}

	public void setDataHoraUltimaAlteracao(Date dataHoraUltimaAlteracao) {
		this.dataHoraUltimaAlteracao = dataHoraUltimaAlteracao;
	}

	public Date getDataMovimento() {
		return dataMovimento;
	}

	public void setDataMovimento(Date dataMovimento) {
		this.dataMovimento = dataMovimento;
	}

	public Long getNuTerminal() {
		return nuTerminal;
	}

	public void setNuTerminal(Long nuTerminal) {
		this.nuTerminal = nuTerminal;
	}

	public TipoLotericoEnum getIcLoterico() {
		return icLoterico;
	}

	public void setIcLoterico(TipoLotericoEnum icLoterico) {
		this.icLoterico = icLoterico;
	}

	public Integer getGrupo1() {
		return grupo1;
	}

	public void setGrupo1(Integer grupo1) {
		this.grupo1 = grupo1;
	}

	public Integer getGrupo2() {
		return grupo2;
	}

	public void setGrupo2(Integer grupo2) {
		this.grupo2 = grupo2;
	}

	public Integer getGrupo3() {
		return grupo3;
	}

	public void setGrupo3(Integer grupo3) {
		this.grupo3 = grupo3;
	}

	public String getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(String localizacao) {
		this.localizacao = localizacao;
	}

	public Long getIdItem() {
		if (this.id != null) {
			return this.id;
		} else {
			return this.nuItem.longValue();
		}
	}

	public String getDataMovimentoFormatada() {
		if (this.dataMovimento != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			return sdf.format(this.dataMovimento);
		}
		return null;
	}

	@Transient
	public String getDataFormatada() {
		this.dataMovimento = getDataMovimento();
		String data = "";
		if (this.dataMovimento != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			data = sdf.format(this.dataMovimento);
		}
		return data;
	}

	@Transient
	public String getNomeUnidadeFormatada() {
		return this.getUnidadeGeradora().getDescricaoCompleta();
	}

	@Transient
	public String getCodigoRemessaTipoC() {
		return this.getRemessa().getCodigoRemessaTipoC().toString();
	}

	/**
	 * @return the numeroRemessaTipoAB
	 */
	public RemessaMovimentoDiarioVO getNumeroRemessaTipoC() {
		return numeroRemessaTipoC;
	}

	/**
	 * @param numeroRemessaTipoAB
	 *            the numeroRemessaTipoAB to set
	 */
	public void setNumeroRemessaTipoC(RemessaMovimentoDiarioVO numeroRemessaTipoAB) {
		this.numeroRemessaTipoC = numeroRemessaTipoAB;
	}

	/**
	 * @return the icAlteracaoValida
	 */
	public SituacaoAlteracaoRemessaEnum getIcAlteracaoValida() {
		return icAlteracaoValida;
	}

	/**
	 * @param icAlteracaoValida
	 *            the icAlteracaoValida to set
	 */
	public void setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum icAlteracaoValida) {
		this.icAlteracaoValida = icAlteracaoValida;
	}

	/**
	 * @return the flagValorDiferenteIcLoterico
	 */
	public boolean isFlagValorDiferenteIcLoterico() {
		return flagValorDiferenteIcLoterico;
	}

	/**
	 * @param flagValorDiferenteIcLoterico
	 *            the flagValorDiferenteIcLoterico to set
	 */
	public void setFlagValorDiferenteIcLoterico(boolean flagValorDiferenteIcLoterico) {
		this.flagValorDiferenteIcLoterico = flagValorDiferenteIcLoterico;
	}

	/**
	 * @return the flagValorDiferenteNuTerminal
	 */
	public boolean isFlagValorDiferenteNuTerminal() {
		return flagValorDiferenteNuTerminal;
	}

	/**
	 * @param flagValorDiferenteNuTerminal
	 *            the flagValorDiferenteNuTerminal to set
	 */
	public void setFlagValorDiferenteNuTerminal(boolean flagValorDiferenteNuTerminal) {
		this.flagValorDiferenteNuTerminal = flagValorDiferenteNuTerminal;
	}

	/**
	 * @return the flagValorDiferenteIcGrupo1
	 */
	public boolean isFlagValorDiferenteIcGrupo1() {
		return flagValorDiferenteIcGrupo1;
	}

	/**
	 * @param flagValorDiferenteIcGrupo1
	 *            the flagValorDiferenteIcGrupo1 to set
	 */
	public void setFlagValorDiferenteIcGrupo1(boolean flagValorDiferenteIcGrupo1) {
		this.flagValorDiferenteIcGrupo1 = flagValorDiferenteIcGrupo1;
	}

	/**
	 * @return the flagValorDiferenteIcGrupo2
	 */
	public boolean isFlagValorDiferenteIcGrupo2() {
		return flagValorDiferenteIcGrupo2;
	}

	/**
	 * @param flagValorDiferenteIcGrupo2
	 *            the flagValorDiferenteIcGrupo2 to set
	 */
	public void setFlagValorDiferenteIcGrupo2(boolean flagValorDiferenteIcGrupo2) {
		this.flagValorDiferenteIcGrupo2 = flagValorDiferenteIcGrupo2;
	}

	/**
	 * @return the flagValorDiferenteIcGrupo3
	 */
	public boolean isFlagValorDiferenteIcGrupo3() {
		return flagValorDiferenteIcGrupo3;
	}

	/**
	 * @param flagValorDiferenteIcGrupo3
	 *            the flagValorDiferenteIcGrupo3 to set
	 */
	public void setFlagValorDiferenteIcGrupo3(boolean flagValorDiferenteIcGrupo3) {
		this.flagValorDiferenteIcGrupo3 = flagValorDiferenteIcGrupo3;
	}

	/**
	 * @return the conteudo
	 */
	public String getConteudo() {
		return conteudo;
	}

	/**
	 * @param conteudo
	 *            the conteudo to set
	 */
	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

}
