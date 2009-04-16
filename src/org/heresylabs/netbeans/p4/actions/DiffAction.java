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

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.StringReader;
import org.heresylabs.netbeans.p4.PerforceVersioningSystem;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.TopComponent;

/**
 * Action to open internal Diff. Works only if single file is selected.
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class DiffAction extends AbstractSingleNodeAction {

    public DiffAction() {
        super("Diff");
    }

    @Override
    protected void performAction(File file) {
        showDiff(file);
    }

    private void showDiff(File file) {
        DiffTopComponent diffTopComponent = new DiffTopComponent(createDiffView(file));
        diffTopComponent.setName(file.getAbsolutePath());
        diffTopComponent.setDisplayName(file.getName());
        diffTopComponent.open();
        diffTopComponent.requestActive();
    }

    private DiffView createDiffView(File file) {
        try {
            String name = file.getName();
            String mime;
            FileObject fo = FileUtil.toFileObject(file);
            if (fo == null) {
                mime = "text/plain";
            }
            else {
                mime = fo.getMIMEType();
            }
            StreamSource local = createLocalStreamSource(file, mime);
            StreamSource remote = createRemoteStreamSource(file, name, mime);
            DiffView diffView = Diff.getDefault().createDiff(local, remote);
            return diffView;
        }
        catch (Exception e) {
            PerforceVersioningSystem.logError(this, e);
        }
        return null;
    }

    private StreamSource createLocalStreamSource(File file, String mime) {
        StreamSource local = StreamSource.createSource(file.getName(), file.getAbsolutePath(), mime, file);
        return local;
    }

    private StreamSource createRemoteStreamSource(File file, String name, String mime) {
        String output = PerforceVersioningSystem.getInstance().getWrapper().execute("print", file).getOutput();
        int lineEnd = output.indexOf('\n');
        String content;
        String title;
        if (lineEnd < 0) {
            content = output;
            title = file.getName();
        }
        else {
            lineEnd++;
            title = output.substring(0, lineEnd);
            content = output.substring(lineEnd);
        }
        return StreamSource.createSource(file.getName(), title, mime, new StringReader(content));
    }

    private static class DiffTopComponent extends TopComponent {

        public DiffTopComponent(Component panel) {
            setLayout(new BorderLayout());
            add(panel, BorderLayout.CENTER);
        }

        public DiffTopComponent(DiffView diffView) {
            this(diffView.getComponent());
        }

        @Override
        protected String preferredID() {
            return "Perforce Diff";
        }
    }
}
