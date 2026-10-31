package com.takima.backskeleton.Inventory.InventoryService;

import com.takima.backskeleton.Inventory.DAO.InventoryRepository;
import com.takima.backskeleton.Inventory.DAO.NotificationRepository;
import com.takima.backskeleton.Inventory.DTO.InventoryRequest;
import com.takima.backskeleton.Inventory.DTO.InventoryResponse;
import com.takima.backskeleton.Inventory.models.Inventory;
import com.takima.backskeleton.User.DAO.UserRepository;
import com.takima.backskeleton.User.models.User;
import com.takima.backskeleton.config.InventoryProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository repo;
    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;
    private final InventoryProperties props;

    public InventoryService(InventoryRepository repo, NotificationRepository notificationRepo,
                            UserRepository userRepo, InventoryProperties props) {
        this.repo = repo;
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
        this.props = props;
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> listForUser(UUID userId) {
        return repo.findAllByUserIdOrderByExpiryDateAsc(userId)
                   .stream().map(this::toResponse).toList();
    }

    @Transactional
    public InventoryResponse create(UUID userId, InventoryRequest req) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new AccessDeniedException("User not found"));

        Inventory inv = new Inventory(user, req.name(), req.quantity(), req.unit(), req.expiryDate());
        return toResponse(repo.save(inv));
    }

    @Transactional
    public InventoryResponse update(UUID userId, UUID id, InventoryRequest req) {
        Inventory inv = repo.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found"));
        inv.setName(req.name());
        inv.setQuantity(req.quantity());
        inv.setUnit(req.unit());
        inv.setExpiryDate(req.expiryDate());
        return toResponse(repo.save(inv));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Inventory inv = repo.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found"));
        // Pas de ON DELETE CASCADE en base : les notifications doivent partir avant l'article.
        notificationRepo.deleteByInventoryId(id);
        repo.delete(inv);
    }

    private InventoryResponse toResponse(Inventory i) {
        return new InventoryResponse(
            i.getId(), i.getName(), i.getQuantity(), i.getUnit(), i.getExpiryDate(),
            computeStatus(i.getExpiryDate()), i.getCreatedAt(), i.getUpdatedAt()
        );
    }

    private InventoryResponse.ExpiryStatus computeStatus(LocalDate expiry) {
        if (expiry == null) return InventoryResponse.ExpiryStatus.NO_DATE;
        LocalDate today = LocalDate.now();
        if (expiry.isBefore(today)) return InventoryResponse.ExpiryStatus.EXPIRED;
        if (!expiry.isAfter(today.plusDays(props.expiringSoonDays())))
            return InventoryResponse.ExpiryStatus.EXPIRING_SOON;
        return InventoryResponse.ExpiryStatus.OK;
    }
}