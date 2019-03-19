import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import convenience.RTED;
import distance.RTED_InfoTree_Opt;
import util.LblTree;

import java.awt.peer.SystemTrayPeer;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class GumTreeRunner {

    public static void main(String[] args) throws IOException {
        String file1 = "src/main/java/GumTreeRunner.java";
        String file2 = file1;

        //JDT Tree
        ITree srcTree = new JdtTreeGenerator().generateFromFile(file1).getRoot();
        ITree dstTree = new JdtTreeGenerator().generateFromFile(file2).getRoot();

        runGumTree(srcTree, dstTree);

        String srcTreeString = convertITreeToString(srcTree);
        String dstTreeString = convertITreeToString(dstTree);

        RTED_InfoTree_Opt rted = new RTED_InfoTree_Opt(1, 1, 1);
        LblTree ltSrc = LblTree.fromString(srcTreeString);
        LblTree ltDst = LblTree.fromString(dstTreeString);
        rted.init(ltSrc, ltDst);

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

    public static String convertITreeToString(ITree tree) {

        String pre = "{";
        String childs = "";
        String post = "}";

        for (ITree child : tree.getChildren()) {
            childs += convertITreeToString(child);
        }

        return pre + tree.getLabel() + childs + post;
    }

}
