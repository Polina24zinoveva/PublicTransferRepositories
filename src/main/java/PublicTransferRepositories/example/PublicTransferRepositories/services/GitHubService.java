package PublicTransferRepositories.example.PublicTransferRepositories.services;

import PublicTransferRepositories.example.PublicTransferRepositories.dto.GitHubSearchRepositoriesResponse;
import PublicTransferRepositories.example.PublicTransferRepositories.dto.Project;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;


@Service
public class GitHubService {
    private static final String GITHUB_BRANCHES_STARTS_WITH = "refs/remotes/github/";
    private static final String LOCAL_BRANCHES_STARTS_WITH = "refs/heads/";
    private static final String GITLAB_BRANCHES_STARTS_WITH = "refs/remotes/gitlab/";
    private static final Logger LOG = Logger.getLogger(GitHubService.class.getName());


    private final RestTemplate restTemplate;
    private final String gitHubToken;
    private final String githubUrl;
    private final String githubUrlUser;
    private final String githubUserName;
    private final String localRepositoriesPath;

    public GitHubService(RestTemplate restTemplate,
                         @Value("${github.token}") String gitHubToken,
                         @Value("${github.url}") String githubUrl,
                         @Value("${github.urlUser}") String githubUrlUser,
                         @Value("${github.userName}") String githubUserName,
                         @Value("${localRepositoriesPath}") String localRepositoriesPath) {
        this.restTemplate = restTemplate;
        this.gitHubToken = gitHubToken;
        this.githubUrl = githubUrl;
        this.githubUrlUser = githubUrlUser;
        this.githubUserName = githubUserName;
        this.localRepositoriesPath = localRepositoriesPath;
    }


    public ArrayList<Project> getAllRepositories()
    {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/vnd.github+json");
        headers.add("Authorization", "Bearer " + gitHubToken);
        headers.add("X-GitHub-Api-Version", "2022-11-28");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<GitHubSearchRepositoriesResponse> response = restTemplate
                .exchange(githubUrl + "/search/repositories?q=user:" + githubUserName,
                HttpMethod.GET,entity, new ParameterizedTypeReference<>() {});
        List<Project> projects = Objects.requireNonNull(response.getBody()).getItems();
        return new ArrayList<>(projects);
    }


