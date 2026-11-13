package com.visura.service;

import com.visura.database.entity.LocalCredential;
import com.visura.database.entity.User;
import com.visura.database.repository.LocalCredentialRepository;
import com.visura.exception.custom.CredentialException;
import com.visura.exception.enums.problem.CredentialProblem;
import com.visura.mapper.LocalCredentialMapper;
import com.visura.vo.Password;
import com.visura.vo.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalCredentialService {

    private final LocalCredentialRepository localCredentialRepository;
    private final LocalCredentialMapper localCredentialMapper;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public PasswordHash hash(Password password) {
        return PasswordHash.of(encode(password));
    }

    @Transactional
    public void save(User user, PasswordHash hash) {
        localCredentialRepository.save(create(user, hash));
    }

    public LocalCredential verify(UUID user, Password password) {
        LocalCredential credential = find(user);
        matched(credential, password);
        return credential;
    }

    public PasswordHash rehash(LocalCredential credential, Password password) {
        unmatched(credential, password);
        return hash(password);
    }

    @Transactional
    public void replace(LocalCredential credential, PasswordHash hash) {
        localCredentialRepository.change(credential.getId(), credential.getPasswordHash().value(), hash.value());
    }

    @Transactional
    public void reset(UUID user, PasswordHash hash) {
        LocalCredential credential = find(user);
        replace(credential, hash);
    }

    public void upgrade(UUID user, PasswordHash hash) {
        LocalCredential credential = find(user);
        swap(credential, hash);
    }

    private String encode(Password password) {
        return passwordEncoder.encode(password.value());
    }

    private LocalCredential create(User user, PasswordHash hash) {
        return localCredentialMapper.entity(user, hash, Instant.now(clock));
    }

    private LocalCredential find(UUID user) {
        return localCredentialRepository.findByUserUuid(user)
                .orElseThrow(() -> new CredentialException(CredentialProblem.NOT_FOUND));
    }

    private void matched(LocalCredential credential, Password password) {
        if (!current(credential, password)) {
            throw new CredentialException(CredentialProblem.INCORRECT_CURRENT_PASSWORD);
        }
    }

    private void unmatched(LocalCredential credential, Password password) {
        if (current(credential, password)) {
            throw new CredentialException(CredentialProblem.SAME_PASSWORD);
        }
    }

    private void swap(LocalCredential credential, PasswordHash hash) {
        try {
            replace(credential, hash);
        } catch (DataIntegrityViolationException exception) {
            // outra troca de senha gravou primeiro; o hash dela é o que vale
        }
    }

    private boolean current(LocalCredential credential, Password password) {
        return passwordEncoder.matches(password.value(), credential.getPasswordHash().value());
    }
}
