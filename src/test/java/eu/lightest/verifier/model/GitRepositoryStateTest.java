package eu.lightest.verifier.model;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class GitRepositoryStateTest {
    
    @Test
    public void getBuildVersion() throws IOException {
        GitRepositoryState gitRepositoryState = GitRepositoryState.get();
        assertNotNull(gitRepositoryState);
        
        String buildVersion = gitRepositoryState.getBuildVersion();
        assertNotNull(buildVersion);
        
        System.out.println("buildVersion: " + buildVersion);
    }
    
    @Test
    public void getBuildVersioShort() throws IOException {
        String buildVersion = GitRepositoryState.get().getBuildVersion();
        assertNotNull(buildVersion);
        
        System.out.println("buildVersion: " + buildVersion);
    }
}