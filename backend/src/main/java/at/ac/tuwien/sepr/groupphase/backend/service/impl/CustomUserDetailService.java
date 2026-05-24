package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.UserCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.UserDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.UserMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.UserType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.StaffRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

@Service
public class CustomUserDetailService implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;

    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final Map<UserType, JpaRepository<? extends ApplicationUser, Long>> repositoryMap;
    private final UserMapper mapper;

    /**
     * Constructor for EquipmentService. Initializes the service with the necessary repositories and mapper.
     *
     * @param userRepository        the repository for managing user entities
     * @param passwordEncoder       the encoder used to hash the passwords of users
     * @param jwtTokenizer          the library tho create and manage sessions and their tokens for users
     * @param customerRepository    the repository for managing customer entities
     * @param staffRepository       the repository for managing staff entities
     * @param mapper                the mapper for converting between entities and DTOs
     */
    @Autowired
    public CustomUserDetailService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenizer jwtTokenizer,  CustomerRepository customerRepository, StaffRepository staffRepository, UserMapper mapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenizer = jwtTokenizer;

        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
        this.repositoryMap = Map.of(
            UserType.CUSTOMER, customerRepository,
            UserType.STAFF, staffRepository
        );
        this.mapper = mapper;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LOGGER.debug("Load all user by email");
        try {
            ApplicationUser applicationUser = findApplicationUserByEmail(email);

            List<GrantedAuthority> grantedAuthorities;
            if (applicationUser.getAdmin()) {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER");
            } else {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_USER");
            }

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
            List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
            return jwtTokenizer.getAuthToken(userDetails.getUsername(), roles);
        }
        throw new BadCredentialsException("Username or password is incorrect or account is locked");
    }


    @Override
    public UserDetailDto createUser(UserCreationDto userCreationDto) {
        LOGGER.trace("Creating user with email {}", userCreationDto.getEmail());

        JpaRepository<ApplicationUser, Long> repo = (JpaRepository<ApplicationUser, Long>) repositoryMap.get(userCreationDto.getType());

        if (userCreationDto == null) {
            throw new IllegalArgumentException("userCreationDto is null");
        }

        if (userCreationDto.getEmail() == null || userCreationDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("email is blank");
        }

        if (repo == null) {
            throw new IllegalArgumentException("Unknown equipment type: " + userCreationDto.getType());
        }

        ApplicationUser user = userCreationDto.toEntity();

        user.setPassword(passwordEncoder.encode(userCreationDto.getPassword()));

        UserDetailDto created = mapper.entityToDto(repo.save(user));

        return created;
    }


    @Override
    public UserDetailDto updateUser(Long id, UserUpdateDto updateDto) {

        LOGGER.info("Updating user with id {}", id);

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        if (id < 0) {
            throw new IllegalArgumentException("id is negative");
        }

        ApplicationUser existingUser = userRepository.findById(id)
            .orElseThrow(() ->
                new NotFoundException("User with ID " + id + " was not found.")
            );

        mapper.updateEntityFromDto(updateDto, existingUser);


        if (updateDto.getPassword() != null && !updateDto.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        ApplicationUser savedUser = userRepository.save(existingUser);

        return mapper.entityToDto(savedUser);
    }
}
