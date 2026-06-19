package at.ac.tuwien.sepr.groupphase.backend.service.impl;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.CustomerProfileMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
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

    public CustomerProfileServiceImpl(CustomerProfileRepository customerProfileRepository,
                                      CustomerRepository customerRepository,
                                      CustomerProfileMapper mapper,
                                      CustomerProfileValidator customerProfileValidator,
                                      CurrentUserService currentUserService) {
        this.customerProfileRepository = customerProfileRepository;
        this.customerRepository = customerRepository;
        this.customerProfileMapper = mapper;
        this.customerProfileValidator = customerProfileValidator;
        this.currentUserService = currentUserService;
    }

    @Override
    public CustomerProfileDetailDto createCustomerProfile(CustomerProfileCreationDto dto) {
        Long customerId = currentUserService.getUserId();

        LOGGER.trace("Creating customer profile for customer with id {}", customerId);

        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new NotFoundException("Customer with ID " + customerId + " was not found."));

        CustomerProfile customerProfile = new CustomerProfile(
            dto.getProfileName(),
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
    public CustomerProfileDetailDto updateCustomerProfile(Long customerProfileId, CustomerProfileUpdateDto dto) {
        LOGGER.trace("Updating customer profile for customer with id {}", customerProfileId);

        customerProfileValidator.validateUpdateDto(dto);

        Long currentUserId = currentUserService.getUserId();
        CustomerProfile profile = checkUserAccessPermission(customerProfileId, currentUserId);

        if (dto.getProfileName() != null) {
            profile.setProfileName(dto.getProfileName());
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
    public CustomerProfileDetailDto getCustomerProfileById(Long customerProfileId) {
        LOGGER.trace("Get customer profile by id {}", customerProfileId);

        Long currentUserId = currentUserService.getUserId();
        CustomerProfile profile = checkUserAccessPermission(customerProfileId, currentUserId);

        return customerProfileMapper.entityToDetailDto(profile);
    }

    @Override
    public void deleteCustomerProfile(Long customerProfileId) {
        LOGGER.trace("Deleting customer profile with id {}", customerProfileId);

        Long currentUserId = currentUserService.getUserId();
        CustomerProfile profile = checkUserAccessPermission(customerProfileId, currentUserId);

        customerProfileRepository.delete(profile);
    }


    //Helper Methods
    //Checks whether a given CustomerProfileID belongs to a given CustomerID
    private CustomerProfile checkUserAccessPermission(Long profileId, Long currentCustomerId) {

        if (profileId == null) {
            throw new IllegalArgumentException("Customer profile id is null.");
        }

        CustomerProfile profile = customerProfileRepository.findById(profileId)
            .orElseThrow(() ->
                new NotFoundException("Customer profile with ID " + profileId + " was not found.")
            );

        if (!profile.getCustomer().getId().equals(currentCustomerId)) {
            throw new AccessDeniedException("You have no permission to perform this action.");
        }

        return profile;
    }
}
