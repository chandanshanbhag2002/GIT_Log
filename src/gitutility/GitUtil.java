/*
 * Author:Chandan Shanbhag
 * 
 * 
 */
package gitutility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 *
 * @author chandan.shanbhag
 */
public class GitUtil {

    public String PLATFORM_REPO;
    public File LOCAL_REPO;
    public String USER_NAME;
    public String PASSWORD;
    BufferedWriter output = null;

    public UsernamePasswordCredentialsProvider setCredentials() {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException e) {
             System.out.println(e.toString());
        }

        UsernamePasswordCredentialsProvider cp = null;
        try {
            cp = new UsernamePasswordCredentialsProvider(
                    USER_NAME, PASSWORD);

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return cp;
    }

    public boolean createOrUpdateRepo() throws IOException, GitAPIException {

        if (LOCAL_REPO.exists()) {
            System.out.println("Pulling Latest Remote Copy.......");
            FetchResult fetch = Git.open(LOCAL_REPO).fetch()
                    .setCredentialsProvider(setCredentials()).call();
            return true;

        } else {
            System.out.println("Clonning Repo......");
            Git git = Git.cloneRepository().setURI(PLATFORM_REPO)
                    .setCredentialsProvider(setCredentials())
                    .setDirectory(LOCAL_REPO).setCloneAllBranches(true).call();
            return true;

        }

    }

    public List<String> getBranches() throws IOException, GitAPIException {
        createOrUpdateRepo();
        Git git = Git.open(LOCAL_REPO);
        Collection<Ref> r = git.lsRemote().setHeads(true).setTags(true)
                .setCredentialsProvider(setCredentials()).call();
        System.out.println();
        List<String> tags = new ArrayList<String>();
        for (Ref reference : r) {
            String temp = reference.getName().toString();
            System.out.println(temp.substring(temp.lastIndexOf("/") + 1));
            String[] one = temp.split("/");
            tags.add(one[2]);
        }
        git.close();
        System.out.println(tags);
        return tags;
    }

    public void getBranchLog(String branchname) throws IOException, GitAPIException {
        createOrUpdateRepo();
        BufferedWriter output = null;
        Git git = Git.open(LOCAL_REPO);
        //output = new BufferedWriter(new FileWriter("GitLog" + GetProperties.rand + ".txt"));
        Repository repo = new FileRepository(LOCAL_REPO + "/.git");

        Iterable<RevCommit> logs = git.log()
                .not(repo.resolve("master"))
                .not(repo.resolve("head"))
                .add(repo.resolve("remotes/origin/" + branchname)).call();

        for (Iterator<RevCommit> iterator = logs.iterator(); iterator.hasNext();) {
            RevCommit rev = iterator.next();
            System.out.println(rev.getFullMessage());
            output.write(rev.getFullMessage());
        }

        git.close();
        output.close();
    }

    public void checkoutbranch(String branchname) throws IOException, GitAPIException {
        Git git = Git.open(LOCAL_REPO);
        git.reset().setMode(ResetCommand.ResetType.HARD).call();
        createOrUpdateRepo();
        try {
            git.checkout().setCreateBranch(true).setName(branchname)
                    .setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
                    .setStartPoint("origin/" + branchname).call();
            System.out.println(branchname + " branch is checked out");
        } catch (RefAlreadyExistsException r) {
            git.checkout().setName(branchname).call();
            System.out.println(branchname + " branch is checked out");
        } catch (RefNotFoundException e) {
            System.out.println(branchname + " is not valid branch");
        }

    }

    public List getlogbetweenrange(String branch, String from, String to) throws IOException, GitAPIException {
        StringBuilder sb = new StringBuilder();
        Git git = Git.open(LOCAL_REPO);
        git.reset().setMode(ResetCommand.ResetType.HARD).call();
        createOrUpdateRepo();
        checkoutbranch(branch);
        Repository r = new FileRepository(LOCAL_REPO + "/.git");
        Iterable<RevCommit> log = git.log().addRange(r.resolve(from), r.resolve(to)).call();

        for (RevCommit rev : log) {
            System.out.println(rev.getFullMessage());
            sb.append(rev.getFullMessage());
        }
        String s = sb.toString();
        ParseVcsLog p = new ParseVcsLog();
        return p.getJiraList(s);

    }

//    public static void main(String args[]) throws IOException, GitAPIException {
//        GitUtil g = new GitUtil();
//       // String s=g.getlogbetweenrange("Patch5","a4b8e696db0523885f65aa94f14a27fc701c7864","edabb7297c46b9eef2d29c195a6a7f9926bd9bc8");
//        ParseVcsLog p=new ParseVcsLog();
//       // p.getJiraList(s);
//        //g.createOrUpdateRepo();
//        //g.checkoutbranch("Patch5");
//        //g.getlogbetweenrange("Patch5");
//        // g.checkoutbranch("nextrelease");
//        
//        
//
//    }
// Create a trust manager that does not validate certificate chains
    TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }
        }
    };

// Install the all-trusting trust manager
}
