package com.votingsystem.model;

public class Candidate {
    private int id;
    private int electionId;
    private String name;
    private String party;

    public Candidate(int id, int electionId, String name, String party) {
        this.id = id;
        this.electionId = electionId;
        this.name = name;
        this.party = party;
    }

    public int getId() { return id; }
    public int getElectionId() { return electionId; }
    public String getName() { return name; }
    public String getParty() { return party; }

    @Override
    public String toString() { return name + " (" + party + ")"; }
}