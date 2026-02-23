// FileRepository.java
package com.dfs.metadata_service.repository;

import com.dfs.metadata_service.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}