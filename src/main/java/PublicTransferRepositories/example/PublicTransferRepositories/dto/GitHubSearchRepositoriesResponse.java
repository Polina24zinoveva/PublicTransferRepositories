package PublicTransferRepositories.example.PublicTransferRepositories.dto;

import java.util.List;

public class GitHubSearchRepositoriesResponse {
    private List<Project> items;

    public List<Project> getItems() {
        return items;
    }

    public void setItems(List<Project> items) {
        this.items = items;
    }
}
