package exchangetask;

import java.util.HashMap;
import java.util.Map;

public class TotalSizeAtPriceCache {

    void add(Order order) {
        Integer orderPrice = order.getPrice();
        totalSizeAtPriceCache.put(orderPrice, getSize(orderPrice) + order.getSize());
    }

    void remove(Order order) {
        update(order, 0);
    }

    void update(Order order, int newSize) {
        Integer orderPrice = order.getPrice();
        int newTotalSize = getSize(orderPrice) + newSize - order.getSize();
        if (newTotalSize > 0) {
            totalSizeAtPriceCache.put(orderPrice, newTotalSize);
        } else {
            totalSizeAtPriceCache.remove(orderPrice);
        }
    }

    int getSize(int price) {
        Integer size = totalSizeAtPriceCache.get(price);
        return size != null ? size : 0;
    }

    private Map<Integer, Integer> totalSizeAtPriceCache = new HashMap<>();
}
