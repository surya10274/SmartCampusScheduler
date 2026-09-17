package com.campus.scheduler.service;

import com.campus.scheduler.exception.EntityNotFoundException;
import com.campus.scheduler.exception.ValidationException;
import com.campus.scheduler.model.Resource;
import com.campus.scheduler.util.FileStorageUtil;
import com.campus.scheduler.util.IdGenerator;
import com.campus.scheduler.util.Logger;
import com.campus.scheduler.util.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Module 1: Resource Management.
 * Handles CRUD operations for campus resources (rooms, labs, equipment, etc.)
 * and persists them to data/resources.txt.
 */
public class ResourceService {

    private static final String FILE_NAME = "resources.txt";
    private final List<Resource> resources;

    public ResourceService() {
        this.resources = FileStorageUtil.loadAll(FILE_NAME, Resource::fromRecord);
        int highest = resources.stream()
                .map(r -> r.getResourceId().replaceAll("[^0-9]", ""))
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .max().orElse(0);
        IdGenerator.primeResourceCounter(highest);
        Logger.info("ResourceService initialised with " + resources.size() + " resource(s).");
    }

    public Resource addResource(String name, Resource.ResourceType type, int capacity, String location)
            throws ValidationException {
        Validator.requireNonBlank(name, "Resource name");
        Validator.requirePositive(capacity, "Capacity");
        Validator.requireNonBlank(location, "Location");
        Resource resource = new Resource(IdGenerator.nextResourceId(), name, type, capacity, location);
        resources.add(resource);
        persist();
        Logger.info("Added resource " + resource.getResourceId() + " (" + name + ")");
        return resource;
    }

    public Resource getById(String resourceId) throws EntityNotFoundException {
        return resources.stream()
                .filter(r -> r.getResourceId().equalsIgnoreCase(resourceId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Resource not found: " + resourceId));
    }

    public List<Resource> getAll() {
        return new ArrayList<>(resources);
    }

    public List<Resource> findAvailableByType(Resource.ResourceType type, int minCapacity) {
        List<Resource> matches = new ArrayList<>();
        for (Resource r : resources) {
            if (r.getType() == type
                    && r.getCapacity() >= minCapacity
                    && r.getStatus() == Resource.ResourceStatus.AVAILABLE) {
                matches.add(r);
            }
        }
        return matches;
    }

    public Resource updateResource(String resourceId, String name, Integer capacity, String location,
                                    Resource.ResourceStatus status) throws EntityNotFoundException, ValidationException {
        Resource resource = getById(resourceId);
        if (name != null) {
            Validator.requireNonBlank(name, "Resource name");
            resource.setName(name);
        }
        if (capacity != null) {
            Validator.requirePositive(capacity, "Capacity");
            resource.setCapacity(capacity);
        }
        if (location != null) {
            resource.setLocation(location);
        }
        if (status != null) {
            resource.setStatus(status);
        }
        persist();
        Logger.info("Updated resource " + resourceId);
        return resource;
    }

    public void deleteResource(String resourceId) throws EntityNotFoundException {
        Resource resource = getById(resourceId);
        resources.remove(resource);
        persist();
        Logger.info("Deleted resource " + resourceId);
    }

    public Optional<Resource> findByName(String name) {
        return resources.stream().filter(r -> r.getName().equalsIgnoreCase(name)).findFirst();
    }

    private void persist() {
        FileStorageUtil.saveAll(FILE_NAME, resources, Resource::toRecord);
    }
}
