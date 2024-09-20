package com.example.securedoc.unit;

import com.example.securedoc.cache.CacheStore;
import com.example.securedoc.entity.ConfirmationEntity;
import com.example.securedoc.entity.CredentialEntity;
import com.example.securedoc.entity.RoleEntity;
import com.example.securedoc.entity.UserEntity;
import com.example.securedoc.event.UserEvent;
import com.example.securedoc.exception.entity.RoleEntityNotFoundException;
import com.example.securedoc.repository.ConfirmationRepository;
import com.example.securedoc.repository.CredentialRepository;
import com.example.securedoc.repository.RoleRepository;
import com.example.securedoc.repository.UserRepository;
import com.example.securedoc.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private ConfirmationRepository confirmationRepository;
    @Mock
    private CredentialRepository credentialRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private CacheStore<String, Integer> userLoginCache;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void whenCreateUser_thenControlFlowAsExpected() {
        doReturn(null).when(this.userRepository).save(any(UserEntity.class));
        doReturn(null).when(this.credentialRepository).save(any(CredentialEntity.class));
        doReturn(null).when(this.confirmationRepository).save(any(ConfirmationEntity.class));
        when(this.roleRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(new RoleEntity("USER", null)));
        doNothing().when(this.publisher).publishEvent(any(UserEvent.class));

        var firstName = "John";
        var lastName = "Doe";
        var email = "john.doe@example.com";
        var password = "password";

        userService.createUser(firstName, lastName, email, password);

        verify(this.userRepository, times(1)).save(any(UserEntity.class));
        verify(this.credentialRepository, times(1)).save(any(CredentialEntity.class));
        verify(this.confirmationRepository, times(1)).save(any(ConfirmationEntity.class));
        verify(this.publisher, times(1)).publishEvent(any(UserEvent.class));
    }

    @Test
    void testEncode_shouldEncodePassword() {
        var password = "password";

        var encodedPassword = encoder.encode(password);
        var bcryptStartEncoded = encodedPassword.substring(0, 7);

        assertEquals("$2a$10$", bcryptStartEncoded);
    }

    @Test
    void testGetRoleName_whenRoleExists_thenReturnRoleEntity() {
        var role = "ROLE";
        var roleEntity = new RoleEntity(role, null);
        when(this.roleRepository.findByNameIgnoreCase(role)).thenReturn(Optional.of(roleEntity));

        var roleFromDb = userService.getRoleName(role);

        verify(this.roleRepository, times(1)).findByNameIgnoreCase(role);
        assertEquals(roleFromDb, roleEntity);
    }

    @Test
    void testGetRoleName_whenRoleDoesNotExist_thenThrowException() {
        var fakeRole = "FAKE ROLE";
        when(this.roleRepository.findByNameIgnoreCase(fakeRole)).thenReturn(Optional.empty());

        assertThrows(RoleEntityNotFoundException.class, () -> userService.getRoleName(fakeRole));
    }
}