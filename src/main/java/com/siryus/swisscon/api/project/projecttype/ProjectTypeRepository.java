package com.siryus.swisscon.api.project.projecttype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectTypeRepository extends JpaRepository<ProjectType, Integer> {
}
