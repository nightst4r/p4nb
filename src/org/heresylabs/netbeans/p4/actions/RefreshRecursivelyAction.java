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
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class RefreshRecursivelyAction extends AbstractAction {

    public RefreshRecursivelyAction() {
        super("Refresh Recursively");
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        Set<File> rootFiles = VCSContext.forNodes(activatedNodes).getRootFiles();
        Set<File> filesRecuresively = new HashSet<File>();
        for (File f : rootFiles) {
            addFilesRecursively(f, filesRecuresively);
        }
        PerforceVersioningSystem.getInstance().refresh(filesRecuresively);
    }

    private void addFilesRecursively(File file, Set<File> files) {
        if (file.isFile()) {
            files.add(file);
            return;
        }
        File[] f = file.listFiles();
        for (int i = 0; i < f.length; i++) {
            addFilesRecursively(f[i], files);
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        // refresh is always possible
        return true;
    }
}
