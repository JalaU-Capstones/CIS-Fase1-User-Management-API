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
    void mongoAdapter_findAll_ShouldCallSpringRepository() {
        MongoUser mongoUser = new MongoUser(UUID.randomUUID(), "name", "login", "pass");
        given(mongoSpringRepository.findAll()).willReturn(List.of(mongoUser));
        List<User> result = mongoAdapter.findAll();
        assertThat(result).hasSize(1);
        then(mongoSpringRepository).should().findAll();
    }

    @Test
    void mongoAdapter_save_ShouldCallSpringRepository() {
        User user = new User(UUID.randomUUID(), "name", "login", "pass");
        MongoUser mongoUser = new MongoUser(user.getId(), user.getName(), user.getLogin(), user.getPassword());
        given(mongoSpringRepository.save(any(MongoUser.class))).willReturn(mongoUser);
        
        User result = mongoAdapter.save(user);
        
        assertThat(result.getLogin()).isEqualTo(user.getLogin());
        then(mongoSpringRepository).should().save(any(MongoUser.class));
    }

    @Test
    void mySqlAdapter_deleteUserAndRelatedData_ShouldCallRepositoryMethods() {
        UUID id = UUID.randomUUID();
        mySqlAdapter.deleteUserAndRelatedData(id);
        then(userRepository).should().deleteUserByIdNative(id.toString());
    }
}
