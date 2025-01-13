package PublicTransferRepositories.example.PublicTransferRepositories.controllers;

import PublicTransferRepositories.example.PublicTransferRepositories.dto.Project;
import PublicTransferRepositories.example.PublicTransferRepositories.services.GitHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
@Tag(name = "Страница локального диска", description = "Методы для работы с локальным диском")
public class DiskPageController {

    private final GitHubService gitHubService;
    private final String localRepositoriesPath;


    public DiskPageController(GitHubService gitHubService, @Value("${localRepositoriesPath}") String localRepositoriesPath) {
        this.gitHubService = gitHubService;
        this.localRepositoriesPath = localRepositoriesPath;
    }

    @Operation(summary = "Вывод всех репозиториев на диске")
    @GetMapping("/disk")
    public String disk(Model model) {
        repositoriesOnGithubAvailability(model);
        return "index";
    }

    protected ArrayList<Project> findAllRepositories() {
        ArrayList<Project> repositories = new ArrayList<>();

        File folder = new File(localRepositoriesPath.substring(0, localRepositoriesPath.length() - 1));

        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            Project project = new Project();
            project.setName(file.getName());
            repositories.add(project);
        }

        ArrayList<Project> sortedProjects = new ArrayList<>(repositories.stream()
                .sorted(Comparator.comparing(Project::getName))
                .collect(Collectors.toList()));

        return sortedProjects;
    }

    @Operation(summary = "Загрузка репозитория с локального диска на github")
    @PostMapping("/downloadFromDiskToGithub/{filename}")
    public String downloadFromDiskToGithub(@PathVariable @Parameter(description = "Название репозитория") String filename, Model model, RedirectAttributes redirectAttributes) {
        try {
            String status = gitHubService.downloadFromDiskToGithub(filename);
            repositoriesOnGithubAvailability(model);
            redirectAttributes.addFlashAttribute("message", "Репозиторий " + filename + " успешно " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке репозитория" + filename);
        }
        return "redirect:/disk";
    }

    @Operation(summary = "Загрузка всех репозиториев с локального диска на github")
    @PostMapping("/downloadFromDiskToGithubAllRepositories")
    public String downloadFromDiskToGithubAllRepositories(Model model, RedirectAttributes redirectAttributes) {
        try {
            ArrayList<Project> repositories = findAllRepositories();
            gitHubService.downloadFromDiskToGithubAllRepositories(repositories);
            repositoriesOnGithubAvailability(model);
            redirectAttributes.addFlashAttribute("message", "Все репозитории успешно загружены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке всех репозиториев");
        }
        return "redirect:/disk";
    }

    private void repositoriesOnGithubAvailability(Model model) {
        ArrayList<Project> diskRepositories = findAllRepositories();
        ArrayList<Project> gitHubRepositories = gitHubService.getAllRepositories();
        for (Project project : diskRepositories){
            Project pr = new Project();
            pr.setName(project.getName().replaceAll(" ", "_").toLowerCase());
            project.setSaveOnGithub(gitHubRepositories.contains(pr));
        }
        model.addAttribute("allRepositoriesFromDisk", diskRepositories);
    }
}
