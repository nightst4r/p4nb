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
import org.heresylabs.netbeans.p4.FileStatusProvider.Status;
import org.heresylabs.netbeans.p4.PerforceVersioningSystem;

/**
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class DiffExternalAction extends AbstractSingleNodeAction {

    public DiffExternalAction() {
        super("Diff External");
    }

    @Override
    protected void performAction(File file) {
        PerforceVersioningSystem.getInstance().p4merge(file);
    }

    @Override
    protected boolean statusEnabled(Status status) {
        // TODO same method in DiffAction
        switch (status) {
            case EDIT:
            case OUTDATED:
                return true;
            default:
                return false;
        }
    }

}
