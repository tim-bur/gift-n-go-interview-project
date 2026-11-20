package com.elemental.tech.workitems.persistence;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.elemental.tech.workitems.domain.WorkItem;
import com.elemental.tech.workitems.domain.WorkItem.Priority;
import com.elemental.tech.workitems.domain.WorkItem.Status;

public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {
    // example methods for 
    List<WorkItem> findByPriorityAndStatus(Priority priority, Status status);
    
    List<WorkItem> findByPriority(Priority priority, Sort sort);

    List<WorkItem> findByPriority(Priority priority, Pageable pageable);
}
