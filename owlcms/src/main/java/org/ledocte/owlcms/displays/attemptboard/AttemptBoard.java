/***
 * Copyright (c) 2018-2019 Jean-François Lamy
 * 
 * This software is licensed under the the Apache 2.0 License amended with the
 * Commons Clause.
 * License text at https://github.com/jflamy/owlcms4/master/License
 * See https://redislabs.com/wp-content/uploads/2018/10/Commons-Clause-White-Paper.pdf
 */
package org.ledocte.owlcms.displays.attemptboard;

import java.util.Optional;

import org.ledocte.owlcms.data.athlete.Athlete;
import org.ledocte.owlcms.init.OwlcmsSession;
import org.ledocte.owlcms.state.UIEvent;
import org.ledocte.owlcms.ui.home.QueryParameterReader;
import org.ledocte.owlcms.ui.home.SafeEventBusRegistration;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * The Class AttemptBoard.
 */
@SuppressWarnings("serial")
@Tag("attempt-board-template")
@HtmlImport("frontend://components/AttemptBoard.html")
@Route("displays/attemptBoard")
@Theme(value = Material.class, variant = Material.DARK)
@Push
public class AttemptBoard extends PolymerTemplate<AttemptBoard.AttemptBoardModel> implements QueryParameterReader, SafeEventBusRegistration {
	
	final private static Logger logger = (Logger)LoggerFactory.getLogger(AttemptBoard.class);
	final private static Logger uiEventLogger = (Logger) LoggerFactory.getLogger("owlcms.uiEventLogger");
	static {
		logger.setLevel(Level.DEBUG);
		uiEventLogger.setLevel(Level.DEBUG);
	}
	
	/**
	 * AttemptBoardModel
	 * 
	 * Vaadin Flow propagates these variables to the corresponding Polymer template JavaScript properties.
	 * When the JS properties are changed, a "propname-changed" event is triggered.
	 * {@link Element.#addPropertyChangeListener(String, String, com.vaadin.flow.dom.PropertyChangeListener)}
	 *
	 */
	public interface AttemptBoardModel extends TemplateModel {
		String getLastName();
		String getFirstName();
		String getTeamName();
		Integer getStartNumber();
		String getAttempt();
		Integer getWeight();
		void setLastName(String lastName);
		void setFirstName(String firstName);
		void setTeamName(String teamName);
		void setStartNumber(Integer integer);
		void setAttempt(String formattedAttempt);
		void setWeight(Integer weight);
	}
	
	@Id("timer")
	private TimerElement timer; // created by Flow during template instanciation
	@Id("decisions")
	private DecisionElement decisions; // created by Flow during template instanciation
	private EventBus uiEventBus;

	/**
	 * Instantiates a new attempt board.
	 */
	public AttemptBoard() {
	}

	/* @see com.vaadin.flow.component.Component#onAttach(com.vaadin.flow.component.AttachEvent) */
	@Override
	protected void onAttach(AttachEvent attachEvent) {
		// fop obtained via QueryParameterReader interface default methods.
		OwlcmsSession.withFop(fop -> {
			init();
			// sync with current status of FOP
			doUpdate(fop.getCurAthlete());
			// we send on fopEventBus, listen on uiEventBus.
			uiEventBus = uiEventBusRegister(this, fop);
		});
	}

	private void init() {
		OwlcmsSession.withFop(fop -> {
			logger.debug("Starting attempt board on FOP {}", fop.getName());
			setId("attempt-board-template");
			this.getElement().setProperty("interactive", true);
		});
	}

//	/* @see com.vaadin.flow.component.Component#onDetach(com.vaadin.flow.component.DetachEvent) */
//	@Override
//	protected void onDetach(DetachEvent detachEvent) {
//		try {fopEventBus.unregister(this);} catch (Exception e) {}
//	}
	
	
	@Subscribe
	public void orderUpdated(UIEvent.LiftingOrderUpdated e) {
		Athlete a = e.getAthlete();
		doUpdate(a);
	}

	@Subscribe
	public void athleteAnnounced(UIEvent.AthleteAnnounced e) {
		Athlete a = e.getAthlete();
		doUpdate(a);
	}
	
	@Subscribe
	public void intermissionDone(UIEvent.IntermissionDone e) {
		Athlete a = e.getAthlete();
		doUpdate(a);
	}

	protected void doUpdate(Athlete a) {
		Optional<UI> ui2 = this.getUI();
		if (ui2.isPresent()) {
			uiEventLogger.debug("$$$ attemptBoard update");
			ui2.get().access(() -> {
				AttemptBoardModel model = getModel();	
				model.setLastName(a.getLastName());
				model.setFirstName(a.getFirstName());
				model.setTeamName(a.getTeam());
				model.setStartNumber(a.getStartNumber());
				String formattedAttempt = formatAttempt(a.getAttemptsDone());
				model.setAttempt(formattedAttempt);
				model.setWeight(a.getNextAttemptRequestedWeight());
			});
		} else {
			uiEventLogger.debug("$$$ attemptBoard detached, unregistering");
			uiEventBus.unregister(this);
		}
	}
	
	private String formatAttempt(Integer attemptsDone) {
		return ((attemptsDone%3 + 1) + " att.");
	}

	/**
	 * Reset.
	 */
	public void reset() {
		this.getElement().callFunction("reset");
	}
}
