package com.siryus.swisscon.api.auth.user;

import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.auth.AuthConstants;
import com.siryus.swisscon.api.auth.AuthException;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleService;
import com.siryus.swisscon.api.exceptions.NotFoundException;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileService;
import com.siryus.swisscon.api.general.country.Country;
import com.siryus.swisscon.api.general.favorite.FavoriteRepository;
import com.siryus.swisscon.api.general.gender.Gender;
import com.siryus.swisscon.api.general.langcode.LangCode;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;
import com.siryus.swisscon.api.util.CustomStringUtils;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.DTOValidator;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service("userService")
public class UserService {

    private final FavoriteRepository favoriteRepository;
    private final FileService fileService;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyUserRoleService companyUserRoleService;
    private final SecurityHelper securityHelper;
    private final EventsEmitter eventsEmitter;

    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Autowired
    public UserService(
            FavoriteRepository favoriteRepository,
            FileService fileService,
            CompanyUserRoleRepository companyUserRoleRepository,
            ProjectUserRoleRepository projectUserRoleRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            CompanyUserRoleService companyUserRoleService,
            SecurityHelper securityHelper,
            EventsEmitter eventsEmitter
    ) {
        this.favoriteRepository = favoriteRepository;
        this.fileService = fileService;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyUserRoleService = companyUserRoleService;
        this.securityHelper = securityHelper;
        this.eventsEmitter = eventsEmitter;
    }

    public UserProfileDTO getOtherUserProfile(Integer userId) {
        return UserProfileDTO.fromUser(userRepository.findById(userId).orElseThrow(NotFoundException::new));
    }

    @Transactional
    public UserProfileDTO updateOtherUserProfile(Integer userId, UserProfileDTO userProfileDTO) {
        DTOValidator.validateAndThrow(userProfileDTO);

        return updateUserProfile(
                userRepository.findById(userId).orElseThrow(NotFoundException::new),
                userProfileDTO
        );
    }

    @Transactional
    public UserProfileDTO updateUserProfile(UserProfileDTO userProfileDTO) {
        DTOValidator.validateAndThrow(userProfileDTO);

        return updateUserProfile(
                userRepository.findById(Integer.valueOf(LecwUtils.currentUser().getId())).orElseThrow(NotFoundException::new),
                userProfileDTO
        );
    }

    public boolean userWithEmailExists(String email) {
        return userRepository.userWithEmailExists(email);
    }

    public boolean userWithMobileAlreadyExists(Integer countryCode, String mobile) {
        return userRepository.userWithMobileExists(countryCode, CustomStringUtils.removeLeadingZeros(mobile));
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new RuntimeException("Cannot update an user without id");
        }
        Optional<User> userOpt = userRepository.findById(user.getId());
        assert userOpt.isPresent();

        if (!userOpt.get().getPassword().equals(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword())); //TODO check if this is correct here
        }
        try {
            return userRepository.save(user);
        } finally {
            eventsEmitter.emitCacheUpdate(ReferenceType.USER, user.getId());
        }
    }

    public User findById(Integer id) {
        return id == null ? null : userRepository.findById(id).orElse(null);
    }

    public User create(User user) {
        return userRepository.save(user);
    }

    public void validateUserId(Integer id) {
        if(!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found (id = " + id + ")");
        }
    }

    @Transactional()
    public CompanyUserRole toggleAdmin(Integer userId, boolean makeAdmin) {
        CompanyUserRole companyUserRole = companyUserRoleService.findCompanyRoleByUser(userId);

        // company owner cannot be toggled for admin
        if (isNotCompanyOwner(companyUserRole)) {
            Role role = makeAdmin ? roleRepository.getRoleByName(RoleName.COMPANY_ADMIN.toString()) :
                    roleRepository.getRoleByName(RoleName.COMPANY_WORKER.toString());
            companyUserRole.setRole(role);
            return companyUserRoleRepository.save(companyUserRole);
        }
        return companyUserRole;
    }

    public LangCode getCurrentUserLang() {
        return Optional.ofNullable(getUser(securityHelper.currentUserId()).getPrefLang())
                .orElse(LangCode
                        .builder()
                        .id(AuthConstants.DEFAULT_LANGUAGE)
                        .build());
    }

    public Optional<User> findByMobileForLogin(String mobile) {
        return userRepository.findByMobile(mobile)
                .or(()->userRepository.findByMobileWithZeroPrefix(mobile))
                .or(()->findByMobileWithoutCountryCode(mobile));
    }

    private Optional<User> findByMobileWithoutCountryCode(String mobile) {
        List<User> users = userRepository.findMobileWithoutCountryCode(mobile);
        if(users.size() > 1) {
            throw AuthException.multipleUsersWithMobileFound(mobile);
        }
        return !users.isEmpty() ? Optional.of(users.get(0)) : Optional.empty();
    }

    private UserProfileDTO updateUserProfile(User user, UserProfileDTO userProfileDTO) {
        // update picture and delete old one
        File previousPicture = user.getPicture();
        if(userProfileDTO.getProfileImageId() != null) {
            File newPicture = fileService.findById(userProfileDTO.getProfileImageId());
            if(fileService.isNewPicture(previousPicture, newPicture)) {
                newPicture.setReferenceType(ReferenceType.USER.toString());
                newPicture.setReferenceId(user.getId());
                File updatedNewPicture = fileService.update(newPicture);
                user.setPicture(updatedNewPicture);
                Optional.ofNullable(previousPicture).ifPresent(p -> fileService.disable(previousPicture));
            }
        }
        else if(previousPicture != null) {
            user.setPicture(null);
            fileService.disable(previousPicture);
        }

        // set simple properties
        user.setCategory(userProfileDTO.getCategory());
        user.setPosition(userProfileDTO.getPosition());
        user.setTitle(userProfileDTO.getTitle());
        user.setAboutMe(userProfileDTO.getAboutMe());
        user.setDriversLicense(userProfileDTO.getDriversLicense());
        user.setCar(userProfileDTO.getCar());
        user.setLicensePlate(userProfileDTO.getLicensePlate());
        user.setResponsibility(userProfileDTO.getResponsibility());
        user.setGivenName(userProfileDTO.getFirstName());
        user.setSurName(userProfileDTO.getLastName());
        user.setBirthdate(userProfileDTO.getBirthDate());
        user.setSsn(userProfileDTO.getSsn());

        // set residence
        Country residence = new Country();
        residence.setId(userProfileDTO.getCountryOfResidenceId());
        user.setCountryOfResidence(residence);

        // set nationality
        Country nationality = new Country();
        nationality.setId(userProfileDTO.getNationalityId());
        user.setNationality(nationality);

        // set gender
        Gender gender = new Gender();
        gender.setId(userProfileDTO.getGenderId());
        user.setGender(gender);

        User updatedUser = update(user);

        eventsEmitter.emitNotification(NotificationEvent.fromCompany(NotificationType.USER_PROFILE_UPDATED,
                companyUserRoleService.findCompanyRoleByUser(user.getId()).getCompany().getId(),
                securityHelper.currentUserId(), user.getId(), user.getId()));

        return UserProfileDTO.fromUser(updatedUser);
    }

    private boolean isNotCompanyOwner(CompanyUserRole companyUserRole) {
        return !RoleName.COMPANY_OWNER.toString().equals(companyUserRole.getRole().getName());
    }

    public User getUser(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found (id = " + userId + ")"));
    }

    public AuthorDTO getAuthor(Integer userId) {
        return AuthorDTO.from(getUser(userId));
    }
}
