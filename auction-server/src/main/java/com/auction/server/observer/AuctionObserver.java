package com.auction.server.observer;

import com.auction.common.dto.AuctionStatusDTO;

public interface AuctionObserver {
    void onUpdate(AuctionStatusDTO status);
}
