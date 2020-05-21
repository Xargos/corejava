package mavenplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

import static mavenplugin.TimestampFileService.createTimeStampFile;

@Mojo(name = "post-inc", defaultPhase = LifecyclePhase.INSTALL)
public class PostIncrementalMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.compileSourceRoots}", readonly = true, required = true)
    private List<String> compileSourceRoots;

    public void execute() {
        long start = System.currentTimeMillis();
        createTimeStampFile(project.getBasedir().toPath());
        long total = System.currentTimeMillis() - start;
        info(String.format("Total time %s ms", total));
    }

    private void info(String template, Object... args) {
        getLog().info(String.format(template, args));
    }
}
