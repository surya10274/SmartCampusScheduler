package com.campus.scheduler.service;

import com.campus.scheduler.exception.EntityNotFoundException;
import com.campus.scheduler.exception.ValidationException;
import com.campus.scheduler.model.User;
import com.campus.scheduler.util.FileStorageUtil;
import com.campus.scheduler.util.IdGenerator;
import com.campus.scheduler.util.Logger;
import com.campus.scheduler.util.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * Module: User Management.
 * Manages the registry of students, faculty, and admins who can organise
 * events or make bookings. Persists to data/users.txt.
 */
public class UserService {

    private static final String FILE_NAME = "users.txt";
    private final List<User> users;

    public UserService() {
        this.users = FileStorageUtil.loadAll(FILE_NAME, User::fromRecord);
        int highest = users.stream()
                .map(u -> u.getUserId().replaceAll("[^0-9]", ""))
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .max().orElse(0);
        IdGenerator.primeUserCounter(highest);
        Logger.info("UserService initialised with " + users.size() + " user(s).");
    }

    public User registerUser(String name, String email, User.Role role) throws ValidationException {
        Validator.requireNonBlank(name, "Name");
        Validator.requireValidEmail(email);
        boolean duplicate = users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        if (duplicate) {
            throw new ValidationException("A user with email " + email + " is already registered.");
        }
        User user = new User(IdGenerator.nextUserId(), name, email, role);
        users.add(user);
        persist();
        Logger.info("Registered user " + user.getUserId() + " (" + name + ")");
        return user;
    }

    public User getById(String userId) throws EntityNotFoundException {
        return users.stream()
                .filter(u -> u.getUserId().equalsIgnoreCase(userId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    public List<User> getAll() {
        return new ArrayList<>(users);
    }

    public void deleteUser(String userId) throws EntityNotFoundException {
        User user = getById(userId);
        users.remove(user);
        persist();
        Logger.info("Deleted user " + userId);
    }

    private void persist() {
        FileStorageUtil.saveAll(FILE_NAME, users, User::toRecord);
    }
}
