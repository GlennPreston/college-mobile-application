package com.example.lastmanstanding;

/**
 * Created by Glenn on 18/04/2017.
 */

public class FixturesModel {
    String homeTeamBadge;
    String homeTeamName;
    String homeTeamGoals;
    String awayTeamGoals;
    String awayTeamName;
    String awayTeamBadge;

    public FixturesModel (String homeTeamBadge, String homeTeamName, String homeTeamGoals, String awayTeamGoals, String awayTeamName, String awayTeamBadge) {
        this.homeTeamBadge = homeTeamBadge;
        this.homeTeamName = homeTeamName;
        this.homeTeamGoals = homeTeamGoals;
        this.awayTeamGoals = awayTeamGoals;
        this.awayTeamName = awayTeamName;
        this.awayTeamBadge = awayTeamBadge;
    }

    public String getHomeTeamBadge() {
        return homeTeamBadge;
    }

    public String getHomeTeamName() {
        return homeTeamName;
    }

    public String getHomeTeamGoals() {
        return homeTeamGoals;
    }

    public String getAwayTeamGoals() {
        return awayTeamGoals;
    }

    public String getAwayTeamName() {
        return awayTeamName;
    }

    public String getAwayTeamBadge() {
        return awayTeamBadge;
    }
}