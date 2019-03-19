import com.sun.javafx.binding.StringFormatter;

import java.util.ArrayList;
import java.util.List;

public class Statistics {

    private List<StatisticsEntry> entryList;

    public Statistics() {
        entryList = new ArrayList<>();
    }

    public void addEntry(double gumTree, double rted, double changeDistiller) {
        entryList.add(new StatisticsEntry(gumTree, rted, changeDistiller));
    }

    public void printStats() {
        // Compare rted to gumtree
        int gumTreeBetter = 0;
        int equalValue = 0;
        int rtedBetter = 0;
        for (StatisticsEntry statisticsEntry : entryList) {
            if (statisticsEntry.gumTree < statisticsEntry.rted) {
                gumTreeBetter++;
            } else if (statisticsEntry.gumTree == statisticsEntry.rted) {
                equalValue++;
            } else {
                rtedBetter++;
            }
        }
        System.out.println("-------------- GumTree vs. rTED --------------");
        System.out.println(String.format("GumTree better: %s (%f)", gumTreeBetter, gumTreeBetter / (double)entryList.size()));
        System.out.println(String.format("Equal: %s (%f)", equalValue, equalValue / (double)entryList.size()));
        System.out.println(String.format("RTED better: %s (%f)", rtedBetter, rtedBetter / (double)entryList.size()));


        // Compare changedistiller to gumtree
        gumTreeBetter = 0;
        equalValue = 0;
        int cdBetter = 0;
        for (StatisticsEntry statisticsEntry : entryList) {
            if (statisticsEntry.gumTree < statisticsEntry.changeDistiller) {
                gumTreeBetter++;
            } else if (statisticsEntry.gumTree == statisticsEntry.changeDistiller) {
                equalValue++;
            } else {
                cdBetter++;
            }
        }
        System.out.println("-------------- GumTree vs. ChangeDistiller --------------");
        System.out.println(String.format("GumTree better: %s (%f)", gumTreeBetter, gumTreeBetter / (double)entryList.size()));
        System.out.println(String.format("Equal: %s (%f)", equalValue, equalValue / (double)entryList.size()));
        System.out.println(String.format("ChangeDistiller better: %s (%f)", cdBetter, cdBetter / (double)entryList.size()));
    }

    public class StatisticsEntry {

        double gumTree;
        double rted;
        double changeDistiller;

        public StatisticsEntry(double gumTree, double rted, double changeDistiller) {
            this.gumTree = gumTree;
            this.rted = rted;
            this.changeDistiller = changeDistiller;
        }
    }
}
