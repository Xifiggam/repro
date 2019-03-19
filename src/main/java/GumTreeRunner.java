import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.gen.jdt.AbstractJdtTreeGenerator;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.gen.jdt.cd.CdJdtTreeGenerator;
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

        String file1 = "inputfiles/GumTreeRunner.java";
        String file2 = "inputfiles/GumTreeRunner_dst.java";

        ITree srcTree = new JdtTreeGenerator().generateFromFile(file1).getRoot();
        ITree dstTree = new JdtTreeGenerator().generateFromFile(file2).getRoot();
        System.out.println("TED: " + RTEDCalculator.caclulateRTEDValue(srcTree, dstTree));

        //System.out.println(runGumTree(srcTree, dstTree));
        analyzeRepo("/home/phmoll/Documents/RTED/LIRE/", new JdtTreeGenerator());



    }

    public static int runGumTree(ITree srcTree, ITree dstTree){
        //Run.initGenerators();
        Matcher m = Matchers.getInstance().getMatcher(srcTree, dstTree); // retrieve the default matcher
        m.match();
        ActionGenerator g = new ActionGenerator(srcTree, dstTree, m.getMappings());
        g.generate();
        List<Action> actions = g.getActions(); // return the actions
        return actions.size();
    }

    public static void analyzeRepo(String repositoryPath, AbstractJdtTreeGenerator treeGenerator) throws IOException {

        Statistics stats = new Statistics();

        Repository repository = GitHelper.openRepository(repositoryPath);
        Collection<RevCommit> commits = GitHelper.getCommits(repository, "HEAD");
        for (RevCommit commit : commits) {
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

                    System.out.println(treeGenerator.getClass().getName() + " : " + repositoryPath + " : " + commit.getName() + " : " + diff.getOldPath());
                    int gumTreeScore = runGumTree(srcTreeJdt, dstTreeJdt);
                    System.out.println("(JDT) GumTree: " + gumTreeScore);
                    double rtedScore = RTEDCalculator.caclulateRTEDValue(srcTreeJdt, dstTreeJdt);
                    System.out.println("(JDT) RTED: " + rtedScore);
                    int changeDistillerScore = runChangeDistiller(srcTreeJdt, dstTreeJdt);
                    System.out.println("(JDT) ChangeDistiller: " + changeDistillerScore);
                    //System.out.println("(CD) ChangeDistiller: " + runChangeDistiller(srcTreeCD, dstTreeCD));
                    //System.out.println("(CD) GumTree: " + runGumTree(srcTreeCD, dstTreeCD));

                    stats.addEntry(gumTreeScore, rtedScore, changeDistillerScore);
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
        //Run.initGenerators();
        Matcher m = new CompositeMatchers.ChangeDistiller(srcTree,dstTree, new MappingStore());
        m.match();
        ActionGenerator g = new ActionGenerator(srcTree, dstTree, m.getMappings());
        g.generate();
        List<Action> actions = g.getActions(); // return the actions
        return actions.size();
    }


}
