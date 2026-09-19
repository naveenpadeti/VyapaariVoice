package com.vaanistock.auth;

import com.vaanistock.business.Business;
import com.vaanistock.business.BusinessRepository;
import com.vaanistock.business.BusinessType;
import com.vaanistock.category.CategoryService;
import com.vaanistock.common.BusinessException;
import com.vaanistock.common.ResourceNotFoundException;
import com.vaanistock.security.JwtUtil;
import com.vaanistock.security.UserPrincipal;
import com.vaanistock.user.User;
import com.vaanistock.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final CategoryService categoryService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       BusinessRepository businessRepository,
                       CategoryService categoryService,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.categoryService = categoryService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BusinessException("Email is already registered: " + request.getEmail());
        }
        if (userRepository.existsByMobile(request.getMobile().trim())) {
            throw new BusinessException("Mobile number is already registered: " + request.getMobile());
        }

        User user = new User(
                request.getName().trim(),
                request.getMobile().trim(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getPreferredLanguage()
        );
        User savedUser = userRepository.save(user);

        Business business = new Business(
                savedUser.getId(),
                request.getBusinessName().trim(),
                request.getBusinessType() != null ? request.getBusinessType() : BusinessType.WHOLESALE,
                request.getLocation().trim()
        );
        Business savedBusiness = businessRepository.save(business);

        // Provision default categories
        categoryService.seedDefaultCategories(savedBusiness.getId());

        UserPrincipal principal = UserPrincipal.create(savedUser, savedBusiness.getId());
        String token = jwtUtil.generateToken(principal);

        return buildAuthResponse(token, savedUser, savedBusiness);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsername().trim().toLowerCase();
        User user = userRepository.findByEmailOrMobile(identifier, identifier)
                .orElseThrow(() -> new BadCredentialsException("Invalid mobile/email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid mobile/email or password");
        }

        Business business = businessRepository.findFirstByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No business found for user: " + user.getId()));

        UserPrincipal principal = UserPrincipal.create(user, business.getId());
        String token = jwtUtil.generateToken(principal);

        return buildAuthResponse(token, user, business);
    }

    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Business business = businessRepository.findFirstByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found for user: " + userId));

        UserPrincipal principal = UserPrincipal.create(user, business.getId());
        String token = jwtUtil.generateToken(principal);

        return buildAuthResponse(token, user, business);
    }

    @Transactional
    public AuthResponse updateProfile(Long userId, Long businessId, String name, String preferredLanguage, String businessName, String location) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with id: " + businessId));

        if (name != null && !name.isBlank()) user.setName(name.trim());
        if (preferredLanguage != null && !preferredLanguage.isBlank()) user.setPreferredLanguage(preferredLanguage.trim());
        if (businessName != null && !businessName.isBlank()) business.setBusinessName(businessName.trim());
        if (location != null && !location.isBlank()) business.setLocation(location.trim());

        userRepository.save(user);
        businessRepository.save(business);

        UserPrincipal principal = UserPrincipal.create(user, business.getId());
        String token = jwtUtil.generateToken(principal);

        return buildAuthResponse(token, user, business);
    }

    private AuthResponse buildAuthResponse(String token, User user, Business business) {
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
                user.getId(),
                user.getName(),
                user.getMobile(),
                user.getEmail(),
                user.getPreferredLanguage()
        );

        AuthResponse.BusinessDto businessDto = new AuthResponse.BusinessDto(
                business.getId(),
                business.getBusinessName(),
                business.getBusinessType(),
                business.getLocation()
        );

        return new AuthResponse(token, userDto, businessDto);
    }
}
