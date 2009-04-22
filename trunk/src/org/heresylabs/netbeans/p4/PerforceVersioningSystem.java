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
package org.heresylabs.netbeans.p4;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.heresylabs.netbeans.p4.FileStatusProvider.Status;
import org.heresylabs.netbeans.p4.actions.DiffAction;
import org.heresylabs.netbeans.p4.actions.DiffExternalAction;
import org.heresylabs.netbeans.p4.actions.FileAction;
import org.heresylabs.netbeans.p4.actions.OptionsAction;
import org.heresylabs.netbeans.p4.actions.RefreshAction;
import org.heresylabs.netbeans.p4.actions.RefreshRecursivelyAction;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSAnnotator.ActionDestination;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.openide.util.NbPreferences;
import org.openide.util.actions.SystemAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class PerforceVersioningSystem extends VersioningSystem {

    public static final String NAME = "Perforce";
    private static final String KEY_CONNECTIONS = "connections";
    private static final String KEY_PREFERENCES = "preferences";
    private static PerforceVersioningSystem INSTANCE;

    // <editor-fold defaultstate="collapsed" desc=" init block ">
    /**
     * Singleton provider.
     * @return
     */
    public static PerforceVersioningSystem getInstance() {
        if (INSTANCE == null) {
            logWarning(PerforceVersioningSystem.class, "PerforceVersioningSystem singleton is null");
        }
        return INSTANCE;
    }

    /**
     * Counstructs and inits perforce support, and assignes self to static singleton
     */
    public PerforceVersioningSystem() {
        synchronized (PerforceVersioningSystem.class) {
            // TODO remove this check in future
            if (INSTANCE != null) {
                logWarning(this, "PerforceVersioningSystem constructed again");
            }
            INSTANCE = this;
        }
        putProperty(PROP_DISPLAY_NAME, NAME);
        putProperty(PROP_MENU_LABEL, NAME);
        init();
    }

    private void init() {
        Preferences preferences = NbPreferences.forModule(getClass());
        loadConnections(preferences);
        String prefs = preferences.get(KEY_PREFERENCES, null);
        if (prefs == null) {
            perforcePreferences = new PerforcePreferences();
        }
        else {
            perforcePreferences = parsePreferences(prefs);
        }
        initPerformanceHacks();
    }

    /**
     * Small performance hack to check for workspaces as fast as possible
     */
    private void initPerformanceHacks() {
        workspaces = new String[connections.size()];
        for (int i = 0; i < connections.size(); i++) {
            Connection connection = connections.get(i);
            workspaces[i] = perforcePreferences.isCaseSensetiveWorkspaces() ? connection.getWorkspacePath() : connection.getWorkspacePath().toLowerCase();
        }
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" workspaces performance hack ">

    private String[] workspaces;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" VersioningSystem implementation ">
    private Annotator annotator = new Annotator();
    private Interceptor interceptor = new Interceptor();

    @Override
    public void getOriginalFile(File workingCopy, File originalFile) {
        // TODO check if p4 can overwrite already existing and if JVM can get another file with same filename
        // checking for file status, if it is added, none or unkown - no comparision available
        Status status = fileStatusProvider.getFileStatus(workingCopy);
        if (status != Status.EDIT && status != Status.OUTDATED) {
            return;
        }
        String originalPath;
        try {
            originalPath = originalFile.getCanonicalPath();
        }
        catch (Exception e) {
            originalPath = originalFile.getAbsolutePath();
        }
        wrapper.execute("print -o \"" + originalPath + "\" -q", workingCopy);
    }

    @Override
    public File getTopmostManagedAncestor(File file) {
        Connection c = getConnectionForFile(file);
        if (c == null) {
            return null;
        }
        return new File(c.getWorkspacePath());
    }

    @Override
    public VCSAnnotator getVCSAnnotator() {
        return annotator;
    }

    @Override
    public VCSInterceptor getVCSInterceptor() {
        return interceptor;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" connections ">

    private List<Connection> connections = new ArrayList<Connection>();

    /**
     * @return copy of current connections
     */
    public List<Connection> getConnections() {
        return new ArrayList<Connection>(connections);
    }

    /**
     * Set new connections list, with full save and init cycle.
     * @param connections
     */
    public void setConnections(List<Connection> connections) {
        this.connections = connections;
        saveConnections(NbPreferences.forModule(getClass()));
        initPerformanceHacks();
        fireVersionedFilesChanged();
    }

    /**
     * Parse prefs for connections params
     * @param prefs
     */
    private void loadConnections(Preferences prefs) {
        // TODO think about synchronisation
        List<String> connectionsStrings = getStringList(prefs, KEY_CONNECTIONS);
        if (connectionsStrings == null || connectionsStrings.isEmpty()) {
            return;
        }
        List<Connection> conns = new ArrayList<Connection>(connectionsStrings.size());
        for (int i = 0; i < connectionsStrings.size(); i++) {
            String string = connectionsStrings.get(i);
            conns.add(parseConnection(string));
        }
        connections = conns;
    }

    /**
     * Save all connection params to NbPreferences.
     * @param prefs
     */
    private void saveConnections(Preferences prefs) {
        List<String> conns = new ArrayList<String>(connections.size());
        for (int i = 0; i < connections.size(); i++) {
            conns.add(getConnectionAsString(connections.get(i)));
        }
        putStringList(prefs, KEY_CONNECTIONS, conns);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" VCS logic ">

    private PerforcePreferences perforcePreferences;

    public PerforcePreferences getPerforcePreferences() {
        return new PerforcePreferences(perforcePreferences.isCaseSensetiveWorkspaces(),
                perforcePreferences.isConfirmEdit(), perforcePreferences.isInterceptAdd(),
                perforcePreferences.isPrintOutput(), perforcePreferences.isShowAction(),
                perforcePreferences.getColorBase(), perforcePreferences.getColorLocal(),
                perforcePreferences.getColorUnknown(), perforcePreferences.getColorAdd(),
                perforcePreferences.getColorDelete(), perforcePreferences.getColorEdit(),
                perforcePreferences.getColorOutdated());
    }

    public void setPerforcePreferences(PerforcePreferences perforcePreferences) {
        this.perforcePreferences = perforcePreferences;
        Preferences preferences = NbPreferences.forModule(getClass());
        preferences.put(KEY_PREFERENCES, getPreferencesAsString(perforcePreferences));
        initPerformanceHacks();
    }

    /**
     * Implementation of Actions getter.
     * @see org.netbeans.modules.versioning.spi.VCSAnnotator#getActions(org.netbeans.modules.versioning.spi.VCSContext, org.netbeans.modules.versioning.spi.VCSAnnotator.ActionDestination)
     */
    private Action[] getPerforceActions(VCSContext context, ActionDestination destination) {
        if (destination == ActionDestination.PopupMenu) {
            return asArray(
                    SystemAction.get(DiffAction.class),
                    SystemAction.get(DiffExternalAction.class),
                    null,
                    new FileAction(context, "add", "Add"),
                    new FileAction(context, "delete", "Delete"),
                    null,
                    new FileAction(context, "revert", "Revert"),
                    null,
                    new FileAction(context, "edit", "Edit"),
                    null,
                    new FileAction(context, "sync", "Sync"),
                    new FileAction(context, "sync -f", "Sync Force"),
                    null,
                    SystemAction.get(RefreshAction.class),
                    SystemAction.get(RefreshRecursivelyAction.class));
        }
        // if we are still here - it's main menu
        return asArray(
                SystemAction.get(DiffAction.class),
                SystemAction.get(DiffExternalAction.class),
                null,
                new FileAction(context, "add", "Add"),
                new FileAction(context, "delete", "Delete"),
                null,
                new FileAction(context, "revert", "Revert"),
                null,
                new FileAction(context, "edit", "Edit"),
                null,
                new FileAction(context, "sync", "Sync"),
                new FileAction(context, "sync -f", "Sync Force"),
                null,
                SystemAction.get(RefreshAction.class),
                SystemAction.get(RefreshRecursivelyAction.class),
                null,
                optionsAction);
    }

    public Connection getConnectionForFile(File file) {
        if (file == null) {
            return null;
        }
        String filePath;
        try {
            filePath = file.getCanonicalPath();
        }
        catch (Exception e) {
            filePath = file.getAbsolutePath();
        }

        if (!perforcePreferences.isCaseSensetiveWorkspaces()) {
            filePath = filePath.toLowerCase();
        }

        for (int i = 0; i < workspaces.length; i++) {
            if (filePath.startsWith(workspaces[i])) {
                // workspaces and connections must have same indexes
                return connections.get(i);
            }
        }
        return null;
    }

    private CliWrapper wrapper = new CliWrapper();
    private FileStatusProvider fileStatusProvider = new FileStatusProvider();
    private final OptionsAction optionsAction = new OptionsAction();

    public CliWrapper getWrapper() {
        return wrapper;
    }

    private void edit(File file) {
        wrapper.execute("edit", file);
        refresh(file);
    }

    private void add(File file) {
        wrapper.execute("add", file);
        refresh(file);
    }

    private void delete(File file) {
        wrapper.execute("delete", file);
        refresh(file);
    }

    private void revert(File file) {
        wrapper.execute("revert", file);
        refresh(file);
    }

    public void p4merge(File file) {
        String tmpPath = System.getProperty("java.io.tmpdir");
        File remoteFile = new File(tmpPath, file.getName() + System.currentTimeMillis());
        // doing it to leave temp folder clear:
        remoteFile.deleteOnExit();
        getOriginalFile(file, remoteFile);
        try {
            Runtime.getRuntime().exec("p4merge \"" + remoteFile.getCanonicalPath() + "\" \"" + file.getCanonicalPath() + "\"", null);
        }
        catch (Exception e) {
            logError(this, e);
        }
    }

    public void refresh(Set<File> files) {
        fileStatusProvider.refreshAsync(files.toArray(new File[files.size()]));
    }

    public void refresh(File file) {
        fileStatusProvider.refreshAsync(file);
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" file statuses ">
    private String annotatePerforceName(String name, VCSContext context) {
        if (context.getFiles().size() > 1) {
            return name;
        }
        File file = context.getRootFiles().iterator().next();
        if (file.isFile()) {
            Status status = fileStatusProvider.getFileStatus(file);
            String suffix;
            String nameColor = perforcePreferences.getColorBase();

            if (status.isLocal()) {
                suffix = "Local Only";
                nameColor = perforcePreferences.getColorLocal();
            }
            else if (status.isUnknown()) {
                suffix = "...";
                nameColor = perforcePreferences.getColorUnknown();
            }
            else {
                suffix = fileStatusProvider.getFileRevision(file);
                switch (status) {
                    case ADD: {
                        nameColor = perforcePreferences.getColorAdd();
                        break;
                    }
                    case DELETE: {
                        nameColor = perforcePreferences.getColorDelete();
                        break;
                    }
                    case EDIT: {
                        nameColor = perforcePreferences.getColorEdit();
                        break;
                    }
                    case OUTDATED: {
                        nameColor = perforcePreferences.getColorOutdated();
                        break;
                    }
                }
            }

            StringBuilder nameBuilder = new StringBuilder();
            nameBuilder.append("<font color=\"#");
            nameBuilder.append(nameColor);
            nameBuilder.append("\">");
            nameBuilder.append(name);
            nameBuilder.append("</font>");
            boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
            if (annotationsVisible) {
                nameBuilder.append("   <font color=\"#999999\">[ ");
                nameBuilder.append(suffix);
                if (!status.isUnknown() && perforcePreferences.isShowAction()) {
                    nameBuilder.append(':').append(' ');
                    // TODO add status in other form than this
                    nameBuilder.append(status.toString());
                }
                nameBuilder.append(" ]</font>");
            }
            return nameBuilder.toString();
        }
        return name;
    }

    public void fireFilesRefreshed(Set<File> files) {
        fireStatusChanged(files);
        //fireAnnotationsChanged(f);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" static and util methods ">
    /**
     * Sorry NB guys, but "friends only" restriction for Util classes is not right!
     */
    private static List<String> getStringList(Preferences prefs, String key) {
        List<String> retval = new ArrayList<String>();
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(key)) {
                    int idx = Integer.parseInt(k.substring(k.lastIndexOf('.') + 1));
                    retval.add(idx + "." + prefs.get(k, null));
                }
            }
            List<String> rv = new ArrayList<String>(retval.size());
            rv.addAll(retval);
            for (String s : retval) {
                int pos = s.indexOf('.');
                int index = Integer.parseInt(s.substring(0, pos));
                rv.set(index, s.substring(pos + 1));
            }
            return rv;
        }
        catch (Exception ex) {
            Logger.getLogger(PerforceVersioningSystem.class.getName()).log(Level.INFO, null, ex);
            return new ArrayList<String>(0);
        }
    }

    /**
     * Sorry NB guys, but "friends only" restriction for Util classes is not right!
     */
    private static void putStringList(Preferences prefs, String key, List<String> value) {
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(key + ".")) {
                    prefs.remove(k);
                }
            }
            int idx = 0;
            for (String s : value) {
                prefs.put(key + "." + idx++, s);
            }
        }
        catch (BackingStoreException ex) {
            Logger.getLogger(PerforceVersioningSystem.class.getName()).log(Level.INFO, null, ex);
        }
    }

    private static final String RC_DELIMITER = "~=~";

    private static String getConnectionAsString(Connection connection) {
        StringBuilder sb = new StringBuilder();
        sb.append(connection.getServer());
        sb.append(RC_DELIMITER);
        sb.append(connection.getUser());
        sb.append(RC_DELIMITER);
        sb.append(connection.getClient());
        sb.append(RC_DELIMITER);
        sb.append(connection.getPassword());
        sb.append(RC_DELIMITER);
        sb.append(connection.getWorkspacePath());
        return sb.toString();
    }

    private static Connection parseConnection(String string) {
        String[] lines = string.split(RC_DELIMITER);
        return new Connection(lines[0], lines[1], lines[2], lines[3], lines[4]);
    }

    private static String getPreferencesAsString(PerforcePreferences p) {
        StringBuilder sb = new StringBuilder();
        sb.append(p.isCaseSensetiveWorkspaces() ? 't' : 'f');
        sb.append(p.isConfirmEdit() ? 't' : 'f');
        sb.append(p.isInterceptAdd() ? 't' : 'f');
        sb.append(p.isPrintOutput() ? 't' : 'f');
        sb.append(p.isShowAction() ? 't' : 'f');
        sb.append(p.isInvalidateOnRefresh() ? 't' : 'f');
        sb.append(RC_DELIMITER);
        sb.append(p.getColorAdd());
        sb.append(RC_DELIMITER);
        sb.append(p.getColorBase());
        sb.append(RC_DELIMITER);
        sb.append(p.getColorDelete());
        sb.append(RC_DELIMITER);
        sb.append(p.getColorEdit());
        sb.append(RC_DELIMITER);
        sb.append(p.getColorLocal());
        sb.append(RC_DELIMITER);
        sb.append(p.getColorOutdated());
        sb.append(RC_DELIMITER);
        sb.append(p.getColorUnknown());
        return sb.toString();
    }

    private static PerforcePreferences parsePreferences(String s) {
        PerforcePreferences p = new PerforcePreferences();
        p.setCaseSensetiveWorkspaces(s.charAt(0) == 't');
        p.setConfirmEdit(s.charAt(1) == 't');
        p.setInterceptAdd(s.charAt(2) == 't');
        p.setPrintOutput(s.charAt(3) == 't');
        p.setShowAction(s.charAt(4) == 't');
        p.setInvalidateOnRefresh(s.charAt(5) == 't');
        String[] colors = s.split(RC_DELIMITER);
        p.setColorAdd(colors[1]);
        p.setColorBase(colors[2]);
        p.setColorDelete(colors[3]);
        p.setColorEdit(colors[4]);
        p.setColorLocal(colors[5]);
        p.setColorOutdated(colors[6]);
        p.setColorUnknown(colors[7]);
        return p;
    }

    public static void logError(Object caller, Throwable e) {
        Logger.getLogger(caller.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
    }

    public static void logWarning(Object caller, String warning) {
        Logger.getLogger(caller.getClass().getName()).log(Level.WARNING, warning);
    }

    public static void print(String message) {
        print(message, false);
    }

    public static void print(String message, boolean error) {

        // checking for printing preferences:
        if (!getInstance().getPerforcePreferences().isPrintOutput()) {
            return;
        }

        String m;
        int passFlagIndex = message.indexOf(" -P ");
        if (passFlagIndex >= 0) {
            int passIndex = passFlagIndex + 4;
            int spaceIndex = message.indexOf(' ', passIndex);
            StringBuilder sb = new StringBuilder(message.length());
            sb.append(message, 0, passIndex);
            sb.append("********");
            sb.append(message, spaceIndex, message.length());
            m = sb.toString();
        }
        else {
            m = message;
        }

        InputOutput io = IOProvider.getDefault().getIO("Perforce", false);
        OutputWriter out = error ? io.getErr() : io.getOut();
        out.print('[');
        out.print(getTime());
        out.print("] ");
        out.println(m);
        out.flush();
    }

    private static final Date currentDate = new Date();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    private static String getTime() {
        synchronized (dateFormat) {
            currentDate.setTime(System.currentTimeMillis());
            return dateFormat.format(currentDate);
        }
    }

    /**
     * Utility method to convert vararg to array
     */
    private static <T> T[] asArray(T... arg) {
        return arg;
    }

    private int showConfirmation(String message, String filename) {
        if (filename.length() > 60) {
            filename = filename.substring(0, 60) + '\n' + filename.substring(60);
        }
        String[] options = {"Yes", "No"};
        int res = JOptionPane.showOptionDialog(null, message + filename, "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        return res;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" internal classes ">
    private class Annotator extends VCSAnnotator {

        @Override
        public Image annotateIcon(Image icon, VCSContext context) {
            // TODO implement
            return super.annotateIcon(icon, context);
        }

        @Override
        public String annotateName(String name, VCSContext context) {
            return annotatePerforceName(name, context);
        }

        @Override
        public Action[] getActions(VCSContext context, ActionDestination destination) {
            return getPerforceActions(context, destination);
        }

    }

    private class Interceptor extends VCSInterceptor {

        @Override
        public boolean isMutable(File file) {
            /*
             * TODO find the best way to implement this option for perforce:
             * - in default implementation "intercept edit" option will not be used
             * - if return true and user will say "no" - editing will be possible, but saving will not.
             */
            return super.isMutable(file);
        }

        @Override
        public boolean beforeDelete(File file) {
            return file.isFile() && !fileStatusProvider.getFileStatusForce(file).isLocal();
        }

        @Override
        public void doDelete(File file) throws IOException {
            int res = showConfirmation("Are you sure you want to delete ", file.getAbsolutePath());
            if (res == JOptionPane.NO_OPTION) {
                return;
            }
            Status status = fileStatusProvider.getFileStatusForce(file);
            if (status.isLocal()) {
                logWarning(this, file.getName() + " is not revisioned. Should not be deleted by p4nb");
                return;
            }
            if (status != Status.NONE) {
                revert(file);
            }
            if (status == Status.ADD) {
                file.delete();
                return;
            }
            delete(file);
        }

        @Override
        public boolean beforeMove(File from, File to) {
            return super.beforeMove(from, to);
        }

        @Override
        public void doMove(File from, File to) throws IOException {
            super.doMove(from, to);
        }

        @Override
        public void afterMove(File from, File to) {
            super.afterMove(from, to);
        }

        @Override
        public void afterCreate(File file) {
            if (file.isFile() && perforcePreferences.isInterceptAdd()) {
                add(file);
            }
        }

        @Override
        public void beforeEdit(File file) {
            if (file.canWrite()) {
                return;
            }
            if (perforcePreferences.isConfirmEdit()) {
                int res = showConfirmation("Are you sure you want to \"p4 edit\" file \n", file.getAbsolutePath());
                if (res == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            edit(file);
        }

    }
    // </editor-fold>
}
