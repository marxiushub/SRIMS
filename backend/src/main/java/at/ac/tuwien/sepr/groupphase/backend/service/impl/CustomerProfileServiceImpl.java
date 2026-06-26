package at.ac.tuwien.sepr.groupphase.backend.service.impl;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.CustomerProfileMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ReservationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.CurrentUserService;
import at.ac.tuwien.sepr.groupphase.backend.service.CustomerProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Implementation of {@link CustomUserDetailService} for handling customer profile-related operations.
 */
@Service
public class CustomerProfileServiceImpl implements CustomerProfileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CustomerProfileRepository customerProfileRepository;
    private final CustomerRepository customerRepository;
    private final CustomerProfileMapper customerProfileMapper;
    private final CustomerProfileValidator customerProfileValidator;
    private final CurrentUserService currentUserService;
    private final ReservationRepository reservationRepository;

    public CustomerProfileServiceImpl(CustomerProfileRepository customerProfileRepository,
                                      CustomerRepository customerRepository,
                                      CustomerProfileMapper mapper,
                                      CustomerProfileValidator customerProfileValidator,
                                      CurrentUserService currentUserService,
                                      ReservationRepository reservationRepository) {
        this.customerProfileRepository = customerProfileRepository;
        this.customerRepository = customerRepository;
        this.customerProfileMapper = mapper;
        this.customerProfileValidator = customerProfileValidator;
        this.currentUserService = currentUserService;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public CustomerProfileDetailDto createCustomerProfile(CustomerProfileCreationDto dto) {
        Long customerId = currentUserService.getUserId();

        LOGGER.trace("Creating customer profile for customer with id {}", customerId);

        customerProfileValidator.validateCreationDto(dto);

        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new NotFoundException("Customer with ID " + customerId + " was not found."));

        String profileName = dto.getProfileName().trim();

        if (customerProfileRepository.existsByCustomerIdAndProfileName(customerId, profileName)) {
            throw new ValidationException("A profile with the name " + profileName + " already exists", "Ein Profil names " + profileName + " existiert schon");
        }

        CustomerProfile customerProfile = new CustomerProfile(
            profileName,
            dto.getHeight(),
            dto.getWeight(),
            dto.getShoeSize(),
            dto.getSkillLevel(),
            customer
        );

        CustomerProfile savedCustomerProfile = customerProfileRepository.save(customerProfile);
        return customerProfileMapper.entityToDetailDto(savedCustomerProfile);
    }

    @Override
    public List<CustomerProfileDetailDto> getCustomerProfiles() {

        Long customerId = currentUserService.getUserId();

        LOGGER.trace("Get customer profiles for customer with id {}", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException(
                "Customer with ID " + customerId + " was not found."
            );
        }

        return customerProfileRepository.findByCustomerId(customerId)
            .stream()
            .map(customerProfileMapper::entityToDetailDto)
            .toList();
    }

    @Override
    public List<CustomerProfileDetailDto> getCustomerProfiles(Long customerId) {
        LOGGER.trace("Get customer profiles for customer with id {}", customerId);

        validateProfileId(customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer with ID " + customerId + " was not found.");
        }

        return customerProfileRepository.findByCustomerId(customerId).stream().map(customerProfileMapper::entityToDetailDto).toList();
    }

    @Override
    public CustomerProfileDetailDto getCustomerProfileById(Long profileId) {
        LOGGER.trace("Get customer profile with id {}", profileId);

        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null.");
        }

        CustomerProfile profile = customerProfileRepository.findById(profileId)
            .orElseThrow(() ->
                new NotFoundException(
                    "CustomerProfile with ID " + profileId + " was not found."
                ));

        Long currentUserId = currentUserService.getUserId();

        boolean isStaff = currentUserService.hasAuthority("STAFF");

        if (!isStaff && !profile.getCustomer().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You have no permission to access this profile.");
        }

        return customerProfileMapper.entityToDetailDto(profile);
    }

    @Override
    public CustomerProfileDetailDto updateCustomerProfile(Long customerProfileId, CustomerProfileUpdateDto dto) {
        LOGGER.trace("Updating customer profile for customer with id {}", customerProfileId);

        customerProfileValidator.validateUpdateDto(dto);

        Long currentUserId = currentUserService.getUserId();
        CustomerProfile profile = checkUserAccessPermission(customerProfileId, currentUserId);

        if (dto.getProfileName() != null) {

            String newName = dto.getProfileName().trim();

            boolean nameAlreadyExists = customerProfileRepository.existsByCustomerIdAndProfileName(currentUserId, newName);

            if (nameAlreadyExists) {
                throw new ValidationException("A profile with the name " + newName + " already exists", "Ein Profil names" + newName + "existiert schon");
            }
        }

        if (dto.getProfileName() != null) {
            profile.setProfileName(dto.getProfileName().trim());
        }

        if (dto.getHeight() != null) {
            profile.setHeight(dto.getHeight());
        }

        if (dto.getWeight() != null) {
            profile.setWeight(dto.getWeight());
        }

        if (dto.getShoeSize() != null) {
            profile.setShoeSize(dto.getShoeSize());
        }

        if (dto.getSkillLevel() != null) {
            profile.setSkillLevel(dto.getSkillLevel());
        }

        return customerProfileMapper.entityToDetailDto(
            customerProfileRepository.save(profile)
        );
    }

    @Override
    public void deleteCustomerProfile(Long customerProfileId) {
        LOGGER.trace("Deleting customer profile with id {}", customerProfileId);

        Long currentUserId = currentUserService.getUserId();
        CustomerProfile profile = checkUserAccessPermission(customerProfileId, currentUserId);

        if (reservationRepository.existsByCustomerProfileId(profile.getId())) {
            throw new ValidationException("Cannot delete customer profile with existing reservations.",
                "Kunden-Profile, die mit Reservierungen verbunden sind, können nicht gelöscht werden");
        }

        customerProfileRepository.delete(profile);
    }


    //Helper Methods
    //Checks whether a given CustomerProfileID belongs to a given CustomerID
    private CustomerProfile checkUserAccessPermission(Long profileId, Long currentCustomerId) {

        validateProfileId(profileId);

        CustomerProfile profile = customerProfileRepository.findById(profileId)
            .orElseThrow(() ->
                new NotFoundException("Customer profile with ID " + profileId + " was not found.")
            );

        if (!profile.getCustomer().getId().equals(currentCustomerId)) {
            throw new AccessDeniedException("You have no permission to perform this action.");
        }

        return profile;
    }

    //Checks if a given CustomerProfileID is valid
    public void validateProfileId(Long profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("Customer profile ID is null");
        }

        if (profileId <= 0) {
            throw new IllegalArgumentException("Customer Profile ID must be positive");
        }
    }
}
