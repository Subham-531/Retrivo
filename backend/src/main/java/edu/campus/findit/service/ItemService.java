package edu.campus.findit.service;

import edu.campus.findit.dto.request.ItemCreateRequest;
import edu.campus.findit.dto.request.ItemUpdateRequest;
import edu.campus.findit.dto.response.ItemResponse;
import edu.campus.findit.exception.BadRequestException;
import edu.campus.findit.exception.ResourceNotFoundException;
import edu.campus.findit.exception.UnauthorizedException;
import edu.campus.findit.model.Category;
import edu.campus.findit.model.Item;
import edu.campus.findit.model.ItemHistory;
import edu.campus.findit.model.User;
import edu.campus.findit.model.enums.ItemStatus;
import edu.campus.findit.model.enums.ReportType;
import edu.campus.findit.model.enums.Role;
import edu.campus.findit.repository.CategoryRepository;
import edu.campus.findit.repository.ItemHistoryRepository;
import edu.campus.findit.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final ItemHistoryRepository historyRepository;
    private final MatchingService matchingService;

    public ItemService(ItemRepository itemRepository,
                       CategoryRepository categoryRepository,
                       ItemHistoryRepository historyRepository,
                       MatchingService matchingService) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.historyRepository = historyRepository;
        this.matchingService = matchingService;
    }

    @Transactional
    public ItemResponse createItem(ItemCreateRequest req, User currentUser) {
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + req.getCategoryId()));

        Item item = new Item();
        item.setUser(currentUser);
        item.setCategory(category);
        item.setItemName(req.getItemName().trim());
        item.setColour(req.getColour().trim());
        item.setBrand(req.getBrand() != null ? req.getBrand().trim() : null);
        item.setDescription(req.getDescription().trim());
        item.setLocation(req.getLocation().trim());
        item.setEventDate(req.getEventDate());
        item.setReportType(req.getReportType());
        item.setStatus(ItemStatus.ACTIVE);
        item.setImagePath(req.getImagePath());
        item.setIdentifyingDetails(req.getIdentifyingDetails() != null ? req.getIdentifyingDetails().trim() : null);

        Item savedItem = itemRepository.save(item);

        // Record creation in ItemHistory
        ItemHistory history = new ItemHistory(
                savedItem, currentUser, "NONE", "ACTIVE",
                "Item report submitted as " + req.getReportType()
        );
        historyRepository.save(history);

        // Trigger smart matching algorithm
        matchingService.matchItemAgainstOpposites(savedItem);

        return ItemResponse.fromEntity(savedItem, true);
    }

    @Transactional
    public ItemResponse updateItem(Long id, ItemUpdateRequest req, User currentUser) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + id));

        // Ownership or Admin check
        boolean isOwner = item.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to update this item report");
        }

        ItemStatus previousStatus = item.getStatus();

        if (req.getItemName() != null) item.setItemName(req.getItemName().trim());
        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + req.getCategoryId()));
            item.setCategory(category);
        }
        if (req.getColour() != null) item.setColour(req.getColour().trim());
        if (req.getBrand() != null) item.setBrand(req.getBrand().trim());
        if (req.getDescription() != null) item.setDescription(req.getDescription().trim());
        if (req.getLocation() != null) item.setLocation(req.getLocation().trim());
        if (req.getEventDate() != null) item.setEventDate(req.getEventDate());
        if (req.getImagePath() != null) item.setImagePath(req.getImagePath());
        if (req.getIdentifyingDetails() != null) item.setIdentifyingDetails(req.getIdentifyingDetails().trim());

        if (req.getStatus() != null && req.getStatus() != previousStatus) {
            item.setStatus(req.getStatus());
            ItemHistory history = new ItemHistory(
                    item, currentUser, previousStatus.name(), req.getStatus().name(),
                    "Status updated by " + (isAdmin ? "administrator" : "reporter")
            );
            historyRepository.save(history);
        }

        Item saved = itemRepository.save(item);

        // Recompute matches if active
        if (saved.getStatus() == ItemStatus.ACTIVE) {
            matchingService.matchItemAgainstOpposites(saved);
        }

        return ItemResponse.fromEntity(saved, true);
    }

    @Transactional
    public void deleteItem(Long id, User currentUser) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + id));

        boolean isOwner = item.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to delete this item report");
        }

        itemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id, User currentUser) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + id));

        boolean canSeeSensitive = (currentUser != null) &&
                (currentUser.getId().equals(item.getUser().getId()) || currentUser.getRole() == Role.ADMIN);

        return ItemResponse.fromEntity(item, canSeeSensitive);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> browseItems(String query,
                                          ReportType reportType,
                                          Integer categoryId,
                                          String colour,
                                          String location,
                                          ItemStatus status,
                                          Pageable pageable) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String cleanColour = (colour != null && !colour.trim().isEmpty()) ? colour.trim() : null;
        String cleanLoc = (location != null && !location.trim().isEmpty()) ? location.trim() : null;

        return itemRepository.searchAndFilter(
                cleanQuery, reportType, categoryId, cleanColour, cleanLoc, status, pageable
        ).map(item -> ItemResponse.fromEntity(item, false));
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getMyItems(User currentUser) {
        return itemRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(item -> ItemResponse.fromEntity(item, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getRecentItems() {
        return itemRepository.findTop6ByStatusOrderByCreatedAtDesc(ItemStatus.ACTIVE)
                .stream()
                .map(item -> ItemResponse.fromEntity(item, false))
                .collect(Collectors.toList());
    }
}

