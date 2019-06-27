package br.gov.caixa.gitecsa.util;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import br.gov.caixa.gitecsa.repository.DataRepository;
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
    public Logger produceLog(InjectionPoint injectionPoint) {
        String caminhoArquivo = System.getProperty(Constantes.EXTRANET_LOG4J);

        DOMConfigurator.configure(caminhoArquivo);

        return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }

    @Produces
    @RequestScoped
    public FacesContext produceFacesContext() {
        return FacesContext.getCurrentInstance();
    }

}
