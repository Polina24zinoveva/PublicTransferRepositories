package PublicTransferRepositories.example.PublicTransferRepositories.controllers;

import PublicTransferRepositories.example.PublicTransferRepositories.dto.Project;
import PublicTransferRepositories.example.PublicTransferRepositories.services.GitHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;

@Controller
@Tag(name = "Страница gitHub", description = "Методы для работы с gitHub")
public class GitHubPageController {

    private final GitHubService gitHubService;

    public GitHubPageController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Operation(summary = "Вывод всех репозиториев из gitHub")
    @GetMapping("/github")
    public String gitHubPage(Model model) {
        ArrayList<Project> repositories = gitHubService.getAllRepositories();
        model.addAttribute("allRepositoriesFromGithub", repositories);
        return "index";
    }


}
