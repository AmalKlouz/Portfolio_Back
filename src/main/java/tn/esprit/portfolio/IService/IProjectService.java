package tn.esprit.portfolio.IService;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.portfolio.Entity.Project;

import java.io.IOException;
import java.util.List;

public interface IProjectService {



    Project createProject(Project project);

    List<Project> getAllProjects();

    Project getProjectById(Long id);

    Project updateProject(Long id, Project updatedProject);

    void deleteProject(Long id);
}
