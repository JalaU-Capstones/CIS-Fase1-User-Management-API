package com.cis.api.repository;

import com.cis.api.model.MongoUser;
import com.cis.api.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AdapterTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MySqlUserRepositoryAdapter mySqlAdapter;

    @Mock
    private MongoUserSpringRepository mongoSpringRepository;

    @InjectMocks
    private MongoUserRepositoryAdapter mongoAdapter;

    @Test
    void mySqlAdapter_findAll_ShouldCallRepository() {
        given(userRepository.findAll()).willReturn(List.of(new User()));
        List<User> result = mySqlAdapter.findAll();
        assertThat(result).hasSize(1);
        then(userRepository).should().findAll();
    }

    @Test
    void mySqlAdapter_findById_ShouldCallRepository() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "name", "login", "pass");
        given(userRepository.findById(id)).willReturn(Optional.of(user));
        Optional<User> result = mySqlAdapter.findById(id);
        assertThat(result).isPresent();
        then(userRepository).should().findById(id);
    }

    @Test
    void mySqlAdapter_findByLogin_ShouldCallRepository() {
        given(userRepository.findByLogin("login")).willReturn(Optional.of(new User()));
        Optional<User> result = mySqlAdapter.findByLogin("login");
        assertThat(result).isPresent();
        then(userRepository).should().findByLogin("login");
    }

    @Test
    void mySqlAdapter_save_ShouldCallRepository() {
        User user = new User();
        given(userRepository.save(user)).willReturn(user);
        User result = mySqlAdapter.save(user);
        assertThat(result).isEqualTo(user);
        then(userRepository).should().save(user);
    }

    @Test
    void mySqlAdapter_existsByLogin_ShouldCallRepository() {
        given(userRepository.existsByLogin("login")).willReturn(true);
        boolean result = mySqlAdapter.existsByLogin("login");
        assertThat(result).isTrue();
        then(userRepository).should().existsByLogin("login");
    }

    @Test
    void mySqlAdapter_existsByLoginAndIdNot_ShouldCallRepository() {
        UUID id = UUID.randomUUID();
        given(userRepository.existsByLoginAndIdNot("login", id)).willReturn(true);
        boolean result = mySqlAdapter.existsByLoginAndIdNot("login", id);
        assertThat(result).isTrue();
        then(userRepository).should().existsByLoginAndIdNot("login", id);
    }

    @Test
    void mongoAdapter_findAll_ShouldCallSpringRepository() {
        MongoUser mongoUser = new MongoUser(UUID.randomUUID().toString(), "name", "login", "pass");
        given(mongoSpringRepository.findAll()).willReturn(List.of(mongoUser));
        List<User> result = mongoAdapter.findAll();
        assertThat(result).hasSize(1);
        then(mongoSpringRepository).should().findAll();
    }

    @Test
    void mongoAdapter_findById_ShouldCallSpringRepository() {
        UUID id = UUID.randomUUID();
        MongoUser mongoUser = new MongoUser(id.toString(), "name", "login", "pass");
        given(mongoSpringRepository.findById(id.toString())).willReturn(Optional.of(mongoUser));
        Optional<User> result = mongoAdapter.findById(id);
        assertThat(result).isPresent();
        then(mongoSpringRepository).should().findById(id.toString());
    }

    @Test
    void mongoAdapter_findByLogin_ShouldCallSpringRepository() {
        MongoUser mongoUser = new MongoUser(UUID.randomUUID().toString(), "name", "login", "pass");
        given(mongoSpringRepository.findByLogin("login")).willReturn(Optional.of(mongoUser));
        Optional<User> result = mongoAdapter.findByLogin("login");
        assertThat(result).isPresent();
        then(mongoSpringRepository).should().findByLogin("login");
    }

    @Test
    void mongoAdapter_save_ShouldCallSpringRepository() {
        User user = new User(UUID.randomUUID(), "name", "login", "pass");
        MongoUser mongoUser = new MongoUser(user.getId().toString(), user.getName(), user.getLogin(), user.getPassword());
        given(mongoSpringRepository.save(any(MongoUser.class))).willReturn(mongoUser);
        
        User result = mongoAdapter.save(user);
        
        assertThat(result.getLogin()).isEqualTo(user.getLogin());
        then(mongoSpringRepository).should().save(any(MongoUser.class));
    }

    @Test
    void mongoAdapter_existsByLogin_ShouldCallSpringRepository() {
        given(mongoSpringRepository.existsByLogin("login")).willReturn(true);
        boolean result = mongoAdapter.existsByLogin("login");
        assertThat(result).isTrue();
        then(mongoSpringRepository).should().existsByLogin("login");
    }

    @Test
    void mongoAdapter_existsByLoginAndIdNot_ShouldCallSpringRepository() {
        UUID id = UUID.randomUUID();
        given(mongoSpringRepository.existsByLoginAndIdNot("login", id.toString())).willReturn(true);
        boolean result = mongoAdapter.existsByLoginAndIdNot("login", id);
        assertThat(result).isTrue();
        then(mongoSpringRepository).should().existsByLoginAndIdNot("login", id.toString());
    }

    @Test
    void mySqlAdapter_deleteUserAndRelatedData_ShouldCallRepositoryMethods() {
        UUID id = UUID.randomUUID();
        mySqlAdapter.deleteUserAndRelatedData(id);
        then(userRepository).should().deleteUserByIdNative(id.toString());
    }

    @Test
    void mongoAdapter_deleteUserAndRelatedData_ShouldCallSpringRepository() {
        UUID id = UUID.randomUUID();
        mongoAdapter.deleteUserAndRelatedData(id);
        then(mongoSpringRepository).should().deleteById(id.toString());
    }
}
