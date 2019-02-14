package org.ledocte.owlcms.ui.preparation;

import org.ledocte.owlcms.ui.home.MainNavigationLayout;

import com.github.appreciated.app.layout.behaviour.AppLayout;
import com.vaadin.flow.component.html.Label;

@SuppressWarnings("serial")
public class PreparationNavigationLayout extends MainNavigationLayout {

	/* (non-Javadoc)
	 * @see org.ledocte.owlcms.ui.home.MainNavigationLayout#createAppLayoutInstance()
	 */
	@Override
	public AppLayout createAppLayoutInstance() {
		AppLayout appLayout = super.createAppLayoutInstance();
		appLayout.setTitleComponent(new Label("Prepare Competition"));
		return appLayout;
		
	}
	
	

}
