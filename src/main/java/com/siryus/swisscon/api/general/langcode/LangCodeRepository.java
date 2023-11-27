package com.siryus.swisscon.api.general.langcode;

import java.lang.String;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LangCodeRepository extends JpaRepository<LangCode, String> {
}
