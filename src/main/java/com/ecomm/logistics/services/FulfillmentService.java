package com.ecomm.logistics.services;

import com.ecomm.logistics.dtos.FulfillmentRequest;
import com.ecomm.logistics.dtos.FulfillmentResponse;
import com.ecomm.logistics.dtos.PagedResponse;
import com.ecomm.logistics.exceptions.ResourceNotFoundException;
import com.ecomm.logistics.models.Fulfillment;
import com.ecomm.logistics.models.Fulfillment.FulfillmentStatus;
import com.ecomm.logistics.models.Order;
import com.ecomm.logistics.models.Order.FulfillmentOverallStatus;
import com.ecomm.logistics.repositories.FulfillmentRepository;
import com.ecomm.logistics.repositories.OrderRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FulfillmentService {

    private final FulfillmentRepository fulfillmentRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    public FulfillmentService(FulfillmentRepository fulfillmentRepository,
            OrderRepository orderRepository,
            ModelMapper modelMapper) {
        this.fulfillmentRepository = fulfillmentRepository;
        this.orderRepository = orderRepository;
        this.modelMapper = modelMapper;
    }

    private String generateAlphanumericId(int length, String prefix) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return prefix + sb.toString().substring(prefix.length());
    }

    @Transactional
    public FulfillmentResponse createFulfillment(String orderId, FulfillmentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        Fulfillment fulfillment = modelMapper.map(request, Fulfillment.class);
        fulfillment.setId(generateAlphanumericId(12, "FUL"));
        fulfillment.setOrgId(order.getOrgId());
        fulfillment.setOrderId(orderId);
        if (fulfillment.getStatus() == null) {
            fulfillment.setStatus(FulfillmentStatus.CREATED);
        }

        Fulfillment saved = fulfillmentRepository.save(fulfillment);
        updateOrderFulfillmentStatus(orderId);
        return modelMapper.map(saved, FulfillmentResponse.class);
    }

    public FulfillmentResponse getFulfillmentById(String orderId, String fulfillmentId) {
        Fulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Fulfillment not found with id: " + fulfillmentId));
        
        if (!fulfillment.getOrderId().equals(orderId)) {
            throw new ResourceNotFoundException("Fulfillment not found in this order");
        }
        
        return modelMapper.map(fulfillment, FulfillmentResponse.class);
    }

    public PagedResponse<FulfillmentResponse> listFulfillments(String orderId, 
            java.time.LocalDateTime from, java.time.LocalDateTime to,
            int page, int size, String sort,
            FulfillmentStatus status, String carrier) {
        String[] sortParts = sort.split(",");
        String property = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        Specification<Fulfillment> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("orderId"), orderId));
            
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (carrier != null) {
                predicates.add(cb.like(cb.lower(root.get("carrier")), "%" + carrier.toLowerCase() + "%"));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Fulfillment> fulfillmentPage = fulfillmentRepository.findAll(spec, pageable);

        return PagedResponse.<FulfillmentResponse>builder()
                .data(fulfillmentPage.getContent().stream()
                        .map(f -> modelMapper.map(f, FulfillmentResponse.class))
                        .collect(Collectors.toList()))
                .page(fulfillmentPage.getNumber())
                .size(fulfillmentPage.getSize())
                .totalElements(fulfillmentPage.getTotalElements())
                .totalPages(fulfillmentPage.getTotalPages())
                .hasNext(fulfillmentPage.hasNext())
                .build();
    }

    public PagedResponse<FulfillmentResponse> searchFulfillmentsByExternal(String orderId, String externalFulfillmentId,
            int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Fulfillment> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("orderId"), orderId));
            predicates.add(cb.like(cb.lower(root.get("externalFulfillmentId")), "%" + externalFulfillmentId.toLowerCase() + "%"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Fulfillment> fulfillmentPage = fulfillmentRepository.findAll(spec, pageable);

        return PagedResponse.<FulfillmentResponse>builder()
                .data(fulfillmentPage.getContent().stream()
                        .map(f -> modelMapper.map(f, FulfillmentResponse.class))
                        .collect(Collectors.toList()))
                .page(fulfillmentPage.getNumber())
                .size(fulfillmentPage.getSize())
                .totalElements(fulfillmentPage.getTotalElements())
                .totalPages(fulfillmentPage.getTotalPages())
                .hasNext(fulfillmentPage.hasNext())
                .build();
    }

    @Transactional
    public FulfillmentResponse updateFulfillment(String orderId, String fulfillmentId, FulfillmentRequest request) {
        Fulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Fulfillment not found with id: " + fulfillmentId));

        if (!fulfillment.getOrderId().equals(orderId)) {
            throw new ResourceNotFoundException("Fulfillment not found in this order");
        }

        modelMapper.map(request, fulfillment);
        Fulfillment saved = fulfillmentRepository.save(fulfillment);
        updateOrderFulfillmentStatus(fulfillment.getOrderId());
        return modelMapper.map(saved, FulfillmentResponse.class);
    }

    @Transactional
    public FulfillmentResponse patchFulfillment(String orderId, String fulfillmentId, FulfillmentRequest request) {
        Fulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Fulfillment not found with id: " + fulfillmentId));

        if (!fulfillment.getOrderId().equals(orderId)) {
            throw new ResourceNotFoundException("Fulfillment not found in this order");
        }

        // Only update non-null fields
        if (request.getStatus() != null) fulfillment.setStatus(request.getStatus());
        if (request.getCarrier() != null) fulfillment.setCarrier(request.getCarrier());
        if (request.getServiceLevel() != null) fulfillment.setServiceLevel(request.getServiceLevel());
        if (request.getShippedAt() != null) fulfillment.setShippedAt(request.getShippedAt());
        if (request.getDeliveredAt() != null) fulfillment.setDeliveredAt(request.getDeliveredAt());
        
        Fulfillment saved = fulfillmentRepository.save(fulfillment);
        updateOrderFulfillmentStatus(fulfillment.getOrderId());
        return modelMapper.map(saved, FulfillmentResponse.class);
    }

    @Transactional
    public void deleteFulfillment(String orderId, String fulfillmentId) {
        Fulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Fulfillment not found with id: " + fulfillmentId));
        
        if (!fulfillment.getOrderId().equals(orderId)) {
            throw new ResourceNotFoundException("Fulfillment not found in this order");
        }
        
        String orderIdToUpdate = fulfillment.getOrderId();
        fulfillmentRepository.delete(fulfillment);
        updateOrderFulfillmentStatus(orderIdToUpdate);
    }

    private void updateOrderFulfillmentStatus(String orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null)
            return;

        List<Fulfillment> fulfillments = fulfillmentRepository
                .findByOrgIdAndOrderId(order.getOrgId(), orderId, Pageable.unpaged()).getContent();

        if (fulfillments.isEmpty()) {
            order.setFulfillmentStatus(FulfillmentOverallStatus.UNFULFILLED);
        } else {
            boolean allDelivered = fulfillments.stream().allMatch(f -> f.getStatus() == FulfillmentStatus.DELIVERED);
            boolean anyShipped = fulfillments.stream().anyMatch(
                    f -> f.getStatus() == FulfillmentStatus.SHIPPED || f.getStatus() == FulfillmentStatus.DELIVERED);
            boolean allCancelled = fulfillments.stream().allMatch(f -> f.getStatus() == FulfillmentStatus.CANCELLED);

            if (allDelivered) {
                order.setFulfillmentStatus(FulfillmentOverallStatus.FULFILLED);
            } else if (allCancelled) {
                order.setFulfillmentStatus(FulfillmentOverallStatus.CANCELLED);
            } else if (anyShipped) {
                order.setFulfillmentStatus(FulfillmentOverallStatus.PARTIAL);
            } else {
                order.setFulfillmentStatus(FulfillmentOverallStatus.UNFULFILLED);
            }
        }
        orderRepository.save(order);
    }
}
