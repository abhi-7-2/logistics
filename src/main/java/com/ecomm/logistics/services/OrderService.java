package com.ecomm.logistics.services;

import com.ecomm.logistics.dtos.OrderRequest;
import com.ecomm.logistics.dtos.OrderResponse;
import com.ecomm.logistics.dtos.PagedResponse;
import com.ecomm.logistics.exceptions.ResourceNotFoundException;
import com.ecomm.logistics.models.Order;
import com.ecomm.logistics.models.Order.FinancialStatus;
import com.ecomm.logistics.models.Order.FulfillmentOverallStatus;
import com.ecomm.logistics.models.Order.OrderStatus;
import com.ecomm.logistics.repositories.OrderRepository;
import com.ecomm.logistics.repositories.OrganizationRepository;
import com.ecomm.logistics.repositories.WebsiteRepository;
import jakarta.persistence.criteria.Predicate;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrganizationRepository organizationRepository;
    private final WebsiteRepository websiteRepository;
    private final ModelMapper modelMapper;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    public OrderService(OrderRepository orderRepository, OrganizationRepository organizationRepository,
            WebsiteRepository websiteRepository, ModelMapper modelMapper) {
        this.orderRepository = orderRepository;
        this.organizationRepository = organizationRepository;
        this.websiteRepository = websiteRepository;
        this.modelMapper = modelMapper;
    }

    private String generateAlphanumericId(int length, String prefix) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return prefix + sb.toString().substring(prefix.length());
    }

    private void validateOrgAndWebsite(String orgId, String websiteId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization not found with id: " + orgId);
        }
        if (!websiteRepository.existsByOrgIdAndId(orgId, websiteId)) {
            throw new ResourceNotFoundException(
                    "Website not found with id: " + websiteId + " for organization: " + orgId);
        }
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        validateOrgAndWebsite(request.getOrgId(), request.getWebsiteId());

        return orderRepository.findByOrgIdAndWebsiteIdAndExternalOrderId(
                request.getOrgId(), request.getWebsiteId(), request.getExternalOrderId())
                .map(existingOrder -> updateExistingOrder(existingOrder, request))
                .orElseGet(() -> createNewOrder(request));
    }

    private OrderResponse createNewOrder(OrderRequest request) {
        Order order = modelMapper.map(request, Order.class);
        order.setId(generateAlphanumericId(12, "ORD"));

        if (order.getStatus() == null)
            order.setStatus(OrderStatus.CREATED);
        if (order.getFinancialStatus() == null)
            order.setFinancialStatus(FinancialStatus.UNKNOWN);
        if (order.getFulfillmentStatus() == null)
            order.setFulfillmentStatus(FulfillmentOverallStatus.UNKNOWN);

        if (order.getItems() != null) {
            order.getItems().forEach(item -> {
                item.setId(generateAlphanumericId(12, "ITM"));
                item.setOrder(order);
            });
        }

        Order saved = orderRepository.save(order);
        return modelMapper.map(saved, OrderResponse.class);
    }

    private OrderResponse updateExistingOrder(Order existingOrder, OrderRequest request) {
        modelMapper.map(request, existingOrder);
        // Note: Reconciling items would be more complex; for now we update fields.
        Order saved = orderRepository.save(existingOrder);
        return modelMapper.map(saved, OrderResponse.class);
    }

    public OrderResponse getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    public PagedResponse<OrderResponse> listOrders(String orgId, String websiteId, 
            java.time.LocalDateTime from, java.time.LocalDateTime to,
            int page, int size, String sort,
            OrderStatus status, FinancialStatus financialStatus, FulfillmentOverallStatus fulfillmentStatus) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization not found with id: " + orgId);
        }

        String[] sortParts = sort.split(",");
        String property = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        Specification<Order> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("orgId"), orgId));
            
            if (websiteId != null) {
                predicates.add(cb.equal(root.get("websiteId"), websiteId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ingestedAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("ingestedAt"), to));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (financialStatus != null) {
                predicates.add(cb.equal(root.get("financialStatus"), financialStatus));
            }
            if (fulfillmentStatus != null) {
                predicates.add(cb.equal(root.get("fulfillmentStatus"), fulfillmentStatus));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        return PagedResponse.<OrderResponse>builder()
                .data(orderPage.getContent().stream()
                        .map(order -> modelMapper.map(order, OrderResponse.class))
                        .collect(Collectors.toList()))
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .hasNext(orderPage.hasNext())
                .build();
    }

    public PagedResponse<OrderResponse> searchOrdersByExternal(String orgId, String websiteId, String externalOrderId,
            String externalOrderNumber, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage;

        if (externalOrderId != null) {
            orderPage = orderRepository.findByOrgIdAndExternalOrderIdContainingIgnoreCase(orgId, externalOrderId,
                    pageable);
        } else if (externalOrderNumber != null) {
            orderPage = orderRepository.findByOrgIdAndExternalOrderNumberContainingIgnoreCase(orgId,
                    externalOrderNumber, pageable);
        } else {
            orderPage = orderRepository.findByOrgId(orgId, pageable);
        }

        return PagedResponse.<OrderResponse>builder()
                .data(orderPage.getContent().stream()
                        .map(order -> modelMapper.map(order, OrderResponse.class))
                        .collect(Collectors.toList()))
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .hasNext(orderPage.hasNext())
                .build();
    }

    @Transactional
    public OrderResponse updateOrder(String orderId, OrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        validateOrgAndWebsite(request.getOrgId(), request.getWebsiteId());
        modelMapper.map(request, order);
        Order saved = orderRepository.save(order);
        return modelMapper.map(saved, OrderResponse.class);
    }

    @Transactional
    public OrderResponse patchOrder(String orderId, OrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (request.getOrgId() != null && request.getWebsiteId() != null) {
            validateOrgAndWebsite(request.getOrgId(), request.getWebsiteId());
        }
        
        // Only update non-null fields
        if (request.getStatus() != null) order.setStatus(request.getStatus());
        if (request.getFinancialStatus() != null) order.setFinancialStatus(request.getFinancialStatus());
        if (request.getFulfillmentStatus() != null) order.setFulfillmentStatus(request.getFulfillmentStatus());
        if (request.getExternalOrderNumber() != null) order.setExternalOrderNumber(request.getExternalOrderNumber());
        if (request.getCustomerEmail() != null) order.setCustomerEmail(request.getCustomerEmail());
        if (request.getOrderTotal() != null) order.setOrderTotal(request.getOrderTotal());
        if (request.getCurrency() != null) order.setCurrency(request.getCurrency());
        if (request.getOrderCreatedAt() != null) order.setOrderCreatedAt(request.getOrderCreatedAt());
        if (request.getOrderUpdatedAt() != null) order.setOrderUpdatedAt(request.getOrderUpdatedAt());
        
        Order saved = orderRepository.save(order);
        return modelMapper.map(saved, OrderResponse.class);
    }

    @Transactional
    public void deleteOrder(String orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }
        orderRepository.deleteById(orderId);
    }
}
