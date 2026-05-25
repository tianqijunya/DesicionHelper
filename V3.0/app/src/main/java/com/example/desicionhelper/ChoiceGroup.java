package com.example.desicionhelper;

import java.util.List;

public class ChoiceGroup {

    private String name;
    private List<String> choices;
    private int pickCount;

    public ChoiceGroup(String name, List<String> choices, int pickCount) {
        this.name = name;
        this.choices = choices;
        this.pickCount = pickCount;
    }

    public String getName() {
        return name;
    }

    public List<String> getChoices() {
        return choices;
    }

    public int getPickCount() {
        return pickCount;
    }

    public int getTotalCount() {
        return choices.size();
    }
}
