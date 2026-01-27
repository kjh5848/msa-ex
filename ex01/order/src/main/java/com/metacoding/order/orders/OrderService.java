package com.metacoding.order.orders;

import com.metacoding.order.adapter.*;
import com.metacoding.order.adapter.dto.*;
import com.metacoding.order.core.handler.ex.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductClient productClient;
    private final DeliveryClient deliveryClient;

    @Transactional
    public OrderResponse.DTO createOrder(int userId, int productId, int quantity, Long price, String address) {
        // 보상 트랜잭션 실행 시 실행된 작업만 롤백 처리를 위해 변수 선언
        Boolean productDecreased = false;
        Boolean orderCreated = false;
        Boolean orderItemCreated = false;
        Boolean deliveryCreated = false;
        // 보상 트랜잭션에서 사용할 주문 정보를 위한 변수 선언
        Order savedOrder = null;

        try {
            // 1. 상품 재고 감소 
            productClient.decreaseQuantity(productId, quantity);
            productDecreased = true;
                    
            // 2. 주문 생성
            Order order = Order.create(
                userId,
                productId,
                quantity
            );
            savedOrder = orderRepository.save(order);
            orderCreated = true;

            // 3. 주문 아이템 생성 
            OrderItem orderItem = OrderItem.create(
                order.getId(),
                productId,
                quantity,
                price
            );
            orderItem.validatePrice(price);
            orderItemRepository.save(orderItem);
            orderItemCreated = true;

            // 4. 배달 생성
            DeliveryRequest.SaveDTO deliveryRequest = new DeliveryRequest.SaveDTO(order.getId(), address);
            deliveryClient.saveDelivery(deliveryRequest);
            deliveryCreated = true;
            
            // 5. 주문 완료
            savedOrder.complete();  // 더티 체킹으로 상태 저장

            return new OrderResponse.DTO(savedOrder);
            
        } catch (Exception e) {
            // 보상 트랜잭션 실행

            // 배달 취소
            if (deliveryCreated == true) {
                System.out.println("배달 취소");
                deliveryClient.cancelDelivery(savedOrder.getId());
            }      
            
            // 상품 재고 복구
            if (productDecreased == true) {
                System.out.println("상품 재고 복구");
                productClient.increaseQuantity(productId, quantity);
            }

            // 주문 생성 중 오류가 발생하면 자동 롤백
            if (orderCreated == true || orderItemCreated == true) {
                System.out.println("자동 롤백");
            }
            
            throw new Exception500("주문 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public OrderResponse.DTO findById(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Exception404("주문을 찾을 수 없습니다."));
        return new OrderResponse.DTO(order);
    }

    @Transactional
    public OrderResponse.DTO cancelOrder(int orderId) {
        // 1. 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Exception404("주문을 찾을 수 없습니다."));

        try {
            // 3. 배송 취소
            deliveryClient.cancelDelivery(orderId);

            // 4. 상품 재고 복구
            productClient.increaseQuantity(order.getProductId(), order.getQuantity());

            // 5. 주문 취소
            order.cancel();  // 더티 체킹으로 상태 저장

            return new OrderResponse.DTO(order);

        } catch (Exception e) {
            throw new Exception500("주문 취소 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
