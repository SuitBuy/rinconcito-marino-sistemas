package com.rinconcitomarino.repository;

import com.rinconcitomarino.model.Opinion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OpinionRepository extends JpaRepository<Opinion, Long>, JpaSpecificationExecutor<Opinion> {
}
