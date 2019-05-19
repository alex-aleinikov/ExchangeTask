package exchangetask;

import java.util.*;

public class Exchange implements ExchangeInterface, QueryInterface {

    @Override
    public void send(long orderId, boolean isBuy, int price, int size) throws RequestRejectedException {
        if (price < 0 || size < 0) {
            throw new RequestRejectedException();
        }
        Map<Integer, Set<Order>> consumedRestingOrders;
        Map<Integer, Set<Order>> producedRestingOrders;
        if (isBuy) {
            consumedRestingOrders = sellRestingOrders;
            producedRestingOrders = buyRestingOrders;
        } else {
            consumedRestingOrders = buyRestingOrders;
            producedRestingOrders = sellRestingOrders;
        }
        Integer consumePrice = null;
        boolean isCachedPriceChanged = false;
        for (Iterator<Map.Entry<Integer, Set<Order>>> entrySetIt = consumedRestingOrders.entrySet().iterator();
             entrySetIt.hasNext(); ) {
            Map.Entry<Integer, Set<Order>> entry = entrySetIt.next();
            consumePrice = entry.getKey();
            Set<Order> consumeOrders = entry.getValue();
            if (isBuy ? price < consumePrice : price > consumePrice || size == 0) {
                break;
            }
            size = consumeOrders(consumeOrders, size);
            if (consumeOrders.isEmpty()) {
                entrySetIt.remove();
                isCachedPriceChanged = true;
            } else if (size == 0) {
                break;
            }
        }
        if (size > 0) {
            addToRestingOrders(producedRestingOrders, new Order(orderId, isBuy, price, size));
            if (isBuy && (highestBuyPrice == null || price > highestBuyPrice)
                    || !isBuy && (lowestSellPrice == null || price < lowestSellPrice)) {
                setStartPriceCache(isBuy, price);
            }
        }
        if (isCachedPriceChanged) {
            setStartPriceCache(!isBuy, !consumedRestingOrders.isEmpty() ? consumePrice : null);
        }
    }

    private int consumeOrders(Set<Order> consumeOrders, int size) {
        int resultSize = size;
        for (Iterator<Order> consumedOrderIt = consumeOrders.iterator();
             consumedOrderIt.hasNext(); ) {
            Order consumedOrder = consumedOrderIt.next();
            int consumedOrderSize = consumedOrder.getSize();
            if (resultSize - consumedOrderSize >= 0) {
                resultSize -= consumedOrderSize;
                totalSizeAtPriceCache.remove(consumedOrder);
                orders.remove(consumedOrder.getOrderId());
                consumedOrderIt.remove();
            } else {
                totalSizeAtPriceCache.update(consumedOrder, consumedOrderSize - resultSize);
                consumedOrder.setSize(consumedOrderSize - resultSize);
                resultSize = 0;
                break;
            }
        }
        return resultSize;
    }

    private void addToRestingOrders(Map<Integer, Set<Order>> restingOrders, Order order) {
        Integer price = order.getPrice();
        Set<Order> producedOrders = restingOrders.get(price);
        if (producedOrders != null) {
            producedOrders.add(order);
        } else {
            restingOrders.put(price, new LinkedHashSet<>() {{
                add(order);
            }});
        }
        orders.put(order.getOrderId(), order);
        totalSizeAtPriceCache.add(order);
    }

    @Override
    public void cancel(long orderId) throws RequestRejectedException {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new RequestRejectedException();
        }
        boolean isBuy = order.isBuy();
        int price = order.getPrice();
        Map<Integer, Set<Order>> restingOrders = isBuy ? buyRestingOrders : sellRestingOrders;
        Set<Order> restingOrdersValues = restingOrders.get(price);
        restingOrdersValues.remove(order);
        if (restingOrdersValues.isEmpty()) {
            restingOrders.remove(price);
            resetStartPriceCache(isBuy, price);
        }
        totalSizeAtPriceCache.remove(order);
        orders.remove(orderId);
    }

    private void setStartPriceCache(boolean isBuy, Integer price) {
        if (isBuy) {
            highestBuyPrice = price;
        } else {
            lowestSellPrice = price;
        }
    }

    private void resetStartPriceCache(boolean isBuy, Integer price) {
        if (isBuy) {
            if (highestBuyPrice != null && highestBuyPrice.equals(price)) {
                highestBuyPrice = null;
            }
        } else if (lowestSellPrice != null && lowestSellPrice.equals(price)) {
            lowestSellPrice = null;
        }
    }

    @Override
    public int getTotalSizeAtPrice(int price) throws RequestRejectedException {
        if (price < 0) {
            throw new RequestRejectedException();
        }
        return totalSizeAtPriceCache.getSize(price);
    }

    @Override
    public int getHighestBuyPrice() {
        return getFirstPrice(true);
    }

    @Override
    public int getLowestSellPrice() {
        return getFirstPrice(false);
    }

    private int getFirstPrice(boolean isBuy) {
        Integer cacheValue = isBuy ? highestBuyPrice : lowestSellPrice;
        if (cacheValue != null) {
            return cacheValue;
        }
        int result = 0;
        Optional<Integer> firstPrice = (isBuy ? buyRestingOrders : sellRestingOrders).keySet().stream().findFirst();
        if (firstPrice.isPresent()) {
            result = firstPrice.get();
            setStartPriceCache(isBuy, result);
        }
        return result;
    }

    private Map<Integer, Set<Order>> buyRestingOrders = new TreeMap<>(Comparator.reverseOrder());
    private Map<Integer, Set<Order>> sellRestingOrders = new TreeMap<>();
    private Map<Long, Order> orders = new HashMap<>();
    TotalSizeAtPriceCache totalSizeAtPriceCache = new TotalSizeAtPriceCache();
    private Integer lowestSellPrice;
    private Integer highestBuyPrice;
}
