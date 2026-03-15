// StorageNodeRepository.java
package com.dfs.metadata_service.repository;

import com.dfs.metadata_service.entity.StorageNode;
import com.dfs.metadata_service.entity.StorageNode.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface StorageNodeRepository extends JpaRepository<StorageNode, Long> {
    Optional<StorageNode> findByIpAddressAndPort(String ipAddress, Integer port);
    List<StorageNode> findByStatus(Status status);
}

// update: 2026-05-14 23:30:30.539752

// update: 2026-05-14 23:30:43.186833

// update: 2026-05-14 23:30:47.178495

// update: 2026-05-14 23:30:49.990826

// update: 2026-05-14 23:30:53.503992

// update: 2026-05-14 23:30:58.240323
