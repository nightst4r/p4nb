/*
 * This file is part of p4nb.
 *
 * p4nb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * p4nb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with p4nb.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.heresylabs.netbeans.p4;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class FileStatusProvider {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private final HashMap<String, Status> actionStringsMap;

    // cached file statuses
    private Task refreshTask;
    private final Set<File> filesToRefresh = new HashSet<File>();
    private final Map<File, Status> statusMap = new HashMap<File, Status>();
    private final Map<File, String> revisionMap = new HashMap<File, String>();
    private final HashMap<File, Long> lastCheckMap = new HashMap<File, Long>();

    public FileStatusProvider() {
        actionStringsMap = new HashMap<String, Status>();
        actionStringsMap.put("edit", Status.EDIT);
        actionStringsMap.put("add", Status.ADD);
        actionStringsMap.put("delete", Status.DELETE);
        // putting null for the case when there is no action
        actionStringsMap.put(null, Status.NONE);

        refreshTask = new RequestProcessor("Perforce - file status refresh", 1).create(new Runnable() {

            public void run() {
                Set<File> files;
                synchronized (filesToRefresh) {
                    files = new HashSet<File>(filesToRefresh);
                    filesToRefresh.clear();
                }
                for (File f : files) {
                    refresh(f);
                }
                PerforceVersioningSystem.getInstance().fireFilesRefreshed(files);
            }

        });
    }

    // <editor-fold defaultstate="collapsed" desc=" internal logic ">
    private void refresh(File file) {
        Proc proc = PerforceVersioningSystem.getInstance().getWrapper().execute("fstat", file);
        String output = proc.getOutput();

        // TODO check for error output for "file not revisioned" or something like that
        if (output == null || output.length() < 1) {
            statusMap.put(file, Status.LOCAL);
        }
        else {
            statusMap.put(file, parseStatus(output));
            revisionMap.put(file, parseRevision(output));
        }
        lastCheckMap.put(file, System.currentTimeMillis());
    }

    private String parseRevision(String output) {
        String haveRev = "0";
        String headRev = "0";

        int haveRevStart = output.indexOf("haveRev");
        if (haveRevStart >= 0) {
            haveRev = cutData(output, haveRevStart);
        }
        int headRevStart = output.indexOf("headRev");
        if (headRevStart >= 0) {
            headRev = cutData(output, headRevStart);
        }
        return ' ' + haveRev + '/' + headRev;
    }

    private Status parseStatus(String output) {
        String actionString = null;
        // will write action with space because of some other statuses like actionOwner:
        int actionStart = output.indexOf("action ");
        if (actionStart >= 0) {
            actionString = cutData(output, actionStart);
        }
        return actionStringsMap.get(actionString);
    }

    public String getFileRevision(File file) {
        return revisionMap.get(file);
    }

    public Status getFileStatus(File file) {
        Long last = lastCheckMap.get(file);
        if (last == null) {
            refreshAsync(false, file);
            return Status.UNKNOWN;
        }
        if (System.currentTimeMillis() - last >= 3000) {
            refreshAsync(false, file);
        }
        return statusMap.get(file);
    }

    public Status getFileStatusForce(File file) {
        refresh(file);
        Status s = getFileStatus(file);
        if (s.isUnknown()) {
            throw new IllegalStateException("getFileStatusForce god Unknown file status");
        }
        return s;
    }

    public void refreshAsync(boolean recursively, File... files) {
        synchronized (filesToRefresh) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (recursively) {
                    listFilesRecursively(file, filesToRefresh);
                }
                else {
                    filesToRefresh.add(file);
                }
            }
        }
        refreshTask.schedule(300);
    }

    private void listFilesRecursively(File file, Set<File> files) {
        if (file.isFile()) {
            files.add(file);
            return;
        }
        File[] f = file.listFiles();
        for (int i = 0; i < f.length; i++) {
            listFilesRecursively(f[i], files);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" parsing p4 output ">
    private String cutData(String output, int startIdex) {
        if (startIdex < 0) {
            reportIllegalFstatOutput(output);
            return null;
        }
        int spaceIndex = output.indexOf(' ', startIdex);
        int lineEnd = output.indexOf(LINE_SEPARATOR, startIdex);
        if (spaceIndex >= lineEnd) {
            reportIllegalFstatOutput(output);
        }
        return output.substring(spaceIndex + 1, lineEnd);
    }

    private void reportIllegalFstatOutput(String output) {
        PerforceVersioningSystem.logWarning(this, "Illegal fstat output: " + output);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" statuses ">
    public enum Status {

        UNKNOWN, LOCAL, NONE, EDIT, ADD, DELETE;

        public boolean isLocal() {
            return this == LOCAL;
        }

        public boolean isUnknown() {
            return this == UNKNOWN;
        }
    }
    // </editor-fold>
}
