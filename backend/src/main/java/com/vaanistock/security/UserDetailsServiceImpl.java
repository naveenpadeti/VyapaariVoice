package com.vaanistock.security;

import com.vaanistock.business.Business;
import com.vaanistock.business.BusinessRepository;
import com.vaanistock.user.User;
import com.vaanistock.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, BusinessRepository businessRepository) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrMobile(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + username));

        Long businessId = businessRepository.findFirstByUserId(user.getId())
                .map(Business::getId)
                .orElse(null);

        return UserPrincipal.create(user, businessId);
    }

    public UserPrincipal loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        Long businessId = businessRepository.findFirstByUserId(user.getId())
                .map(Business::getId)
                .orElse(null);

        return UserPrincipal.create(user, businessId);
    }
}
