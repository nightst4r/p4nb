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
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.nodes.Node;

/**
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public abstract class AbstractSingleNodeAction extends AbstractAction {

    public AbstractSingleNodeAction(String name) {
        super(name);
    }

    protected abstract void performAction(File file);

    @Override
    protected final void performAction(Node[] activatedNodes) {
        performAction(VCSContext.forNodes(activatedNodes).getRootFiles().iterator().next());
    }

    @Override
    protected final boolean enable(Node[] activatedNodes) {
        return VCSContext.forNodes(activatedNodes).getRootFiles().size() == 1;
    }

}
