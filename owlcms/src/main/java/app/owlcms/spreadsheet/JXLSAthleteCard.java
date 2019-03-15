/***
 * Copyright (c) 2018-2019 Jean-François Lamy
 * 
 * This software is licensed under the the Apache 2.0 License amended with the
 * Commons Clause.
 * License text at https://github.com/jflamy/owlcms4/master/License
 * See https://redislabs.com/wp-content/uploads/2018/10/Commons-Clause-White-Paper.pdf
 */
package app.owlcms.spreadsheet;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.owlcms.data.athlete.Athlete;
import app.owlcms.data.athlete.AthleteRepository;
import app.owlcms.data.athleteSort.AthleteSorter;

@SuppressWarnings("serial")
public class JXLSAthleteCard extends JXLSWorkbookStreamSource {

    /**
     * Number of rows in a card
     */
    final static int CARD_SIZE = 10;
    /**
     * Number of cards per page
     */
    final static int CARDS_PER_PAGE = 2;

    /**
     *
     */
    public JXLSAthleteCard() {
        super();
    }

    public JXLSAthleteCard(boolean excludeNotWeighed) {
        super();
    }

    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory.getLogger(JXLSAthleteCard.class);

    @Override
    public InputStream getTemplate(Locale locale) throws IOException {
        String templateName = "/card/AthleteCardTemplate_" + locale.getLanguage() + ".xls";
        final InputStream resourceAsStream = this.getClass().getResourceAsStream(templateName);
        if (resourceAsStream == null) {
            throw new IOException("resource not found: " + templateName);} //$NON-NLS-1$
        return resourceAsStream;
    }

    @Override
    protected List<Athlete> getSortedAthletes() {
    	if (getGroup() != null) {
    		return AthleteSorter
    				.registrationOrderCopy(AthleteRepository.findAllByGroupAndWeighIn(getGroup(), false));
    	} else {
    		return AthleteSorter
    				.registrationOrderCopy(AthleteRepository.findAllByGroupAndWeighIn(null, false));
    	}
    }

    /*
     * (non-Javadoc)
     *
     * @see org.concordiainternational.competition.spreadsheet.JXLSWorkbookStreamSource#postProcess(org.apache.poi.ss.usermodel.Workbook)
     */
    @Override
    protected void postProcess(Workbook workbook) {
        setPageBreaks(workbook);
    }

    private void setPageBreaks(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        int lastRowNum = sheet.getLastRowNum();
        sheet.setAutobreaks(false);
        int increment = CARDS_PER_PAGE * CARD_SIZE + (CARDS_PER_PAGE - 1);

        for (int curRowNum = increment; curRowNum < lastRowNum;) {
            sheet.setRowBreak(curRowNum - 1);
            curRowNum += increment;
        }
    }

}
