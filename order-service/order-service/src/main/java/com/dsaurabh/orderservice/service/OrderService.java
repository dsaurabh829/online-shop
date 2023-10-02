package com.dsaurabh.orderservice.service;

import com.dsaurabh.orderservice.dto.InventoryResponse;
import com.dsaurabh.orderservice.dto.OrderLineItemsDto;
import com.dsaurabh.orderservice.dto.OrderRequest;
import com.dsaurabh.orderservice.model.Order;
import com.dsaurabh.orderservice.model.OrderLineItems;
import com.dsaurabh.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient.Builder webClientBuilder;

    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

       List<OrderLineItems> lineItems =  orderRequest.getOrderLineItemsDtos()
                .stream()
                .map(this::mapToDto).toList();

       order.setOrderLineItems(lineItems);

       List<String> skuCodes = order.getOrderLineItems()
               .stream()
               .map(OrderLineItems::getSkuCode)
               .toList();

       // call inventory service and place order if product is present
        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode" , skuCodes)
                        .build())
                        .retrieve()
                                .bodyToMono(InventoryResponse[].class)
                                        .block();

        boolean allProductsIsInStock = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::getIsInStock);
      if(Boolean.TRUE.equals(allProductsIsInStock))
      {
          orderRepository.save(order);
          return "Order Placed successfully";
      }
      else
          throw new RuntimeException("Product out of stock");
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto){
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }
}
