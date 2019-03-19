
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.util.io.NullOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GitHelper {

    public static String getFileContent(AbbreviatedObjectId objectId, Repository repository) throws IOException {
        return getFileContent(objectId, repository, "UTF-8");
    }

    public static String getFileContent(AbbreviatedObjectId objectId, Repository repository, String encoding) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(getFileStream(objectId, repository), writer, "UTF-8");
        return writer.toString();
    }

    public static InputStream getFileStream(AbbreviatedObjectId objectId, Repository repository) throws IOException {
        AnyObjectId anyObjectId = objectId.toObjectId();

        if (anyObjectId != null && !anyObjectId.equals(ObjectId.zeroId()))
            return repository.open(objectId.toObjectId()).openStream();

        return new NullInputStream(0);
    }

    public static Repository openRepository(String repoPath) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repository = builder
                .readEnvironment()
                .setGitDir(new File(repoPath + "/.git"))
                .build();

        return repository;
    }



    public static Collection<RevCommit> getCommits(Repository repository, String startCommit) throws IOException {
        RevWalk walk = new RevWalk(repository);

        // set start commit
        walk.markStart(walk.parseCommit(repository.resolve(startCommit)));

        // set filter
        walk.setRevFilter(new RevFilter() {
            @Override
            public boolean include(RevWalk walker, RevCommit cmit) throws StopWalkException, IOException {
                if (cmit.getParentCount() > 1)
                    // this is a merge commit
                    return false;

                return true;
            }

            @Override
            public RevFilter clone() {
                return null;
            }
        });

        // extract commits
        List<RevCommit> commits = new ArrayList<>();
        RevCommit commit;
        while ((commit = walk.next()) != null) {
            commits.add(commit);
        }

        return commits;
    }

    public static RevCommit getCommit(Repository repository, String commitHash) throws IOException {
        RevWalk walk = new RevWalk(repository);
        return walk.parseCommit(repository.resolve(commitHash));
    }

    public static List<DiffEntry> getDiffs(Repository repository, RevCommit commit, RevCommit parent) {
        DiffFormatter df = new DiffFormatter(NullOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setPathFilter(PathSuffixFilter.create(".java")); //TODO --> Put this in config file
        df.setDetectRenames(true);
        try {
            return df.scan(parent, commit.getTree());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(0);
    }
}
