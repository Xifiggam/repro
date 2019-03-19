import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.gen.jdt.AbstractJdtTreeGenerator;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
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
        analyzeRepo("C:\\Users\\Veit.ISYSINST\\Desktop\\projects\\Material-Animations", new JdtTreeGenerator());



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

                String srcString = GitHelper.getFileContent(diff.getOldId(), repository);
                String dstString = GitHelper.getFileContent(diff.getNewId(), repository);
                ITree srcTree = new JdtTreeGenerator().generateFromString(srcString).getRoot();
                ITree dstTree = new JdtTreeGenerator().generateFromString(dstString).getRoot();
                System.out.println(treeGenerator.getClass().getName()+ " : " + repositoryPath + " : " + commit.getName()  + " : " +  diff.getOldPath());
                System.out.println("GumTree: " + runGumTree(srcTree, dstTree));
                System.out.println("RTED: " + RTEDCalculator.caclulateRTEDValue(srcTree, dstTree));
            }

        }

    }


}
