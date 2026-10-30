package edu.campus.findit.controller;

import edu.campus.findit.dto.request.ItemCreateRequest;
import edu.campus.findit.dto.request.ItemUpdateRequest;
import edu.campus.findit.dto.response.ApiResponse;
import edu.campus.findit.dto.response.ItemResponse;
import edu.campus.findit.exception.BadRequestException;
import edu.campus.findit.model.User;
import edu.campus.findit.model.enums.ItemStatus;
import edu.campus.findit.model.enums.ReportType;
import edu.campus.findit.service.AuthService;
import edu.campus.findit.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final AuthService authService;

    @Value("${findit.upload.dir:./uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    public ItemController(ItemService itemService, AuthService authService) {
        this.itemService = itemService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponse>> createItem(@Valid @RequestBody ItemCreateRequest req) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        ItemResponse item = itemService.createItem(req, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Item report submitted successfully", item));
    }

    @GetMapping("/browse")
    public ResponseEntity<ApiResponse<Page<ItemResponse>>> browseItems(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ReportType reportType,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String colour,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ItemResponse> results = itemService.browseItems(query, reportType, categoryId, colour, location, status, pageable);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getRecentItems() {
        return ResponseEntity.ok(ApiResponse.ok(itemService.getRecentItems()));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getMyItems() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        return ResponseEntity.ok(ApiResponse.ok(itemService.getMyItems(currentUser)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemById(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        return ResponseEntity.ok(ApiResponse.ok(itemService.getItemById(id, currentUser)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemUpdateRequest req
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        ItemResponse updated = itemService.updateItem(id, req, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Item updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        itemService.deleteItem(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Item report deleted successfully", null));
    }

    @PostMapping("/upload-image")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds 5MB limit");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BadRequestException("Invalid filename format");
        }

        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BadRequestException("Invalid image file type. Allowed formats: JPG, PNG, WEBP");
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate safe server-side filename using random UUID
            String safeFileName = UUID.randomUUID().toString() + "." + ext;
            Path targetLocation = uploadPath.resolve(safeFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok(ApiResponse.ok("Image uploaded successfully", safeFileName));
        } catch (IOException e) {
            throw new BadRequestException("Could not store file: " + e.getMessage());
        }
    }
}

