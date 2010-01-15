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
import java.util.Set;
import org.heresylabs.netbeans.p4.FileStatusProvider.Status;
import org.heresylabs.netbeans.p4.PerforceVersioningSystem;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.nodes.Node;

/**
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class RevertAction extends AbstractAction {

    public RevertAction() {
        super("Revert");
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        PerforceVersioningSystem.saveNodes(activatedNodes);
        for (File file : VCSContext.forNodes(activatedNodes).getRootFiles()){
            execute("revert", file);
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        // TODO same method exists in AbstractSingleNodeAction
        Set<File> files = VCSContext.forNodes(activatedNodes).getRootFiles();
        
        File file = files.iterator().next();
        if (file.isDirectory()) {
            return false;
        }
        Status status = PerforceVersioningSystem.getInstance().getFileStatus(file);
        switch (status) {
            case ADD:
            case EDIT:
                return true;
            default:
                return false;
        }
    }

}
