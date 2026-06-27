package com.zycus.ziprun.domain.agent;

import com.zycus.ziprun.common.enums.AgentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, String> {

    List<Agent> findByStatus(AgentStatus status);

    List<Agent> findByStatusIn(List<AgentStatus> statuses);
}