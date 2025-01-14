/*******************************************************************************
 * Copyright (c) 2009-2022 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("NPOSL-3.0")
 * License text at https://opensource.org/licenses/NPOSL-3.0
 *******************************************************************************/
package app.owlcms.init;

import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import app.owlcms.fieldofplay.FieldOfPlay;
import app.owlcms.i18n.Translator;
import app.owlcms.utils.LoggerUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Store the current user's settings and choices, across the multiple pages that may be opened.
 *
 * This class is either stored in a the Vaadin session shared between pages, or used as a singleton for testing.
 *
 * @author Jean-François Lamy
 */
public class OwlcmsSession {

    private static final String FOP = "fop";
    private final static Logger logger = (Logger) LoggerFactory.getLogger(OwlcmsSession.class);

    private static OwlcmsSession owlcmsSessionSingleton = null;

    static {
        logger.setLevel(Level.INFO);
    }

    /**
     * Gets the attribute.
     *
     * @param s the s
     * @return the attribute
     */
    public static Object getAttribute(String s) {
        return getCurrent().attributes.get(s);
    }

    public static OwlcmsSession getCurrent() {
        VaadinSession currentVaadinSession = VaadinSession.getCurrent();
        if (currentVaadinSession != null) {
            OwlcmsSession owlcmsSession = (OwlcmsSession) currentVaadinSession.getAttribute("owlcmsSession");
            if (owlcmsSession == null) {
                logger.trace("creating new OwlcmsSession {}", LoggerUtils.whereFrom());
                owlcmsSession = new OwlcmsSession();
                currentVaadinSession.setAttribute("owlcmsSession", owlcmsSession);
            }
            return owlcmsSession;
        } else {
            // Used for testing, return a singleton
            if (owlcmsSessionSingleton == null) {
                owlcmsSessionSingleton = new OwlcmsSession();
            }
            return owlcmsSessionSingleton;
        }
    }

    public static FieldOfPlay getFop() {
        return (FieldOfPlay) getAttribute(FOP);
    }

    public static String getFopLoggingName() {
        FieldOfPlay fop = getFop();
        if (fop == null) {
            fop = OwlcmsFactory.getDefaultFOP();
        }
        if (fop != null) {
            return fop.getLoggingName();
        } else {
            return "-";
        }
    }

    public static String getFopNameIfMultiple() {
        if (OwlcmsFactory.getFOPs().size() > 1) {
            FieldOfPlay fop;
            if ((fop = getFop()) != null) {
                return " " + fop.getName();
            } else {
                return null;
            }
        } else {
            return "";
        }
    }

    /**
     * Copied from Vaadin {@link Component} to ensure consistent behavior. {@link Translator} will enforce a language if
     * the competition screens must ignore browser settings
     *
     * @return
     */
    public static Locale getLocale() {
        Locale locale = Translator.getForcedLocale();
        logger.trace("forced locale = {}", locale);
        UI currentUi = UI.getCurrent();
        if (locale == null && currentUi != null) {
            locale = currentUi.getLocale();
        }

        // get first defined locale from translation file, else default
        if (locale == null) {
            List<Locale> locales = Translator.getAvailableLocales();
            if (locales != null && !locales.isEmpty()) {
                locale = locales.get(0);
            } else {
                // defensive, can't happen
                locale = Locale.ENGLISH;
            }
        }

        if (locale.getCountry() == "") {
            // add the country from Locale.getDefault -- probably the country we're running in.
            // this may result in strange things for cloud -- such as es_US but the locale logic will not
            // find es_US and will fall back to using es
            // this will however work for en_US and en_UK and en_CA when running on a laptop, for date formats.
            String country = Locale.getDefault().getCountry();
            String variant = locale.getVariant();
            String language = locale.getLanguage();
            locale = new Locale(language, country, variant);
        }
        if (currentUi != null) {
            currentUi.setLocale(locale);
        }
        return locale;
    }

    public static String getRequestedUrl() {
        return (String) getAttribute("requestedURL");
    }

    public static boolean isAuthenticated() {
        return Boolean.TRUE.equals(getAttribute("authenticated"));
    }

    /**
     * Sets the attribute.
     *
     * @param s the s
     * @param o the o
     */
    public static void setAttribute(String s, Object o) {
        if (o == null) {
            getCurrent().attributes.remove(s);
        } else {
            getCurrent().attributes.put(s, o);
        }
    }

    public static void setAuthenticated(boolean isAuthenticated) {
        setAttribute("authenticated", isAuthenticated);
    }

    public static void setFop(FieldOfPlay fop) {
        logger.trace("setFop {} from {}", (fop != null ? fop.getName() : null), LoggerUtils.whereFrom());
        setAttribute(FOP, fop);
    }

    public static void setRequestedUrl(String url) {
        setAttribute("requestedURL", url);
    }

    public static void withFop(Consumer<FieldOfPlay> command) {
        FieldOfPlay fop = getFop();
        if (fop == null) {
            fop = OwlcmsFactory.getDefaultFOP();
        }
        if (fop != null) {
            command.accept(fop);
        }
    }

    private Properties attributes = new Properties();

    private OwlcmsSession() {
    }
}
