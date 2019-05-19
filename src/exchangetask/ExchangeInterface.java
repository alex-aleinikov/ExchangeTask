package exchangetask;

public interface ExchangeInterface {
    public void send(long orderId, boolean isBuy, int price, int size) throws RequestRejectedException;
    public void cancel(long orderId) throws RequestRejectedException;
}
