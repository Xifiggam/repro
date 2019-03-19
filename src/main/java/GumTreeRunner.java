import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;

import java.io.IOException;
import java.util.List;

public class GumTreeRunner {

    public static void main(String[] args) throws IOException {
        String file1 = "file_v0.java";
        String file2 = "file_v1.java";

        //JDT Tree
        ITree srcTree = new JdtTreeGenerator().generateFromString(file1).getRoot();
        ITree dstTree = new JdtTreeGenerator().generateFromString(file2).getRoot();

        runGumTree(srcTree, dstTree);
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

}