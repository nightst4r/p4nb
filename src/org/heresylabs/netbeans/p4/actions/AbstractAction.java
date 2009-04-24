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
import org.heresylabs.netbeans.p4.PerforceVersioningSystem;
import org.heresylabs.netbeans.p4.Proc;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public abstract class AbstractAction extends NodeAction {

    private final String name;

    public AbstractAction(String name) {
        this.name = name;
    }

    @Override
    abstract protected void performAction(Node[] activatedNodes);

    @Override
    abstract protected boolean enable(Node[] activatedNodes);

    protected void execute(String command, File file) {
        Proc proc = PerforceVersioningSystem.getInstance().getWrapper().execute(command, file);
        if (proc.getExitValue() != 0) {
            PerforceVersioningSystem.logWarning(this, "Bad exitValue of process: " + proc.getErrors());
        }
        PerforceVersioningSystem.getInstance().refresh(file);
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final HelpCtx getHelpCtx() {
        return null;
    }

}
