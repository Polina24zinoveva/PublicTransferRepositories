package PublicTransferRepositories.example.PublicTransferRepositories.dto;

import java.util.Objects;

public class Project {
    private String name;
    private boolean saveOnDisk;
    private boolean saveOnGithub;

    public boolean isSaveOnGithub() {
        return saveOnGithub;
    }

    public void setSaveOnGithub(boolean saveOnGithub) {
        this.saveOnGithub = saveOnGithub;
    }

    public String getName() {
        return name;
    }

    public boolean isSaveOnDisk() {
        return saveOnDisk;
    }

    public void setSaveOnDisk(boolean saveOnDisk) {
        this.saveOnDisk = saveOnDisk;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Project{" +
                "name='" + name + '\'' +
                "saveOnDisk='" + saveOnDisk + '\'' +
                "saveOnGithub='" + saveOnGithub + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(name, project.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
