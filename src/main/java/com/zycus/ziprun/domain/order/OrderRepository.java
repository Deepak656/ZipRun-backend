package com.zycus.ziprun.domain.order;

import com.zycus.ziprun.common.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.assignedAgent.id = :agentId AND o.status = :status")
    List<Order> findByAssignedAgentIdAndStatus(
            @Param("agentId") String agentId,
            @Param("status") OrderStatus status
    );

    @Query("SELECT o FROM Order o WHERE o.assignedAgent.id = :agentId AND o.status IN :statuses")
    List<Order> findActiveOrdersByAgentId(
            @Param("agentId") String agentId,
            @Param("statuses") List<OrderStatus> statuses
    );
}