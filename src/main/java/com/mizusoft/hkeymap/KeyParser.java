package com.mizusoft.hkeymap;

import android.widget.Toast;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tim
 */
public class KeyParser {

    private static String SEP_STR = ":";
    private static String CON_STR = "KEYCODE_";

    public static void asyncLoadKeymap(final MainActivity mainActivity, String keymapLocation) {
        Shell shell = new Shell();
        shell.execute("cat " + keymapLocation);
        shell.setShellExec(new ShellExec() {

            @Override
            public void execute(boolean result, String output) {
                mainActivity.updateListAdapter(
                        parseText(output));
            }
        });
    }

    public static void asyncSaveKeymap(final MainActivity mainActivity, String savePath, ArrayList<String> values) {
        Shell shell = new Shell();
        ArrayList<String> cmnds = new ArrayList<String>();
        cmnds.add("mount -o rw,remount /system");
        cmnds.add("echo \"# created with hkeymap (http://github.com/cybertim/hkeymap)\" > " + savePath);
        for (String s : values) {
            cmnds.add("echo -e \"" + norm(s) + "\" >> " + savePath);
        }
        cmnds.add("mount -o ro,remount /system");
        shell.execute(cmnds.toArray(new String[cmnds.size()]));
        shell.setShellExec(new ShellExec() {

            @Override
            public void execute(boolean result, String output) {
                Toast.makeText(mainActivity.getApplicationContext(), "file has been saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static String norm(String n) {
        String[] s = n.split(SEP_STR);
        return "key " + s[0] + "\\t" + s[1].replace(CON_STR, "") + "\\tWAKE_DROPPED";
    }

    private static ArrayList<String> parseText(String in) {
        Pattern p = Pattern.compile("key\\s+(\\d+)\\s+(\\w+)\\s+\\w+");
        ArrayList<String> items = new ArrayList<String>();
        for (String s : in.split("\n")) {
            Matcher m = p.matcher(s);
            if (m.matches()) {
                items.add(m.group(1) + SEP_STR + CON_STR + m.group(2));
            }
        }
        return items;
    }

}
