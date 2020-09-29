package danasoft;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class TargetStore {
    private static final String TFILE = "targets.txt";
    private final List<TargetView> tList;

    TargetStore(TargetView.TargetListener l){
        tList = new ArrayList<>();
        init(l);
    }

    List<TargetView> getTargets() { return tList; }
//    TargetView findById(int id) {
//        final TargetView t = new TargetView();
//        tList.forEach(tv -> { if (tv.getId() == id) t.set(tv); });
//        return t;
//    }
    void addTarget(final TargetView tv) {
        tList.add(tv);
        persist();
//        return tList;
    }

    private void init(final TargetView.TargetListener listener) {
        List<String> rawTargets;
        try {
            rawTargets = new FileData().readData(TFILE);
        } catch (IOException e) {
            e.printStackTrace();
            rawTargets = new ArrayList<>();
        }
        if (rawTargets.size() < 1) return;
        for (String s : rawTargets) {
            if (s.isEmpty()) continue;
            String[] parts = s.split(";");
            tList.add(new TargetView(Integer.parseInt(parts[0]), parts[1], parts[2], listener));
        }
    }

    private void persist() {
        final List<String> serialList = new ArrayList<>();
        for (TargetView tv : tList) {
            serialList.add(String.format("%d;%s;%s", tv.getId(), tv.getName(), tv.getDir()));
        }
        try {
            new FileData().writeData(serialList, TFILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
