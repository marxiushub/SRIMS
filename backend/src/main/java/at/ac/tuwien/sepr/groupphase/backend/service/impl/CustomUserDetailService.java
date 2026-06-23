package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.CustomerCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.UserCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.UserDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.search.CustomerSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.searchresponse.UserSearchResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.PasswordChangeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.UserMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Role;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.UserType;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.StaffRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.CurrentUserService;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.EmailService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CustomUserDetailService implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;
    private final UserServiceValidator validator;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final Map<UserType, JpaRepository<? extends ApplicationUser, Long>> repositoryMap;
    private final UserMapper mapper;
    private final RoleRepository roleRepository;
    private final PermissionService permissionService;
    private final EmailService emailService;
    private final CurrentUserService currentUserService;

    /**
     * Constructor for UserService. Initializes the service with the necessary repositories and mapper.
     *
     * @param userRepository        the repository for managing user entities
     * @param passwordEncoder       the encoder used to hash the passwords of users
     * @param jwtTokenizer          the library tho create and manage sessions and their tokens for users
     * @param customerRepository    the repository for managing customer entities
     * @param staffRepository       the repository for managing staff entities
     * @param mapper                the mapper for converting between entities and DTOs
     * @param currentUserService    the service to retrieve the current ApplicationUser from the Security Context
     */
    @Autowired
    public CustomUserDetailService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenizer jwtTokenizer,  CustomerRepository customerRepository, StaffRepository staffRepository,
                                   UserMapper mapper, RoleRepository roleRepository, UserServiceValidator validator, PermissionService permissionService, EmailService  emailService, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenizer = jwtTokenizer;
        this.roleRepository = roleRepository;
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
        this.validator = validator;
        this.permissionService = permissionService;
        this.repositoryMap = Map.of(
            UserType.CUSTOMER, customerRepository,
            UserType.STAFF, staffRepository
        );
        this.mapper = mapper;
        this.currentUserService = currentUserService;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LOGGER.debug("Load all user by email");
        try {
            ApplicationUser applicationUser = findApplicationUserByEmail(email);

            List<String> authorityStrings = permissionService.getEffectivePermissions(applicationUser).stream().toList();

            String[] authoritiesArray = authorityStrings.toArray(new String[0]);
            List<GrantedAuthority> grantedAuthorities = AuthorityUtils.createAuthorityList(authoritiesArray);

            return new User(applicationUser.getEmail(), applicationUser.getPassword(), grantedAuthorities);
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public ApplicationUser findApplicationUserByEmail(String email) {
        LOGGER.debug("Find application user by email");
        ApplicationUser applicationUser = userRepository.findUserByEmail(email).orElseThrow(() -> new NotFoundException(String.format("Could not find the user with the email address %s", email)));
        return applicationUser;
    }

    @Override
    public String login(UserLoginDto userLoginDto) {
        UserDetails userDetails = loadUserByUsername(userLoginDto.getEmail());
        if (userDetails != null
            && userDetails.isAccountNonExpired()
            && userDetails.isAccountNonLocked()
            && userDetails.isCredentialsNonExpired()
            && passwordEncoder.matches(userLoginDto.getPassword(), userDetails.getPassword())
        ) {
            ApplicationUser user = userRepository.findUserByEmail(userLoginDto.getEmail()).orElseThrow(() -> new NotFoundException("user for login not found"));
            List<String> permissions = permissionService.getEffectivePermissions(user).stream().toList();

            return jwtTokenizer.getAuthToken(userDetails.getUsername(), user.getId(), permissions);
        }
        throw new BadCredentialsException("Email or password is incorrect or account is locked");
    }


    @Override
    public UserDetailDto createUser(UserCreationDto userCreationDto) {
        LOGGER.trace("Creating user with email {}", userCreationDto.getEmail());

        if (userRepository.findUserByEmail(userCreationDto.getEmail()).isPresent()) {
            throw new ValidationException("Email is already in use", List.of("Email " + userCreationDto.getEmail() + " is already in use."));
        }

        validator.userCreationDtoValidator(userCreationDto);
        ApplicationUser user = userCreationDto.toEntity();
        user.setPassword(passwordEncoder.encode(userCreationDto.getPassword()));
        JpaRepository<ApplicationUser, Long> repo = (JpaRepository<ApplicationUser, Long>) repositoryMap.get(userCreationDto.getType());


        //String roleName = userCreationDto.getType() == UserType.CUSTOMER ? "ROLE_CUSTOMER" : "ROLE_STAFF";
        String roleName = "ROLE_" + userCreationDto.getType().toString();
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new IllegalStateException(roleName + " not found"));
        user.getRoles().clear();
        user.getRoles().add(role);
        ApplicationUser saved = repo.save(user);
        UserDetailDto created = mapper.entityToDetailDto(saved);

        if (userCreationDto instanceof CustomerCreationDto) {
            emailService.sendAccountCreationSuccessEmail(user.getEmail(), user.getUserName());
        }

        return created;
    }


    @Override
    public UserDetailDto updateUser(Long id, UserUpdateDto updateDto) {

        LOGGER.info("Updating user with id {}", id);

        validator.userUpdateDtoValidator(updateDto);
        validator.idTester(id);

        checkUserAccessPermission(id);

        ApplicationUser existingUser = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User with ID " + id + " was not found."));

        mapper.updateEntityFromDto(updateDto, existingUser);

        if (updateDto.getPassword() != null && !updateDto.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        ApplicationUser savedUser = userRepository.save(existingUser);

        return mapper.entityToDetailDto(savedUser);
    }


    @Override
    public void deleteUserById(Long userId) {
        LOGGER.trace("Deleting user with id {}", userId);

        validator.idTester(userId);

        checkUserAccessPermission(userId);

        ApplicationUser user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find user with id %d", userId)));

        userRepository.delete(user);

        LOGGER.trace("Successfully deleted user with id {}", userId);
    }


    @Override
    public UserSearchResponseDto getUserById(Long id) {

        LOGGER.debug("Get user by id {}", id);

        validator.idTester(id);

        checkUserAccessPermission(id);

        ApplicationUser user = userRepository.findById(id)
            .orElseThrow(() ->
                new NotFoundException("User with ID " + id + " was not found.")
            );

        return mapper.entityToSearchResponseDto(user);
    }

    @Override
    public List<UserSearchResponseDto> searchCustomers(CustomerSearchDto searchDto) {

        LOGGER.trace("Search customers with filter {}", searchDto);

        List<Customer> customers =
            customerRepository.searchCustomers(
                normalize(searchDto.getEmail()),
                normalize(searchDto.getUserName()),
                normalize(searchDto.getFirstName()),
                normalize(searchDto.getLastName())
            );

        return customers.stream()
            .map(mapper::entityToSearchResponseDto)
            .toList();
    }

    @Override
    public UserDetailDto changePassword(Long id, PasswordChangeDto passwordChangeDto) {
        LOGGER.info("Changing password for user with id {}", id);

        validator.idTester(id);
        checkUserAccessPermission(id);

        if (passwordChangeDto == null) {
            throw new ValidationException("Validation of the dto for changing passwords failed", List.of("passwordChangeDto is null"));
        }

        if (passwordChangeDto.getOldPassword() == null || passwordChangeDto.getOldPassword().isBlank()) {
            throw new ValidationException("Validation of the dto for changing passwords failed", List.of("oldPassword is blank"));
        }

        if (passwordChangeDto.getNewPassword() == null || passwordChangeDto.getNewPassword().isBlank()) {
            throw new ValidationException("Validation of the dto for changing passwords failed", List.of("newPassword is blank"));
        }

        ApplicationUser existingUser = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User with ID " + id + " was not found."));

        if (!passwordEncoder.matches(passwordChangeDto.getOldPassword(), existingUser.getPassword())) {
            throw new ValidationException("Old password is incorrect");
        }

        if (passwordEncoder.matches(passwordChangeDto.getNewPassword(), existingUser.getPassword())) {
            throw new ValidationException(
                "Validation of the dto for changing passwords failed",
                List.of("newPassword must differ from the current password.")
            );
        }

        existingUser.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));

        ApplicationUser savedUser = userRepository.save(existingUser);

        return mapper.entityToDetailDto(savedUser);
    }

    //Helper Methods:
    //Checks if the given id (requestedUserID) is the same ID as the ID of the user who wants to perform the CRUD action
    private void checkUserAccessPermission(Long requestedUserId) {

        Long currentUserId = currentUserService.getUserId();

        boolean isOwnUser = Objects.equals(currentUserId, requestedUserId);

        boolean hasStaffPermission =
            SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("STAFF"));

        if (!isOwnUser && !hasStaffPermission) {
            throw new AccessDeniedException("You have no permission to perform this action.");
        }
    }

    //Normalizes search-inputs
    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