    public String downloadFromDiskToGithub(String filename) {
        String urlName = filename.replaceAll(" ", "_").toLowerCase();

        if (!repositoryExists(urlName)) createRepository(urlName);
        else LOG.info("Репозиторий " + filename + " уже существует");


        File localPath = new File(localRepositoriesPath + filename);
        String remoteUrl = githubUrlUser + urlName + ".git";

        // Пуш всех веток из локального репозитория на GitHub
        try (Git git = Git.open(localPath)) {

            List<RemoteConfig> remotes = git.remoteList().call();
            boolean remoteGithubExists = remotes.stream()
                    .anyMatch(remoteConfig -> remoteConfig.getName().equals("github"));

            if (!remoteGithubExists){
                RemoteAddCommand remoteAddCommand = git.remoteAdd();
                remoteAddCommand.setName("github");
                remoteAddCommand.setUri(new URIish(remoteUrl));
                remoteAddCommand.call();
            }

            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(gitHubToken, gitHubToken);

            List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

            List<String> githubBranches = branches.stream()
                    .filter(branch -> branch.getName().startsWith(GITHUB_BRANCHES_STARTS_WITH))
                    .map(branch -> branch.getName().replace(GITHUB_BRANCHES_STARTS_WITH, ""))
                    .toList();

            List<Ref> gitlabBranches;

            if (githubBranches.isEmpty()){
                gitlabBranches = new ArrayList<>(branches.stream()
                        .filter(branch -> branch.getName().startsWith(GITLAB_BRANCHES_STARTS_WITH))
                        .toList());

                // Сортировка массива, чтобы master был первым и стал дефолтной веткой в Github
                Optional<Ref> masterBranchOptional = branches.stream()
                        .filter(branch -> branch.getName().startsWith(GITLAB_BRANCHES_STARTS_WITH + "master"))
                        .findFirst();

                List<Ref> mutableGitlabBranches = gitlabBranches;

                masterBranchOptional.ifPresent(masterBranch -> {
                    mutableGitlabBranches.remove(masterBranch);
                    mutableGitlabBranches.add(0, masterBranch);
                });
            }
            else {
                // Проверка на добавление и удаление веток
                List<String> localBranches = branches.stream()
                        .filter(branch -> branch.getName().startsWith(LOCAL_BRANCHES_STARTS_WITH))
                        .map(branch -> branch.getName().replace(LOCAL_BRANCHES_STARTS_WITH, ""))
                        .toList();

                List<String> githubBranchesForCheck = new ArrayList<>(githubBranches);
                List<String> localBranchesForCheck = new ArrayList<>(localBranches);

                localBranchesForCheck.removeAll(githubBranches);
                if (!localBranchesForCheck.isEmpty()){
                    for (String branch : localBranchesForCheck){
                        LOG.info("Добавлена ветка: " + branch);
                    }
                }
                githubBranchesForCheck.removeAll(localBranches);
                if (!githubBranchesForCheck.isEmpty()){
                    for (String branch : githubBranchesForCheck){
                        git.checkout().setName("master").call();

                        PushCommand pushCommand = git.push();
                        pushCommand.setRemote("github");
                        pushCommand.setCredentialsProvider(credentialsProvider);
                        pushCommand.setRefSpecs(new RefSpec(":" + LOCAL_BRANCHES_STARTS_WITH + branch)); // Указываем на удаление ветки
                        pushCommand.call();

                        LOG.info("Удалена ветка: " + branch);
                    }
                }
                branches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
                gitlabBranches = new ArrayList<>(branches.stream()
                        .filter(branch -> branch.getName().startsWith(GITLAB_BRANCHES_STARTS_WITH))
                        .toList());
            }

            // push изменений на github
            for (Ref branch : gitlabBranches) {
                String branchName = branch.getName();
                String localBranchName = branchName.substring(GITLAB_BRANCHES_STARTS_WITH.length());
                try {
                    git.checkout().setCreateBranch(true).setName(localBranchName).setStartPoint(branchName).call();
                    LOG.info("Создана и переключена на ветку: " + localBranchName);
                } catch (RefAlreadyExistsException e) {
                    git.checkout().setName(localBranchName).call();
                    LOG.info("Переключена на существующую ветку: " + localBranchName);
                }

                // Пуш ветки на GitHub
                PushCommand pushCommand = git.push();
                pushCommand.setRemote("github");
                pushCommand.setCredentialsProvider(credentialsProvider);
                pushCommand.setRefSpecs(new RefSpec(localBranchName + ":" + localBranchName));
                pushCommand.setForce(true);
                pushCommand.call();
            }
            LOG.info("Все ветки успешно импортированы на GitHub");
            if (remoteGithubExists) return "обновлен";
            else return "загружен";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean repositoryExists(String filename)
    {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", "application/vnd.github+json");
            headers.add("Authorization", "Bearer " + gitHubToken);
            headers.add("X-GitHub-Api-Version", "2022-11-28");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<GitHubSearchRepositoriesResponse> response = restTemplate.exchange(githubUrl + "/repos/" + githubUserName + "/" + filename,
                    HttpMethod.GET,entity, new ParameterizedTypeReference<>() {});
            return response.hasBody();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void createRepository(String filename)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/vnd.github+json");
        headers.add("Authorization", "Bearer " + gitHubToken);
        headers.add("X-GitHub-Api-Version", "2022-11-28");

        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("name", filename);
        jsonData.put("description", "");
        jsonData.put("homepage", "https://github.com");
        jsonData.put("private", true);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(jsonData, headers);

        try {
            restTemplate.postForEntity("https://api.github.com/user/repos",
                    entity, String.class);
            LOG.info("Репозиторий " + filename + " успешно создан на GitHub.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadFromDiskToGithubAllRepositories(Collection<Project> repositories) {
        for (Project repository : repositories) {
            downloadFromDiskToGithub(repository.getName());
        }
    }
}
