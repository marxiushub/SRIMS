package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.UserCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.UserDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.UserMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {

    /**
     * Find a user in the context of Spring Security based on the email address.
     * <br>
     * For more information have a look at this tutorial:
     * https://www.baeldung.com/spring-security-authentication-with-a-database
     *
     * @param email the email address
     * @return a Spring Security user
     * @throws UsernameNotFoundException is thrown if the specified user does not exists
     */
    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

    /**
     * Find an application user based on the email address.
     *
     * @param email the email address
     * @return a application user
     */
    ApplicationUser findApplicationUserByEmail(String email);

    /**
     * Log in a user.
     *
     * @param userLoginDto login credentials
     * @return the JWT, if successful
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are bad
     */
    String login(UserLoginDto userLoginDto);


    /**
     * Creates a new application user (Customer or Staff) based on the given DTO.
     * <br>
     * The concrete user type is determined by the subtype of {@link UserCreationDto}
     *
     * @param userCreationDto the DTO containing user creation data
     * @return the persisted ApplicationUser entity
     * @throws IllegalArgumentException if the user type is unknown or invalid
     * @throws org.springframework.dao.DataIntegrityViolationException if a user with the same email already exists
     */
    UserDetailDto createUser(UserCreationDto userCreationDto);


    /**
     * Updates an existing application user (Customer or Staff) based on the provided DTO.
     * <br>
     * The concrete update logic is delegated to the {@link UserMapper}, which maps the fields from the
     * corresponding subtype of {@link UserUpdateDto} onto the existing {@link ApplicationUser} entity.
     * <br>
     * The password is not mapped by the mapper and is instead handled separately in this method,
     * where it is securely encoded before being persisted.
     *
     * @param id the ID of the user to update
     * @param userUpdateDto the DTO containing updated user data
     * @return the updated user as a {@link UserDetailDto}
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException if no user with the given ID exists
     * @throws IllegalArgumentException if the provided DTO is null or invalid
     */
    public UserDetailDto updateUser(Long id, UserUpdateDto userUpdateDto);


    /**
     * Deletes an application user (Customer or Staff) by its unique identifier.
     * <br>
     * If the user does not exist, no deletion is performed and an exception is thrown.
     *
     * @param userId the unique identifier of the user to be deleted
     * @throws jakarta.persistence.EntityNotFoundException if no user with the given id exists
     * @throws org.springframework.dao.DataIntegrityViolationException if the user cannot be deleted due to existing references or constraints
     */
    void deleteUserById(Long userId);
}
