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
package org.heresylabs.netbeans.p4.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Set;
import javax.swing.AbstractAction;
import org.heresylabs.netbeans.p4.CliWrapper;
import org.heresylabs.netbeans.p4.PerforceVersioningSystem;
import org.heresylabs.netbeans.p4.Proc;
import org.netbeans.modules.versioning.spi.VCSContext;

/**
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class FileAction extends AbstractAction {

    private final VCSContext context;
    private final String command;

    public FileAction(VCSContext context, String command, String name) {
        putValue(NAME, name);
        this.context = context;
        this.command = command;
    }

    public void actionPerformed(ActionEvent e) {
        Set<File> files = context.getFiles();
        if (files == null || files.isEmpty()) {
            PerforceVersioningSystem.print("No files to perform operaion", true);
            return;
        }

        CliWrapper wrapper = PerforceVersioningSystem.getInstance().getWrapper();
        for (File f : files) {
            Proc p = wrapper.execute(command, f);
            if (p.getExitValue() != 0) {
                PerforceVersioningSystem.logWarning(this, "Bad exitValue of process: " + p.getErrors());
            }
        }
    }

}
