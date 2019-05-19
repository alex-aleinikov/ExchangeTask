package exchangetask;

public interface QueryInterface {
    // Return sum of sizes of resting orders at <price> or zero
    public int getTotalSizeAtPrice(int price) throws RequestRejectedException;
    // Return the highest price with at least one resting Buy order
    public int getHighestBuyPrice() throws RequestRejectedException;
    // Return the lowest price with at least one resting Sell order
    public int getLowestSellPrice() throws RequestRejectedException;
}