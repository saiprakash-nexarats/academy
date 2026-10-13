package com.nexarats.academy.academic.web;

import com.nexarats.academy.academic.dto.ProgramCategoryDto;
import com.nexarats.academy.academic.service.ProgramCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public REST Controller for Program Categories.
 * Supports canonical /api/v1/academic/program-categories and alias /api/v1/program-categories.
 */
@RestController
@RequestMapping({"/api/v1/academic/program-categories", "/api/v1/program-categories"})
public class ProgramCategoryController {

    private final ProgramCatalogService catalogService;

    public ProgramCategoryController(ProgramCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ResponseEntity<List<ProgramCategoryDto>> listCategories() {
        List<ProgramCategoryDto> categories = catalogService.getCategories();
        return ResponseEntity.ok(categories);
    }
}
