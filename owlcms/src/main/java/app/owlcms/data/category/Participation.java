/*******************************************************************************
 * Copyright (c) 2009-2021 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("NPOSL-3.0")
 * License text at https://opensource.org/licenses/NPOSL-3.0
 *******************************************************************************/
package app.owlcms.data.category;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.slf4j.LoggerFactory;

import app.owlcms.data.athlete.Athlete;
import app.owlcms.data.athleteSort.AthleteSorter;
import ch.qos.logback.classic.Logger;

/**
 * Association class between Athlete and Category. Holds rankings and points of athlete and category.
 *
 * An athlete participates in one or more category (when eligible according to age, gender and qualifying total). A
 * category contains zero or more athletes.
 *
 * @author Jean-François Lamy
 */
@Entity(name = "Participation")
@Table(name = "participation")
public class Participation implements IRankHolder {

    final static Logger logger = (Logger) LoggerFactory.getLogger(Participation.class);

    @EmbeddedId
    private ParticipationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("athleteId")
    private Athlete athlete;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    private Category category;

    @Column(columnDefinition = "integer default 0")
    private int cleanJerkRank;

    @Column(columnDefinition = "integer default 0")
    private int customRank;

    @Column(columnDefinition = "integer default 0")
    private int snatchRank;

    @Column(columnDefinition = "integer default 0")
    private int totalRank;

    @Column(columnDefinition = "integer default 0")
    private int combinedRank;

    /**
     * Athlete is member of team for the age group. Points will be scored according to ranks. An athlete can be
     * qualified for JR and SR, but only on the JR team for example.
     */
    @Column(columnDefinition = "boolean default true")
    private boolean teamMember;

    @Column(columnDefinition = "integer default 0")
    private int teamTotalRank;

    @Column(columnDefinition = "integer default 0")
    private int teamCJRank;

    @Column(columnDefinition = "integer default 0")
    private int teamCombinedRank;

    @Column(columnDefinition = "integer default 0")
    private int teamRobiRank;

    @Column(columnDefinition = "integer default 0")
    private int teamSinclairRank;

    @Column(columnDefinition = "integer default 0")
    private int teamSnatchRank;

    public Participation(Athlete athlete, Category category) {
        this();
        this.athlete = athlete;
        this.category = category;
        this.id = new ParticipationId(athlete.getId(), category.getId());
    }

    public Participation(Participation p, Athlete a, Category c) {
        this.athlete = a;
        this.category = c;
        this.cleanJerkRank = p.cleanJerkRank;
        this.customRank = p.customRank;
        this.snatchRank = p.snatchRank;
        this.totalRank = p.totalRank;
        this.combinedRank = p.combinedRank;
    }

    protected Participation() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Participation that = (Participation) o;
        return Objects.equals(athlete, that.athlete) &&
                Objects.equals(category, that.category);
    }

    public Athlete getAthlete() {
        return athlete;
    }

    public Category getCategory() {
        return category;
    }

    public int getCleanJerkPoints() {
        return AthleteSorter.pointsFormula(cleanJerkRank);
    }

    public int getCleanJerkRank() {
        return cleanJerkRank;
    }

    public Integer getCombinedPoints() {
        return getSnatchPoints() + getCleanJerkPoints() + getTotalPoints();
    }

    public int getCombinedRank() {
        return combinedRank;
    }

    public int getCustomPoints() {
        return AthleteSorter.pointsFormula(customRank);
    }

    public int getCustomRank() {
        return customRank;
    }

    public ParticipationId getId() {
        return id;
    }

    public int getSnatchPoints() {
        return AthleteSorter.pointsFormula(snatchRank);
    }

    public int getSnatchRank() {
        return snatchRank;
    }

    public int getTeamCJRank() {
        return teamCJRank;
    }

    public int getTeamCombinedRank() {
        return teamCombinedRank;
    }

    public boolean getTeamMember() {
        return teamMember;
    }

    public int getTeamRobiRank() {
        return teamRobiRank;
    }

    public int getTeamSinclairRank() {
        return teamSinclairRank;
    }

    public int getTeamSnatchRank() {
        return teamSnatchRank;
    }

    public int getTeamTotalRank() {
        return teamTotalRank;
    }

    public int getTotalPoints() {
        return AthleteSorter.pointsFormula(totalRank);
    }

    public int getTotalRank() {
        return totalRank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(athlete, category);
    }

    public String long_dump() {
        return "Participation " + System.identityHashCode(this)
                + " [id=" + id
                + ", athlete=" + athlete + "(" + System.identityHashCode(athlete) + ")"
                + ", category=" + category + "(" + System.identityHashCode(category) + ")"
                + ", snatchRank=" + getSnatchRank()
                + ", cleanJerkRank=" + getCleanJerkRank()
                + ", totalRank=" + getTotalRank()
                + ", teamMember=" + getTeamMember() + "]";
    }

    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setCleanJerkRank(int cleanJerkRank) {
        this.cleanJerkRank = cleanJerkRank;
        logger.trace("cleanJerkRank {}", long_dump());
    }

    public void setCombinedRank(int combinedRank) {
        this.combinedRank = combinedRank;
        logger.trace("combinedRank {}", long_dump());
    }

    public void setCustomRank(int customRank) {
        this.customRank = customRank;
    }

    public void setSnatchRank(int snatchRank) {
        this.snatchRank = snatchRank;
        logger.trace("snatchRank {}", long_dump());
    }

    public void setTeamCJRank(int teamCJRank) {
        this.teamCJRank = teamCJRank;
    }

    public void setTeamCleanJerkRank(int teamCJRank) {
        this.teamCJRank = teamCJRank;
    }

    public void setTeamCombinedRank(int teamCombinedRank) {
        this.teamCombinedRank = teamCombinedRank;

    }

    public void setTeamMember(boolean teamMember) {
        this.teamMember = teamMember;
    }

    public void setTeamRobiRank(int teamRobiRank) {
        this.teamRobiRank = teamRobiRank;

    }

    public void setTeamSinclairRank(int teamSinclairRank) {
        this.teamSinclairRank = teamSinclairRank;
    }

    public void setTeamSnatchRank(int teamSnatchRank) {
        this.teamSnatchRank = teamSnatchRank;
    }

    public void setTeamTotalRank(int teamTotalRank) {
        this.teamTotalRank = teamTotalRank;
    }

    public void setTotalRank(int totalRank) {
        this.totalRank = totalRank;
        logger.trace("totalRank {}", long_dump());
    }

    @Override
    public String toString() {
        return "Participation [athlete=" + athlete + ", category=" + category + "]";
    }
}