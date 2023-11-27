/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.init;

import com.siryus.swisscon.api.auth.user.UserCategory;
import com.siryus.swisscon.api.base.SimpleEntityDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import com.siryus.swisscon.api.company.companylegaltype.CompanyLegalType;
import com.siryus.swisscon.api.company.numworkersofcompany.NumWorkersOfCompany;
import com.siryus.swisscon.api.customroles.dto.CustomPermissionDTO;
import com.siryus.swisscon.api.customroles.dto.CustomRoleDTO;
import com.siryus.swisscon.api.general.currency.CurrencyDTO;
import com.siryus.swisscon.api.general.gender.Gender;
import com.siryus.swisscon.api.general.title.TitleEntity;
import com.siryus.swisscon.api.general.unit.UnitDTO;
import com.siryus.swisscon.api.project.projecttype.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * This class returns global web app settings.
 * 
 * As an example of where this data can be requested is from both non-authenticated and authenticated pages.
 * 
 * @author hng
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InitSettingsDTO {
    private List<CompanyLegalType> companyTypes;

    private List<UserCategory> userCategories;

    private List<CatalogNodeDTO> trades;

    private List<NumWorkersOfCompany> numberOfEmployees;

    private List<Gender> genders;

    private List<TitleEntity> titles;

    private List<CustomRoleDTO> roles;
    
    private List<ProjectType> projectTypes;

    private List<SimpleEntityDTO> countries;

    private List<CustomPermissionDTO> permissions;

    private List<String> languages;

    private List<CurrencyDTO> currencies;

    private List<UnitDTO> units;
}
