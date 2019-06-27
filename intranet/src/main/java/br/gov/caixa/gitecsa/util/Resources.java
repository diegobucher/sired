package br.gov.caixa.gitecsa.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import br.gov.caixa.gitecsa.repository.ConfigurationRepository;
import br.gov.caixa.gitecsa.repository.DataRepository;
import br.gov.caixa.gitecsa.repository.DataSiicoRepository;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.util.Constantes;

/**
 * Classe responsavel pela producao de recursos uteis para utilizacao na aplicacao
 */
public class Resources {

	@Produces
	@DataRepository
	@PersistenceContext(unitName = "siredPU")
	private EntityManager em;

	@Produces
	@DataSiicoRepository
	@PersistenceContext(unitName = "siicoPU")
	private EntityManager emSiico;

	@Produces
	public Logger produceLog(InjectionPoint injectionPoint) {
		// String caminhoArquivo = System.getProperty(Constantes.INTRANET_LOG4J);

		DOMConfigurator.configure("C:\\caixa\\SIRED - 4.4.0.01-bnu\\intranet\\src\\main\\resources\\sired_intra.xml");

		return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
	}

	@Produces
	@RequestScoped
	public FacesContext produceFacesContext() {
		return FacesContext.getCurrentInstance();
	}

	/*
	 * @Produces public Session produceSession() { return em.unwrap(Session.class); }
	 */

	@Produces
	@ConfigurationRepository("")
	public Properties produceProperties(InjectionPoint ip) {

		Properties p = new Properties();

		String value = ip.getAnnotated().getAnnotation(ConfigurationRepository.class).value();

		if (value == null || value.equals("")) {
			return p;
		}
		initProperties(p, value);
		return p;
	}

	private void initProperties(Properties p, String value) {

		String propValue = System.getProperty(value);

		if (propValue != null) {
			value = propValue;
		}

		File f = new File(value);

		if (f.exists() && !f.isDirectory()) {
			try {
				FileInputStream fis = new FileInputStream(f);
				p.load(fis);
			} catch (IOException e) {
				new AppException(e.getMessage(), e);
			}
		}

		try {

			InputStream is = this.getClass().getClassLoader().getResourceAsStream(value);
			p.load(is);

		} catch (Exception e) {
			new AppException(e.getMessage(), e);
		}

	}
}
