import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class GumTreeRunner {

    public static void main(String[] args) throws IOException {

        if(args.length != 1){
            System.out.println("Invalid args");
            System.exit(1);
        }
        analyzeRepo(args[0]);
    }

    public static int runGumTree(ITree srcTree, ITree dstTree){
        //Run.initGenerators();
        Matcher m = Matchers.getInstance().getMatcher(srcTree, dstTree); // retrieve the default matcher
        return getEditscriptSize(m, srcTree, dstTree);
    }

    private static int getEditscriptSize(Matcher m, ITree srcTree, ITree dstTree) {
        m.match();
        ActionGenerator g = new ActionGenerator(srcTree, dstTree, m.getMappings());
        g.generate();
        List<Action> actions = g.getActions(); // return the actions
        return actions.size();
    }

    public static void analyzeRepo(String repositoryPath) throws IOException {

        Statistics stats = new Statistics();
        int currentCommit = 0;
        long startTime = System.currentTimeMillis();
        Repository repository = GitHelper.openRepository(repositoryPath);
        Collection<RevCommit> commits = GitHelper.getCommits(repository, "HEAD");
        int i = 0;
        for (RevCommit commit : commits) {
            currentCommit++;
            System.out.println("Currently at commit " + currentCommit + " from " + commits.size() + " commits in total.");
            if(commit.getParents().length>1){
                System.out.println("Merge commit ignored!");
                continue;
            }
            else if(commit.getParents().length<=0){
                System.out.println("First commit ignored!");
                continue;
            }
            List<DiffEntry> diffs = GitHelper.getDiffs(repository, commit, commit.getParent(0));
            for (DiffEntry diff : diffs) {

                try {
                    String srcString = GitHelper.getFileContent(diff.getOldId(), repository);
                    String dstString = GitHelper.getFileContent(diff.getNewId(), repository);

                    ITree srcTreeJdt = new JdtTreeGenerator().generateFromString(srcString).getRoot();
                    ITree dstTreeJdt = new JdtTreeGenerator().generateFromString(dstString).getRoot();

                    //ITree srcTreeCD = new CdJdtTreeGenerator().generateFromString(srcString).getRoot();
                    //ITree dstTreeCD = new CdJdtTreeGenerator().generateFromString(dstString).getRoot();

                    System.out.println(repositoryPath + " : " + commit.getName() + " : " + diff.getOldPath());
                    int gumTreeScore = runGumTree(srcTreeJdt, dstTreeJdt);
                    System.gc();
                    System.out.println("(JDT) GumTree: " + gumTreeScore);
                    double rtedScore = RTEDCalculator.caclulateRTEDValue(srcTreeJdt, dstTreeJdt);
                    System.gc();
                    System.out.println("(JDT) RTED: " + rtedScore);
                    int changeDistillerScore = runChangeDistiller(srcTreeJdt, dstTreeJdt);
                    System.gc();
                    System.out.println("(JDT) ChangeDistiller: " + changeDistillerScore);
                    //System.out.println("(CD) ChangeDistiller: " + runChangeDistiller(srcTreeCD, dstTreeCD));
                    //System.out.println("(CD) GumTree: " + runGumTree(srcTreeCD, dstTreeCD));

                    stats.addEntry(gumTreeScore, rtedScore, changeDistillerScore);
                    System.out.println("Took: " + (System.currentTimeMillis() - startTime) + " ms");
                    i++;

                    if (i % 50 == 0) {
                        stats.printStats();
                    }
                }
                catch (Exception e){
                    System.out.println("Something somewhere went wrong. Ooopsi!");
                    e.printStackTrace();
                }
            }

        }
        stats.printStats();

    }

    private static int runChangeDistiller(ITree srcTree, ITree dstTree) {
        Matcher m = new CompositeMatchers.ChangeDistiller(srcTree,dstTree, new MappingStore());
        return getEditscriptSize(m, srcTree, dstTree);

    }


}
