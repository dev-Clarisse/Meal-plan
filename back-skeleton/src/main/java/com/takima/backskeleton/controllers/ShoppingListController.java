package com.takima.backskeleton.controllers;

import com.takima.backskeleton.DTO.ShoppingItemDto;
import com.takima.backskeleton.DTO.ShoppingListDto;
import com.takima.backskeleton.DTO.UpdateShoppingItemRequest;
import com.takima.backskeleton.services.CurrentUserService;
import com.takima.backskeleton.services.ShoppingListService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/v1")
@RestController
public class ShoppingListController {
    private final ShoppingListService shoppingListService;
    private final CurrentUserService currentUser;

    public ShoppingListController(ShoppingListService shoppingListService, CurrentUserService currentUser) {
        this.shoppingListService = shoppingListService;
        this.currentUser = currentUser;
    }

    @Operation(summary = "Génère (ou met à jour) la liste de courses du plan, sans doublon")
    @PostMapping("/meal-plans/{id}/shopping-list")
    public ShoppingListDto generate(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return shoppingListService.generate(id, currentUser.idOf(jwt.getSubject()));
    }

    @Operation(summary = "Récupère la liste de courses déjà générée du plan")
    @GetMapping("/meal-plans/{id}/shopping-list")
    public ShoppingListDto get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return shoppingListService.get(id, currentUser.idOf(jwt.getSubject()));
    }

    @Operation(summary = "Coche ou décoche un élément de la liste de courses")
    @PatchMapping("/shopping-items/{id}")
    public ShoppingItemDto setPurchased(@PathVariable Long id,
                                        @Valid @RequestBody UpdateShoppingItemRequest req,
                                        @AuthenticationPrincipal Jwt jwt) {
        return shoppingListService.setPurchased(id, currentUser.idOf(jwt.getSubject()), req.purchased());
    }
}