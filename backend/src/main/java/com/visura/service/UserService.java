package com.visura.service;

import com.visura.database.entity.Individual;
import com.visura.database.entity.User;
import com.visura.database.repository.UserRepository;
import com.visura.exception.custom.UserException;
import com.visura.exception.enums.problem.UserProblem;
import com.visura.mapper.UserMapper;
import com.visura.vo.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public User save(Individual person, Email email) {
        return userRepository.save(create(person, email));
    }

    @Transactional(readOnly = true)
    public User find(UUID user) {
        return existing(user);
    }

    private User create(Individual person, Email email) {
        return userMapper.entity(person, email);
    }

    private User existing(UUID user) {
        return userRepository.findWithPersonByUuid(user)
                .orElseThrow(() -> new UserException(UserProblem.NOT_FOUND));
    }
}
