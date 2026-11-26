package cz.gastro.controller;

import cz.gastro.auth.CurrentUser;
import cz.gastro.model.Availability;
import cz.gastro.model.MealPlan;
import cz.gastro.model.PlanLine;
import cz.gastro.model.Shortage;
import cz.gastro.dto.PlanRequest;
import java.util.List;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cz.gastro.service.MealPlanningService;

@RestController
@RequestMapping("/api")
public class MealPlanningController {
    private final MealPlanningService mealPlanningService;
    private final CurrentUser currentUser;

    public MealPlanningController(
            MealPlanningService mealPlanningService,
            CurrentUser currentUser
    ) {
        this.mealPlanningService = mealPlanningService;
        this.currentUser = currentUser;
    }

    @GetMapping("/availability")
    public List<Availability> availability(@RequestParam LocalDate day) {
        currentUser.requireUser();
        return mealPlanningService.availability(day);
    }

    @GetMapping("/plans")
    public List<MealPlan> plans() {
        currentUser.requireUser();
        return mealPlanningService.plans();
    }

    @PostMapping("/plans")
    public int save(@RequestBody PlanRequest request) {
        currentUser.requireUser();
        return mealPlanningService.save(request);
    }

    @GetMapping("/plans/{id}/items")
    public List<PlanLine> items(@PathVariable int id) {
        currentUser.requireUser();
        return mealPlanningService.items(id);
    }

    @GetMapping("/plans/{id}/shopping")
    public List<Shortage> shoppingList(@PathVariable int id) {
        currentUser.requireUser();
        return mealPlanningService.shoppingList(id);
    }

    @PostMapping("/plans/{id}/prepare")
    public void prepare(@PathVariable int id) {
        currentUser.requireUser();
        mealPlanningService.prepare(id);
    }
}
