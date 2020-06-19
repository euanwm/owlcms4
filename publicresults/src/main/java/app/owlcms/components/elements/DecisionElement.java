/***
 * Copyright (c) 2009-2020 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("Non-Profit OSL" 3.0)
 * License text at https://github.com/jflamy/owlcms4/blob/master/LICENSE.txt
 */
package app.owlcms.components.elements;

import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

import app.owlcms.publicresults.BreakTimerEvent;
import app.owlcms.publicresults.DecisionEvent;
import app.owlcms.publicresults.DecisionReceiverServlet;
import app.owlcms.publicresults.TimerEvent;
import app.owlcms.publicresults.TimerReceiverServlet;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * ExplicitDecision display element.
 */
@Tag("decision-element")
@JsModule("./components/DecisionElement.js")
public class DecisionElement extends PolymerTemplate<DecisionElement.DecisionModel> {

    /**
     * The Interface DecisionModel.
     */
    public interface DecisionModel extends TemplateModel {

        boolean isEnabled();

        boolean isJury();

        boolean isPublicFacing();

        void setEnabled(boolean b);

        void setJury(boolean juryMode);

        void setPublicFacing(boolean publicFacing);
    }

    final private static Logger logger = (Logger) LoggerFactory.getLogger(DecisionElement.class);
    final private static Logger uiEventLogger = (Logger) LoggerFactory.getLogger("UI" + logger.getName());

    static {
        logger.setLevel(Level.INFO);
        uiEventLogger.setLevel(Level.INFO);
    }

    protected EventBus uiEventBus;
    protected EventBus fopEventBus;
    private UI ui;
    public DecisionElement() {
    }

    public boolean isPublicFacing() {
        return Boolean.TRUE.equals(getModel().isPublicFacing());
    }

    public void setJury(boolean juryMode) {
        getModel().setJury(false);
    }

    public void setPublicFacing(boolean publicFacing) {
        getModel().setPublicFacing(publicFacing);
    }

    @Subscribe
    public void slaveDecision(DecisionEvent de) {
        if (ui == null) return;
        ui.access(() -> {
            if (de.isBreak()) {
                logger.debug("slaveBreakStart disable");
                getModel().setEnabled(false);
            } else {
                switch (de.getEventType()) {
                case DOWN_SIGNAL:
                    this.getElement().callJsFunction("showDown", false, false);
                    break;
                case FULL_DECISION:
                    this.getElement().callJsFunction("showDecisions", false, de.getDecisionLight1(), de.getDecisionLight2(),
                            de.getDecisionLight3());
                    getModel().setEnabled(false);
                case RESET:
                    getElement().callJsFunction("reset", false);
                    logger.debug("slaveReset disable");
                    break;
                default:
                    logger.error("unknown decision event type {}", de.getEventType());
                    break;
                }
            }
        });
    }

    @Subscribe
    public void slaveStartTimer(TimerEvent.StartTime e) {
        if (ui == null) return;
        ui.access(() -> {
            getModel().setEnabled(true);
        });
    }

    @Subscribe
    public void slaveStopTimer(TimerEvent.StopTime e) {
        if (ui == null) return;
        ui.access(() -> {
            getModel().setEnabled(true);
        });
    }

    @Subscribe
    public void slaveStartBreakTimer(BreakTimerEvent.StartTime e) {
        if (ui == null) return;
        ui.access(() -> {
            getModel().setEnabled(true);
        });
    }

    @Subscribe
    public void slaveStopBreakTimer(BreakTimerEvent.StopTime e) {
        if (ui == null) return;
        ui.access(() -> {
            getModel().setEnabled(true);
        });
    } 
    protected Object getOrigin() {
        // we use the identity of our parent AttemptBoard or AthleteFacingAttemptBoard
        // to identify
        // our actions.
        return this.getParent().get();
    }

    /*
     * @see com.vaadin.flow.component.Component#onAttach(com.vaadin.flow.component. AttachEvent)
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        this.ui = attachEvent.getUI();
        init();

        DecisionReceiverServlet.getEventBus().register(this);
        TimerReceiverServlet.getEventBus().register(this);
    }


    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        this.ui = null;
        DecisionReceiverServlet.getEventBus().unregister(this);
        TimerReceiverServlet.getEventBus().unregister(this);
    }

    private void init() {
        DecisionModel model = getModel();
        model.setPublicFacing(true);
    }
}
