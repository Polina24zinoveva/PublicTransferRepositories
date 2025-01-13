package PublicTransferRepositories.example.PublicTransferRepositories.controllers;

import PublicTransferRepositories.example.PublicTransferRepositories.dto.Project;
import PublicTransferRepositories.example.PublicTransferRepositories.services.GitLabService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;

@Controller
@Tag(name = "Страница gitLab", description = "Методы для работы с gitLab")
public class GitLabPageController {

    private final GitLabService gitLabService;
    private final DiskPageController diskPageController;

    public GitLabPageController(GitLabService gitLabService, DiskPageController diskPageController) {
        this.gitLabService = gitLabService;
        this.diskPageController = diskPageController;
    }

    @GetMapping("/")
    public String start(Model model) {
        return "redirect:/gitlab";
    }

    @Operation(summary = "Вывод всех репозиториев из gitLab")
    @GetMapping("/gitlab")
    public String getAllRepositories(Model model) {
        repositoriesOnDiskAvailability(model);
        return "index";
    }

    @Operation(summary = "Скачивание репозитория с gitLab на локальный диск")
    @PostMapping("/downloadFromGitlabToDisk/{repositoryName}")
    public String downloadFromGitlabToDisk(@PathVariable @Parameter(description = "Название репозитория") String repositoryName, Model model, RedirectAttributes redirectAttributes) throws GitAPIException {
        try {
            String status = gitLabService.downloadFromGitlabToDisk(repositoryName);
            repositoriesOnDiskAvailability(model);
            redirectAttributes.addFlashAttribute("message", "Репозиторий " + repositoryName + " успешно " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при скачивании репозитория" + repositoryName);
        }
        return "redirect:/gitlab";
    }

    @Operation(summary = "Скачивание всех репозиториев с gitLab на локальный диск")
    @PostMapping("/downloadFromGitlabToDiskAllRepositories")
    public String downloadFromGitlabToDiskAllRepositories(Model model, RedirectAttributes redirectAttributes) throws GitAPIException {
        try {
            ArrayList<Project> repositories = gitLabService.getAllProjects();
            gitLabService.downloadFromGitlabToDiskAllRepositories(repositories);
            repositoriesOnDiskAvailability(model);
            redirectAttributes.addFlashAttribute("message", "Все репозитории успешно скачаны");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при скачивании всех репозиториев");
        }
        return "redirect:/gitlab";
    }

    private void repositoriesOnDiskAvailability(Model model) {
        ArrayList<Project> gitLabRepositories = gitLabService.getAllProjects();
        ArrayList<Project> diskRepositories = diskPageController.findAllRepositories();
        for (Project project : gitLabRepositories){
            project.setSaveOnDisk(diskRepositories.contains(project));
        }
        model.addAttribute("allRepositoriesFromGitlab", gitLabRepositories);
    }
}
