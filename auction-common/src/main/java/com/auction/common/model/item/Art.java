package com.auction.common.model.item;

public class Art extends Item {
    private static final long serialVersionUID = 1L;

    private String artist;
    private int yearCreated;

    public Art() { super(); }

    public Art(String id, String name, String description, double startingPrice,
               String sellerId, String artist, int yearCreated) {
        super(id, name, description, startingPrice, sellerId);
        this.artist = artist;
        this.yearCreated = yearCreated;
    }

    @Override
    public String getCategoryType() { return "ART"; }

    @Override
    public String printInfo() {
        return super.printInfo() + String.format(" | Nghệ sĩ: %s | Năm sáng tác: %d", artist, yearCreated);
    }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public int getYearCreated() { return yearCreated; }
    public void setYearCreated(int yearCreated) { this.yearCreated = yearCreated; }
}