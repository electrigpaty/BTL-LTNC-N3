package com.auction.common.model.user;

public class Bidder extends User {
    private static final long serialVersionUID = 1L;

    private double walletBalance;

    public Bidder() {
        super();
    }

    public Bidder(String id, String username, String password, String email, double walletBalance) {
        super(id, username, password, email);
        this.walletBalance = walletBalance;
    }

    @Override
    public String getRoleName() {
        return "BIDDER";
    }

    @Override
    public String printInfo() {
        return super.printInfo() + String.format(" | Số dư ví: %.2f", walletBalance);
    }

    public double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(double walletBalance) {
        if (walletBalance < 0) {
            throw new IllegalArgumentException("Số dư ví không được âm.");
        }
        this.walletBalance = walletBalance;
    }
}