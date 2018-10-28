package com.example.lastmanstanding;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Glenn on 06/03/2017.
 */

public class fixturesArrayAdapter extends ArrayAdapter<FixturesModel>{
    private Context context;
    private List<FixturesModel> fixturesProperties;

    //constructor, call on creation
    public fixturesArrayAdapter(Context context, int resource, ArrayList<FixturesModel> objects) {
        super(context, resource, objects);

        this.context = context;
        this.fixturesProperties = objects;
    }

    //called when rendering the list
    public View getView(int position, View convertView, ViewGroup parent) {

        //get the property we are displaying
        FixturesModel fixtures = fixturesProperties.get(position);

        //get the inflater and inflate the XML layout for each item
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fixtures_list_layout, null);

        ImageView homeTeamBadge = (ImageView) view.findViewById(R.id.homeTeamBadge);
        TextView homeTeamName = (TextView) view.findViewById(R.id.homeTeamName);
        TextView homeTeamGoals = (TextView) view.findViewById(R.id.homeTeamGoals);
        TextView vs = (TextView) view.findViewById(R.id.vs);
        TextView awayTeamGoals = (TextView) view.findViewById(R.id.awayTeamGoals);
        TextView awayTeamName = (TextView) view.findViewById(R.id.awayTeamName);
        ImageView awayTeamBadge = (ImageView) view.findViewById(R.id.awayTeamBadge);

        homeTeamName.setText(fixtures.homeTeamName);
        homeTeamGoals.setText(fixtures.homeTeamGoals);
        vs.setText("-");
        awayTeamGoals.setText(fixtures.awayTeamGoals);
        awayTeamName.setText(fixtures.awayTeamName);

        int homeBadge = context.getResources().getIdentifier(fixtures.getHomeTeamBadge(), "drawable", context.getPackageName());
        homeTeamBadge.setImageResource(homeBadge);
        int awayBadge = context.getResources().getIdentifier(fixtures.getAwayTeamBadge(), "drawable", context.getPackageName());
        awayTeamBadge.setImageResource(awayBadge);

        return view;
    }
}