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
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class CliWrapper {

    public Proc execute(String command, File file) {
        Connection connection = PerforceVersioningSystem.getInstance().getConnectionForFile(file);
        if (connection == null) {
            // TODO better open connection configuration dialog here
            PerforceVersioningSystem.print("Connection is empty", true);
            return null;
        }

        String filePath;
        try {
            filePath = file.getCanonicalPath();
        }
        catch (IOException ex) {
            filePath = file.getAbsolutePath();
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
