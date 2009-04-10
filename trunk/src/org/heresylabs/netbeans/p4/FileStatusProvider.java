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

/**
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class FileStatusProvider {

    public static final int ACTION_NONE = 0;
    public static final int ACTION_EDIT = 1;
    public static final int ACTION_ADD = 2;
    public static final int ACTION_DELETE = 3;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private final HashMap<String, Integer> statusesMap;

    public FileStatusProvider() {
        statusesMap = new HashMap<String, Integer>();
        statusesMap.put("edit", ACTION_EDIT);
        statusesMap.put("add", ACTION_ADD);
        statusesMap.put("delete", ACTION_DELETE);
        // putting null for the case when there is no action
        statusesMap.put(null, ACTION_NONE);
    }

    public FileStatus getFileStatusNow(File file) {
        Proc proc = PerforceVersioningSystem.getInstance().getWrapper().execute("fstat", file);
        String output = proc.getOutput();

        if (output == null || output.length() < 1) {
            return null;
        }

        String haveRev = null;
        String headRev = null;
        String actionString = null;

        int haveRevStart = output.indexOf("haveRev");
        if (haveRevStart >= 0) {
            haveRev = cutData(output, haveRevStart);
        }
        int headRevStart = output.indexOf("headRev");
        if (headRevStart >= 0) {
            headRev = cutData(output, headRevStart);
        }
        // will write action with space because of some other statuses like actionOwner:
        int actionStart = output.indexOf("action ");
        if (actionStart >= 0) {
            actionString = cutData(output, actionStart);
        }

        String revision = haveRev + '/' + headRev;
        int action = statusesMap.get(actionString);

        return new FileStatus(revision, action);

    }

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

}
