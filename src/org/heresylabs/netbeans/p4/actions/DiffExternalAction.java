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
import javax.swing.AbstractAction;
import org.heresylabs.netbeans.p4.PerforceVersioningSystem;
import org.netbeans.modules.versioning.spi.VCSContext;

/**
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class DiffExternalAction extends AbstractAction {

    private final VCSContext context;

    public DiffExternalAction(VCSContext context) {
        putValue(NAME, "Diff External");
        this.context = context;
    }

    public void actionPerformed(ActionEvent e) {
        if (context == null || context.getFiles().isEmpty()) {
            PerforceVersioningSystem.print("No files to perform operation", true);
            return;
        }
        for (File f : context.getFiles()) {
            if (f.isFile()) {
                // TODO background it
                PerforceVersioningSystem.getInstance().p4merge(f);
            }
        }
    }

}
