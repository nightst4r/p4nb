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
package org.heresylabs.netbeans.p4.options;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.heresylabs.netbeans.p4.Connection;
import org.heresylabs.netbeans.p4.PerforcePreferences;
import org.heresylabs.netbeans.p4.PerforceVersioningSystem;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class PerforceOptionsController extends OptionsPanelController implements ListSelectionListener, DocumentListener {

    // TODO format this class for maximum readability
    private JPanel perforceOptionsPanel;
    private JCheckBox interceptAddBox;
    private JCheckBox confirmEditBox;
    private JCheckBox caseSensetiveWorkspaceBox;
    private JCheckBox printOutputBox;
    private JCheckBox showActionBox;
    private JCheckBox showAnnotations;
    private JCheckBox invalidateOnRefreshBox;
    private ConnectionPanel connectionPanel;
    private ColorsPanel colorsPanel;
    private JList connectionsList;
    private ConnectionsListModel listModel;
    private List<Connection> connections;
    private PerforcePreferences preferences;
    private boolean moved = false;

    public PerforceOptionsController() {
        perforceOptionsPanel = createOptionsPanel();
    }

    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Connections", createConnectionsPanel());
        tabbedPane.addTab("Preferences", createPreferencesPanel());

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createConnectionsPanel() {

        connectionPanel = new ConnectionPanel();
        connectionPanel.workspaceField.getDocument().addDocumentListener(this);
        listModel = new ConnectionsListModel();
        connectionsList = new JList(listModel);
        connectionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        connectionsList.addListSelectionListener(this);

        JScrollPane scrollPane = new JScrollPane(connectionsList);

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(createListButtonsPanel(), BorderLayout.EAST);
        panel.add(connectionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPreferencesPanel() {

        interceptAddBox = new JCheckBox("Intercept Add operation");
        confirmEditBox = new JCheckBox("Confirmation before Edit");
        caseSensetiveWorkspaceBox = new JCheckBox("Case Sensetive workspaces");
        printOutputBox = new JCheckBox("Print output");
        showActionBox = new JCheckBox("Show Action");
        showAnnotations = new JCheckBox("Show Annotations (Labels)");
        invalidateOnRefreshBox = new JCheckBox("Invalidate cache on Refresh");

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder("Prefernces"));
        box.add(interceptAddBox);
        box.add(confirmEditBox);
        box.add(caseSensetiveWorkspaceBox);
        box.add(printOutputBox);
        box.add(showActionBox);
        box.add(showAnnotations);

        colorsPanel = new ColorsPanel();
        colorsPanel.setBorder(BorderFactory.createTitledBorder("Colors"));

        // wrapping into panel because of different Box printing
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.add(box, BorderLayout.NORTH);
        panel.add(colorsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JComponent createListButtonsPanel() {
        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addConnectionAction();
            }

        });
        JButton dupeButton = new JButton("Duplicate");
        dupeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dupeConnectionAction();
            }

        });
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                removeConnectionAction();
            }

        });
        JButton upButton = new JButton("Up");
        upButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                upConnectionAction();
            }

        });
        JButton downButton = new JButton("Down");
        downButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                downConnectionAction();
            }

        });

        JPanel gridPanel = new JPanel(new GridLayout(5, 1, 0, 6));
        gridPanel.add(addButton);
        gridPanel.add(dupeButton);
        gridPanel.add(removeButton);
        gridPanel.add(upButton);
        gridPanel.add(downButton);
        // TODO check if it works:
        //gridPanel.setMaximumSize(gridPanel.getSize());

        Box box = Box.createVerticalBox();
        box.add(gridPanel);
        box.add(Box.createVerticalGlue());
        return box;
    }

    private int selectedRow = -1;

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        // saving modifications to previous selected connection:
        if (selectedRow != -1 && !moved) {
            Connection conn = connections.get(selectedRow);
            connectionPanelToConnection(conn);
        }
        selectedRow = connectionsList.getSelectedIndex();
        if (selectedRow == -1) {
            return;
        }
        Connection conn = connections.get(selectedRow);
        connectionPanel.passwordField.setText(conn.getPassword());
        connectionPanel.clientField.setText(conn.getClient());
        connectionPanel.serverField.setText(conn.getServer());
        connectionPanel.userField.setText(conn.getUser());
        connectionPanel.workspaceField.setText(conn.getWorkspacePath());
        moved = false;
    }

    private void addConnectionAction() {
        Connection c = new Connection();
        c.setWorkspacePath(System.getProperty("user.home"));
        int index = connections.size();
        connections.add(c);
        listModel.fireAdded(index);
        connectionsList.setSelectedIndex(index);
    }

    private void dupeConnectionAction() {
        int index = connectionsList.getSelectedIndex();
        if (index >= 0) {
            Connection oldC = connections.get(index);

            Connection c = new Connection();
            c.setWorkspacePath(oldC.getWorkspacePath());
            c.setServer(oldC.getServer());
            c.setClient(oldC.getClient());
            c.setUser(oldC.getUser());
            c.setPassword(oldC.getPassword());

            int nIndex = connections.size();
            connections.add(c);
            listModel.fireAdded(nIndex);
            connectionsList.setSelectedIndex(nIndex);
        }
    }

    private void removeConnectionAction() {
        int index = connectionsList.getSelectedIndex();
        if (index >= 0) {
            connectionsList.setSelectedIndex(index - 1);
            connections.remove(index);
            listModel.fireRemoved(index);
        }
    }

    private void upConnectionAction() {
        if (connections.size() < 2) {
            return;
        }
        int selected = connectionsList.getSelectedIndex();
        if (selected == 0) {
            return;
        }
        Connection c = connections.remove(selected);
        int newIndex = selected - 1;
        connections.add(newIndex, c);
        moved = true;
        connectionsList.setSelectedIndex(newIndex);
        listModel.fireChanged(newIndex, selected);
    }

    private void downConnectionAction() {
        if (connections.size() < 2) {
            return;
        }
        int selected = connectionsList.getSelectedIndex();
        int newIndex = selected + 1;
        if (newIndex == connections.size()) {
            return;
        }
        Connection c = connections.remove(selected);
        connections.add(newIndex, c);
        moved = true;
        connectionsList.setSelectedIndex(newIndex);
        listModel.fireChanged(selected, newIndex);
    }

    private void fireWorkspaceChanged() {
        int index = connectionsList.getSelectedIndex();
        if (index >= 0) {
            connectionPanelToConnection(connections.get(index));
            listModel.fireChanged(index, index);
        }
    }

    public void insertUpdate(DocumentEvent e) {
        fireWorkspaceChanged();
    }

    public void removeUpdate(DocumentEvent e) {
        fireWorkspaceChanged();
    }

    public void changedUpdate(DocumentEvent e) {
        fireWorkspaceChanged();
    }

    private void changeButtonColor(String colorString, JButton button) {
        button.setBackground(Color.decode('#' + colorString));
        button.setText(colorString);
    }

    // <editor-fold defaultstate="collapsed" desc=" OptionsPanelController ">
    @Override
    public void update() {
        connections = PerforceVersioningSystem.getInstance().getConnections();
        preferences = PerforceVersioningSystem.getInstance().getPerforcePreferences();
        interceptAddBox.setSelected(preferences.isInterceptAdd());
        confirmEditBox.setSelected(preferences.isConfirmEdit());
        caseSensetiveWorkspaceBox.setSelected(preferences.isCaseSensetiveWorkspaces());
        printOutputBox.setSelected(preferences.isPrintOutput());
        showActionBox.setSelected(preferences.isShowAction());
        showAnnotations.setSelected(VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false));
        invalidateOnRefreshBox.setSelected(preferences.isInvalidateOnRefresh());
        if (connections.size() > 0) {
            connectionsList.setSelectedIndex(0);
        }

        // setting colors:
        changeButtonColor(preferences.getColorAdd(), colorsPanel.colorAddButton);
        changeButtonColor(preferences.getColorBase(), colorsPanel.colorBaseButton);
        changeButtonColor(preferences.getColorDelete(), colorsPanel.colorDeleteButton);
        changeButtonColor(preferences.getColorEdit(), colorsPanel.colorEditButton);
        changeButtonColor(preferences.getColorLocal(), colorsPanel.colorLocalButton);
        changeButtonColor(preferences.getColorOutdated(), colorsPanel.colorOutdatedButton);
        changeButtonColor(preferences.getColorUnknown(), colorsPanel.colorUnknownButton);
    }

    @Override
    public void applyChanges() {
        preferences.setCaseSensetiveWorkspaces(caseSensetiveWorkspaceBox.isSelected());
        preferences.setInterceptAdd(interceptAddBox.isSelected());
        preferences.setConfirmEdit(confirmEditBox.isSelected());
        preferences.setPrintOutput(printOutputBox.isSelected());
        preferences.setShowAction(showActionBox.isSelected());
        preferences.setInvalidateOnRefresh(invalidateOnRefreshBox.isSelected());

        int index = connectionsList.getSelectedIndex();
        if (index >= 0) {
            connectionPanelToConnection(connections.get(index));
        }

        preferences.setColorAdd(colorsPanel.colorAddButton.getText());
        preferences.setColorBase(colorsPanel.colorBaseButton.getText());
        preferences.setColorDelete(colorsPanel.colorDeleteButton.getText());
        preferences.setColorEdit(colorsPanel.colorEditButton.getText());
        preferences.setColorLocal(colorsPanel.colorLocalButton.getText());
        preferences.setColorOutdated(colorsPanel.colorOutdatedButton.getText());
        preferences.setColorUnknown(colorsPanel.colorUnknownButton.getText());

        PerforceVersioningSystem.getInstance().setConnections(connections);
        PerforceVersioningSystem.getInstance().setPerforcePreferences(preferences);

        // global IDE option:
        VersioningSupport.getPreferences().putBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, showAnnotations.isSelected());
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isChanged() {
        // TODO implement it better in future
        return true;
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return perforceOptionsPanel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    private void connectionPanelToConnection(Connection c) {
        c.setClient(connectionPanel.clientField.getText());
        c.setServer(connectionPanel.serverField.getText());
        c.setUser(connectionPanel.userField.getText());
        c.setWorkspacePath(connectionPanel.workspaceField.getText());
        c.setPassword(String.valueOf(connectionPanel.passwordField.getPassword()));
    }

    // </editor-fold>
    private class ConnectionsListModel extends AbstractListModel {

        public int getSize() {
            if (connections == null) {
                return 0;
            }
            return connections.size();
        }

        public Object getElementAt(int index) {
            if (connections == null) {
                return " ";
            }
            return connections.get(index).getWorkspacePath();
        }

        public void fireRemoved(int index) {
            fireIntervalRemoved(this, index, index);
        }

        public void fireAdded(int index) {
            fireIntervalAdded(this, index, index);
        }

        public void fireChanged(int a, int b) {
            fireContentsChanged(this, a, b);
        }

    }
}
