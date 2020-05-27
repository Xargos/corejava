package mavenplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;

@Mojo(name = "post-inc", defaultPhase = LifecyclePhase.INSTALL)
public class PostIncrementalMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.compileSourceRoots}", readonly = true, required = true)
    private List<String> compileSourceRoots;

    private final TimestampFileService timestampFileService = TimestampFileService.instance(getLog());

    public void execute() {
        long start = System.currentTimeMillis();
        timestampFileService.createTimeStampFile(project.getName(), project.getBasedir().toPath());
        long total = System.currentTimeMillis() - start;
        info(String.format("Total time %s ms", total));
    }

    private void info(String template, Object... args) {
        getLog().info(String.format(template, args));
    }
}
