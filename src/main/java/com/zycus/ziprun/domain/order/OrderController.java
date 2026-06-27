package com.zycus.ziprun.domain.order;

import com.zycus.ziprun.common.enums.OrderStatus;
import com.zycus.ziprun.common.response.ApiResponse;
import com.zycus.ziprun.dto.request.CreateOrderRequest;
import com.zycus.ziprun.dto.response.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        OrderResponse order = orderService.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order created and assigned successfully")
                        .data(order)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders(
            @RequestParam(required = false) OrderStatus status) {

        return ResponseEntity.ok(ApiResponse.<List<OrderResponse>>builder()
                .success(true)
                .message("Orders retrieved successfully")
                .data(orderService.getOrders(status))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order retrieved successfully")
                .data(orderService.getOrder(orderId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PatchMapping("/{orderId}/deliver")
    public ResponseEntity<ApiResponse<OrderResponse>> markDelivered(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order marked as delivered")
                .data(orderService.markDelivered(orderId))
                .timestamp(LocalDateTime.now())
                .build());
    }
}