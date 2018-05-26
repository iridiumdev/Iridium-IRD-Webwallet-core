package cash.ird.webwallet.server.rest.controller;

import cash.ird.webwallet.server.domain.User;
import cash.ird.webwallet.server.exception.UserRegistrationException;
import cash.ird.webwallet.server.repository.UserRepository;
import cash.ird.webwallet.server.rest.model.UserDto;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = UserDto.class)})
    public Mono<UserDto> register(@RequestBody UserDto userDto) {

        return Mono.<User>create(sink -> {
            if (!userRepository.findByUsername(userDto.getUsername()).isPresent()){
                User user = userRepository.save(
                        User.of(
                                userDto.getUsername(),
                                passwordEncoder.encode(userDto.getPassword())
                        )
                );
                sink.success(user);
            }else {
                sink.error(new UserRegistrationException("User already registered!"));
            }
        }).map(user -> new UserDto(user.getUsername()));

    }

}
