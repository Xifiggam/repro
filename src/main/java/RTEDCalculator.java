import com.github.gumtreediff.tree.ITree;
import distance.RTED_InfoTree_Opt;
import util.LblTree;

public class RTEDCalculator {

    public static double caclulateRTEDValue(ITree src, ITree dst) {
        String srcTreeString = convertITreeToString(src);
        String dstTreeString = convertITreeToString(dst);

        RTED_InfoTree_Opt rted = new RTED_InfoTree_Opt(1, 1, 1);
        LblTree ltSrc = LblTree.fromString(srcTreeString);
        LblTree ltDst = LblTree.fromString(dstTreeString);
        rted.init(ltSrc, ltDst);

        rted.computeOptimalStrategy();
        double ted = rted.nonNormalizedTreeDist();
        return ted;
    }

    public static String convertITreeToString(ITree tree) {

        String pre = "{";
        String childs = "";
        String post = "}";

        for (ITree child : tree.getChildren()) {
            childs += convertITreeToString(child);
        }

        return pre + tree.getId() + childs + post;
    }
}
