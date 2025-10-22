package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.util.PaginationUtil;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto createUser(NewUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("User with email '%s' already exists".formatted(request.getEmail()));
        }
        User saved = userRepository.save(UserMapper.fromNewUser(request));
        return UserMapper.toDto(saved);
    }

    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        if (ids != null && !ids.isEmpty()) {
            return userRepository.findAllById(ids).stream()
                    .sorted(Comparator.comparing(User::getId))
                    .map(UserMapper::toDto)
                    .toList();
        }
        Pageable pageable = PaginationUtil.offsetPageable(from, size);
        return userRepository.findAll(pageable).stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteUser(long userId) {
        boolean exists = userRepository.existsById(userId);
        if (!exists) {
            throw new NotFoundException("User with id=%d was not found".formatted(userId));
        }
        userRepository.deleteById(userId);
    }
}
