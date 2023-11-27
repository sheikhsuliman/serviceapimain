package com.siryus.swisscon.api.general.title;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleRepository extends JpaRepository<TitleEntity, Integer> {
}
