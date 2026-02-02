package com.metacoding.delivery.usecase;

import com.metacoding.delivery.core.handler.ex.Exception404;
import com.metacoding.delivery.domain.delivery.Delivery;
import com.metacoding.delivery.repository.DeliveryRepository;
import com.metacoding.delivery.web.dto.DeliveryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DeliveryService implements CreateDeliveryUseCase, GetDeliveryUseCase, CancelDeliveryUseCase, CompleteDeliveryUseCase {
    private final DeliveryRepository deliveryRepository;

    @Override
    @Transactional
    public DeliveryResponse saveDelivery(int orderId, String address) {
        Delivery delivery = Delivery.create(orderId, address);
        deliveryRepository.save(delivery);
        //delivery.complete();
        return DeliveryResponse.from(delivery);
    }

    @Override
    public DeliveryResponse findById(int deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new Exception404("배달 정보를 조회할 수 없습니다."));
        return DeliveryResponse.from(delivery);
    }

    @Override
    @Transactional
    public void cancelDelivery(int deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new Exception404("배달 정보를 조회할 수 없습니다."));
        delivery.cancel();
    }

    @Override
    @Transactional
    public void completeDelivery(int orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new Exception404("배달 정보를 조회할 수 없습니다."));
        delivery.complete();
    }

    public DeliveryResponse findByIdByOrderId(int orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new Exception404("배달 정보를 조회할 수 없습니다."));
        return DeliveryResponse.from(delivery);
    }
}












