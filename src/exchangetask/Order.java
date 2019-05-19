package exchangetask;

public class Order {

    public Order(long orderId, boolean isBuy, int price, int size) {
        this.orderId = orderId;
        this.isBuy = isBuy;
        this.price = price;
        this.size = size;
    }

    public long getOrderId() {
        return orderId;
    }

    public boolean isBuy() {
        return isBuy;
    }

    public int getPrice() {
        return price;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    private long orderId;
    private boolean isBuy;
    private int price;
    private int size;
}
