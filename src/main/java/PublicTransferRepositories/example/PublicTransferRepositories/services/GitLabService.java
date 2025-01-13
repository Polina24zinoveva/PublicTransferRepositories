package PublicTransferRepositories.example.PublicTransferRepositories.services;

import PublicTransferRepositories.example.PublicTransferRepositories.dto.Project;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class GitLabService {

    private static final String GITLAB_BRANCHES_STARTS_WITH = "refs/remotes/gitlab/";
    private static final String LOCAL_BRANCHES_STARTS_WITH = "refs/heads/";
    private static final Logger LOG = Logger.getLogger(GitLabService.class.getName());

    private final RestTemplate restTemplate;
    private final String gitLabToken;
    private final String gitLabUrl;
    private final String gitlabIdUser;
    private final String gitlabUrlUser;
    private final String localRepositoriesPath;

    public GitLabService(RestTemplate restTemplate,
                         @Value("${gitlab.token}") String gitLabToken,
                         @Value("${gitlab.url}") String gitLabUrl,
                         @Value("${gitlab.idUser}") String gitlabIdUser,
                         @Value("${gitlab.urlUser}") String gitlabUrlUser,
                         @Value("${localRepositoriesPath}") String localRepositoriesPath) {
        this.restTemplate = restTemplate;
        this.gitLabToken = gitLabToken;
        this.gitLabUrl = gitLabUrl;
        this.gitlabIdUser = gitlabIdUser;
        this.gitlabUrlUser = gitlabUrlUser;
        this.localRepositoriesPath = localRepositoriesPath;
    }


    public ArrayList<Project> getAllProjects() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("PRIVATE-TOKEN", gitLabToken);

        HttpEntity<ArrayList<Project>> entity = new HttpEntity<>(headers);
        ResponseEntity<List<Project>> response = restTemplate.exchange(gitLabUrl + "/users/" + gitlabIdUser + "/projects",
                HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
                });

        List<Project> projects = response.getBody();

        if (projects == null || projects.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<Project> sortedProjects = new ArrayList<>(projects.stream()
                .sorted(Comparator.comparing(Project::getName))
                .collect(Collectors.toList()));

        return sortedProjects;
    }

    public String downloadFromGitlabToDisk(String name) throws GitAPIException {
        String urlName = name.replaceAll(" ", "-").toLowerCase();
        String remoteUrl = gitlabUrlUser + urlName + ".git";
        File localPath = new File(localRepositoriesPath + name);

        if (!localPath.exists()) {
            Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(localPath)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitLabToken, gitLabToken))
                    .setRemote("gitlab")
                    .call();
            LOG.info("Репозиторий успешно клонирован в " + localPath.getAbsolutePath());
            return "скачан";
        } else {
            updateBranchesFromGitlabToDisk(localPath);
            return "обновлен";
        }
    }

    public void updateBranchesFromGitlabToDisk(File localPath) {
        try{
            Git git = Git.open(localPath);
            List<RemoteConfig> remotes = git.remoteList().call();
            var remote = remotes.stream()
                    .filter(it -> it.getName().equals("gitlab"))
                    .findFirst()
                    .orElseThrow();
            // Скачивание обновлений в gitlabBranches
            FetchResult fetchResult = git.fetch()
                    .setRemote(remote.getName())
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitLabToken, gitLabToken))
                    .setRefSpecs(remote.getFetchRefSpecs())
                    .setRemoveDeletedRefs(true)
                    .call();

            // Проверка на добавление и удаление веток
            List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            List<String> gitlabBranches = branches.stream()
                    .filter(branch -> branch.getName().startsWith(GITLAB_BRANCHES_STARTS_WITH))
                    .map(branch -> branch.getName().replace(GITLAB_BRANCHES_STARTS_WITH, ""))
                    .toList();
            List<String> gitlabBranchesForCheck = new ArrayList<>(gitlabBranches);

            List<String> localBranches = branches.stream()
                    .filter(branch -> branch.getName().startsWith(LOCAL_BRANCHES_STARTS_WITH))
                    .map(branch -> branch.getName().replace(LOCAL_BRANCHES_STARTS_WITH, ""))
                    .toList();
            List<String> localBranchesForCheck = new ArrayList<>(localBranches);

            gitlabBranchesForCheck.removeAll(localBranches);
            if (!gitlabBranchesForCheck.isEmpty()){
                for (String branch : gitlabBranchesForCheck){
                    git.checkout().setCreateBranch(true).setName(branch).setStartPoint(GITLAB_BRANCHES_STARTS_WITH + branch).call();
                    LOG.info("Добавлена ветка: " + branch);
                }
            }
            localBranchesForCheck.removeAll(gitlabBranches);
            if (!localBranchesForCheck.isEmpty()){
                for (String branch : localBranchesForCheck){
                    git.checkout().setName("master").call();
                    git.branchDelete().setBranchNames(branch).setForce(true).call();
                    LOG.info("Удалена ветка: " + branch);
                }
            }

            Collection<TrackingRefUpdate> updatedBranches = fetchResult.getTrackingRefUpdates();
            if (!updatedBranches.isEmpty()) {
                LOG.info("Обновленные ветки:");
                for (TrackingRefUpdate updatedBranch : updatedBranches) {
                    LOG.info(updatedBranch.getLocalName());
                }

                branches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

                // Скачивание обновлений в localBranches
                for (Ref branch : branches) {
                    String branchName = branch.getName();
                    if (branchName.startsWith(LOCAL_BRANCHES_STARTS_WITH)) {
                        git.reset().setMode(ResetCommand.ResetType.HARD).call();

                        git.checkout().setName(branchName.replace(LOCAL_BRANCHES_STARTS_WITH, "")).call();

                        PullCommand pullCommand = git.pull();
                        pullCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitLabToken, gitLabToken));
                        pullCommand.call();
                        LOG.info("Выполнен pull для ветки: " + branchName);
                    }
                }
            } else {
                LOG.info("Нет обновлений в ветках в репозитории " + localPath);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void downloadFromGitlabToDiskAllRepositories(ArrayList<Project> repositories) throws GitAPIException {
        for (Project repository : repositories) {
            downloadFromGitlabToDisk(repository.getName());
        }
    }
}
