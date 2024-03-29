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

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.heresylabs.netbeans.p4.PerforceVersioningSystem;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.nodes.Node;

/**
 * Action to refresh selected files. If folder is selected - only first level files will be refreshed.
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class RefreshAction extends AbstractAction {

    public RefreshAction() {
        super("Refresh");
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        Set<File> rootFiles = VCSContext.forNodes(activatedNodes).getRootFiles();
        Set<File> files = new HashSet<File>(rootFiles.size());
        for (File file : rootFiles) {
            if (file.isFile()) {
                files.add(file);
            }
            else {
                File[] ff = file.listFiles();
                for (int i = 0; i < ff.length; i++) {
                    File f = ff[i];
                    if (f.isFile()) {
                        files.add(f);
                    }
                }
            }
        }
        PerforceVersioningSystem.getInstance().refresh(files);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        // refresh is always possible
        return true;
    }
}
