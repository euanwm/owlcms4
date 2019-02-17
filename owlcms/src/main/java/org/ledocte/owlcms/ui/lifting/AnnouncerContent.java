/***
 * Copyright (c) 2018-2019 Jean-François Lamy
 * 
 * This software is licensed under the the Apache 2.0 License amended with the
 * Commons Clause.
 * License text at https://github.com/jflamy/owlcms4/master/License
 * See https://redislabs.com/wp-content/uploads/2018/10/Commons-Clause-White-Paper.pdf
 */

package org.ledocte.owlcms.ui.lifting;

import java.util.Collection;

import org.ledocte.owlcms.data.athlete.Athlete;
import org.ledocte.owlcms.data.athlete.AthleteRepository;
import org.ledocte.owlcms.ui.crudui.OwlcmsCrudFormFactory;
import org.ledocte.owlcms.ui.crudui.OwlcmsCrudLayout;
import org.ledocte.owlcms.ui.crudui.OwlcmsGridCrud;
import org.slf4j.LoggerFactory;
import org.vaadin.crudui.crud.CrudListener;
import org.vaadin.crudui.crud.impl.GridCrud;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.Route;

import ch.qos.logback.classic.Logger;

/**
 * The Class AnnouncerContent.
 */
@SuppressWarnings("serial")
@Route(value = "group/announcer", layout = AnnouncerLayout.class)
public class AnnouncerContent extends VerticalLayout implements CrudListener<Athlete> { // or implements LazyCrudListener<Athlete>

	@SuppressWarnings("unused")
	final private static Logger logger = (Logger)LoggerFactory.getLogger(AnnouncerContent.class);
    
    /**
     * Instantiates a new announcer content.
     */
    public AnnouncerContent() {
        setSizeFull();
        GridCrud<Athlete> crud = getGridCrud();
		add(crud);

    }

    /**
     * Gets the grid crud.
     *
     * @return the grid crud
     */
    public GridCrud<Athlete> getGridCrud() {
        OwlcmsCrudFormFactory<Athlete> crudFormFactory = new OwlcmsCrudFormFactory<Athlete>(Athlete.class);
        crudFormFactory.setVisibleProperties("lastName", "firstName", "team", "category", "nextAttemptRequestedWeight", "attemptsDone");
        crudFormFactory.setFieldCaptions("Last Name", "First Name", "Team", "Category", "Requested Weight", "Attempts Done");
        crudFormFactory.setDisabledProperties("nextAttemptRequestedWeight");
        
        Grid<Athlete> grid = new Grid<Athlete>(Athlete.class, false);
        ThemeList themes = grid.getThemeNames();
        themes.add("compact");
        themes.add("row-stripes");
		grid.setColumns("lastName", "firstName", "team", "category", "nextAttemptRequestedWeight", "attemptsDone");
		grid.getColumnByKey("lastName").setHeader("Last Name");
		grid.getColumnByKey("firstName").setHeader("First Name");
		grid.getColumnByKey("team").setHeader("Team");
		grid.getColumnByKey("category").setHeader("Category");
		grid.getColumnByKey("nextAttemptRequestedWeight").setHeader("Requested Weight");
		grid.getColumnByKey("attemptsDone").setHeader("Attempts Done");
        
        GridCrud<Athlete> crud = new OwlcmsGridCrud<Athlete>(Athlete.class, new OwlcmsCrudLayout(Athlete.class), crudFormFactory, grid) {
			@Override
			protected void initToolbar() {}
			@Override
			protected void updateButtons() {}
        };
        crud.setCrudListener(this);
        crud.setClickRowToUpdate(true);
        crud.getCrudLayout().addToolbarComponent(new Label("toolbar stuff goes here"));
        
        return crud;
    }


	/* (non-Javadoc)
	 * @see org.vaadin.crudui.crud.CrudListener#add(java.lang.Object)
	 */
	@Override
    public Athlete add(Athlete Athlete) {
        AthleteRepository.save(Athlete);
        return Athlete;
    }

    /* (non-Javadoc)
     * @see org.vaadin.crudui.crud.CrudListener#update(java.lang.Object)
     */
    @Override
    public Athlete update(Athlete Athlete) {
        if (Athlete.getId().equals(5l)) {
            throw new RuntimeException("A simulated error has occurred");
        }
        return AthleteRepository.save(Athlete);
    }

    /* (non-Javadoc)
     * @see org.vaadin.crudui.crud.CrudListener#delete(java.lang.Object)
     */
    @Override
    public void delete(Athlete Athlete) {
        AthleteRepository.delete(Athlete);
    }

    /* (non-Javadoc)
     * @see org.vaadin.crudui.crud.CrudListener#findAll()
     */
    @Override
    public Collection<Athlete> findAll() {
        return AthleteRepository.findAll();
    }


}
