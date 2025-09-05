package com.NomadsKey.security;
import com.NomadsKey.dto.LoginDto;
import com.NomadsKey.dto.SignUpRequestDto;
import com.NomadsKey.dto.UserDto;
import com.NomadsKey.entity.User;
import com.NomadsKey.entity.enums.Role;
import com.NomadsKey.exception.ResourceNotFoundException;
import com.NomadsKey.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public UserDto signup(SignUpRequestDto signUpRequestDto) {
        User existingUser = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);

        if (existingUser != null) {
            throw new RuntimeException("User is already present with same email id");
        }

        User newUser = modelMapper.map(signUpRequestDto, User.class);
        if (signUpRequestDto.getRole() == Role.ADMIN) {
            throw new RuntimeException("You cannot register as ADMIN directly");
        }
        if (signUpRequestDto.getRole() == Role.HOTEL_MANAGER) {
            newUser.setRole(Set.of(Role.PENDING_HOTEL_MANAGER));
        } else {
            newUser.setRole(Set.of(signUpRequestDto.getRole()));
        }
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        newUser.setDateOfBirth(signUpRequestDto.getDateofbirth());
        newUser.setGender(signUpRequestDto.getGender());
        newUser = userRepository.save(newUser);

        return modelMapper.map(newUser, UserDto.class);
    }

    public String[] login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(), loginDto.getPassword()
        ));

        User user = (User) authentication.getPrincipal();

        String[] arr = new String[2];
        arr[0] = jwtService.generateAccessToken(user);
        arr[1] = jwtService.generateRefreshToken(user);

        return arr;
    }

    public String refreshToken(String refreshToken) {
        Long id = jwtService.getUserIdFromToken(refreshToken);

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: "+id));
        return jwtService.generateAccessToken(user);
    }
}
