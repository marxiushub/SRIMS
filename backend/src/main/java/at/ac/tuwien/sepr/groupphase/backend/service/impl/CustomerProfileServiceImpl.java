package at.ac.tuwien.sepr.groupphase.backend.service.impl;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.CustomerProfileMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.CustomerProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Implementation of {@link CustomUserDetailService} for handling customer profile-related operations.
 * TODO: change Id to token
 */
@Service
public class CustomerProfileServiceImpl implements CustomerProfileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CustomerProfileRepository customerProfileRepository;
    private final CustomerRepository customerRepository;
    private final CustomerProfileMapper customerProfileMapper;

    public CustomerProfileServiceImpl(CustomerProfileRepository customerProfileRepository,
                                      CustomerRepository customerRepository,
                                      CustomerProfileMapper mapper) {
        this.customerProfileRepository = customerProfileRepository;
        this.customerRepository = customerRepository;
        this.customerProfileMapper = mapper;
    }

    @Override
    public CustomerProfileDetailDto createCustomerProfile(CustomerProfileCreationDto dto) {
        LOGGER.trace("Creating customer profile for customer with id {}", dto.getCustomerId());

        Customer customer = customerRepository.findById(dto.getCustomerId()).orElseThrow(() -> new NotFoundException("Customer with ID " + dto.getCustomerId() + " was not found."));

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
    public List<CustomerProfileDetailDto> getCustomerProfiles(Long customerId) {
        LOGGER.trace("Get customer profiles for customer with id {}", customerId);

        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null.");
        }
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer with ID " + customerId + " was not found.");
        }
        return customerProfileRepository.findByCustomerId(customerId).stream().map(customerProfileMapper::entityToDetailDto).toList();
    }

    @Override
    public void deleteCustomerProfile(Long customerProfileId) {
        LOGGER.trace("Deleting customer profile with id {}", customerProfileId);

        if (customerProfileId == null) {
            throw new IllegalArgumentException("Customer profile id is null");
        }
        if (!customerProfileRepository.existsById(customerProfileId)) {
            throw new NotFoundException("Customer profile with id " + customerProfileId + " was not found.");
        }

        customerProfileRepository.deleteById(customerProfileId);
    }
}
