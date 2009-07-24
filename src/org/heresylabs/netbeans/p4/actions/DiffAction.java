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
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import org.heresylabs.netbeans.p4.FileStatusProvider.Status;
import org.heresylabs.netbeans.p4.PerforceVersioningSystem;
import org.heresylabs.netbeans.p4.Proc;
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

    private static final String[] COLUMNS = {"Revision", "Changelist", "Date Submitted", "Submitted By", "Description"};

    public DiffAction() {
        super("Diff");
    }

    @Override
    protected void performAction(File file) {
        showDiff(file);
    }

    private void showDiff(File file) {
        DiffTopComponent diffTopComponent = new DiffTopComponent(createDiffView(file), getFileRevisions(file));
        diffTopComponent.setName(file.getAbsolutePath());
        diffTopComponent.setDisplayName(file.getName());
        diffTopComponent.open();
        diffTopComponent.requestActive();
    }

    private List<Revision> getFileRevisions(File file) {
        Proc revisionProc = PerforceVersioningSystem.getInstance().getWrapper().execute("filelog -t", file);
        if (revisionProc == null || revisionProc.getExitValue() != 0) {
            PerforceVersioningSystem.print(revisionProc.getErrors(), true);
            return Collections.emptyList();
        }
        return parseRevisions(revisionProc.getOutput());
    }

    private List<Revision> parseRevisions(String output) {
        Pattern pattern = Pattern.compile("... #(\\d+) .+ (\\d+).+on (.+) by (.+)@.+'(.+)'");
        Matcher matcher = pattern.matcher(output);
        List<Revision> result = new ArrayList<Revision>();
        while (matcher.find()) {
            Revision r = new Revision(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
            result.add(r);
        }
        return result;
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
        Proc printProc = PerforceVersioningSystem.getInstance().getWrapper().execute("print", file);
        if (printProc == null || printProc.getExitValue() != 0) {
            return StreamSource.createSource(name, name, mime, new StringReader(printProc.getErrors()));
        }
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
        return StreamSource.createSource(name, title, mime, new StringReader(content));
    }

    @Override
    protected boolean statusEnabled(Status status) {
        return true;
        /*
        switch (status) {
            case EDIT:
            case OUTDATED:
                return true;
            default:
                return false;
        }
         */
    }

    private static class DiffTopComponent extends TopComponent {

        public DiffTopComponent(DiffView diffView, List<Revision> revisions) {
            RevisionTableModel tableModel = new RevisionTableModel(revisions);
            JTable table = new JTable(tableModel);

            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setTopComponent(new JScrollPane(table));
            splitPane.setBottomComponent(diffView.getComponent());
            splitPane.setOneTouchExpandable(true);
            splitPane.setResizeWeight(0.2);

            setLayout(new BorderLayout());
            add(splitPane, BorderLayout.CENTER);
        }

        @Override
        protected String preferredID() {
            return "Perforce Diff";
        }

    }

    private static class RevisionTableModel extends AbstractTableModel {

        private List<Revision> list;

        public RevisionTableModel(List<Revision> list) {
            this.list = list;
        }

        public int getRowCount() {
            return list.size();
        }

        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Revision r = list.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return r.getRevision();
                case 1:
                    return r.getChangelist();
                case 2:
                    return r.getDateSubmitted();
                case 3:
                    return r.getSubmittedBy();
                case 4:
                    return r.getDescription();

            }
            return null;
        }

    }

    public static class Revision {

        private String revision;
        private String changelist;
        private String dateSubmitted;
        private String submittedBy;
        private String description;

        public Revision(String revision, String changelist, String dateSubmitted, String submittedBy, String description) {
            this.revision = revision;
            this.changelist = changelist;
            this.dateSubmitted = dateSubmitted;
            this.submittedBy = submittedBy;
            this.description = description;
        }

        public String getChangelist() {
            return changelist;
        }

        public void setChangelist(String changelist) {
            this.changelist = changelist;
        }

        public String getDateSubmitted() {
            return dateSubmitted;
        }

        public void setDateSubmitted(String dateSubmitted) {
            this.dateSubmitted = dateSubmitted;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRevision() {
            return revision;
        }

        public void setRevision(String revision) {
            this.revision = revision;
        }

        public String getSubmittedBy() {
            return submittedBy;
        }

        public void setSubmittedBy(String submittedBy) {
            this.submittedBy = submittedBy;
        }

        @Override
        public String toString() {
            return "rev: " + revision +
                    " changelist: " + changelist +
                    " date: " + dateSubmitted +
                    " by: " + submittedBy +
                    " desc: " + description;
        }

    }
}
