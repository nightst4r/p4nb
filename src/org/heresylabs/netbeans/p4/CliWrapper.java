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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class to wrap p4 cli into Proc object, with full output and error streams inside.
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class CliWrapper {

    /**
     * Execute p4 with given command on given file.
     * @param command p4 command to execute
     * @param file target argument for p4 command
     * @return Proc with output and error streams of p4 execution,
     * or null if there was no connection or some exception happened.
     */
    public Proc execute(String command, File file) {
        Connection connection = PerforceVersioningSystem.getInstance().getConnectionForFile(file);
        if (connection == null) {
            // TODO better open connection configuration dialog here
            PerforceVersioningSystem.print("Connection is empty", true);
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("p4");
        sb.append(" -u ").append(connection.getUser());
        sb.append(" -c ").append(connection.getClient());
        sb.append(" -p ").append(connection.getServer());
        sb.append(" -P ").append(connection.getPassword());
        sb.append(' ').append(command).append(' ');
        sb.append(file.getName());
        return procExecute(sb.toString(), file.getParentFile());
    }

    /**
     * Util method to implement external p4 execution
     * @param command command argument for p4
     * @param dir working folder of process
     * @return Proc with execution outputs or null if exception thrown
     */
    private Proc procExecute(String command, File dir) {
        try {
            PerforceVersioningSystem.print(command, false);
            Process p = Runtime.getRuntime().exec(command, null, dir);
            String output = readStreamContent(p.getInputStream());
            String error = readStreamContent(p.getErrorStream());
            int exitValue = p.waitFor();
            return new Proc(exitValue, output, error);
        }
        catch (Exception e) {
            PerforceVersioningSystem.logError(this, e);
        }
        return null;
    }

    /**
     * Util method to read InputStream into String
     * @param in stream to read
     * @return String containing full strem content
     * @throws java.io.IOException
     */
    private String readStreamContent(InputStream in) throws IOException {
        byte[] buffer = new byte[4096];
        int read = in.read(buffer);
        if (read == -1) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        do {
            out.write(buffer, 0, read);
        } while ((read = in.read(buffer)) >= 0);
        return out.toString();
    }
}
