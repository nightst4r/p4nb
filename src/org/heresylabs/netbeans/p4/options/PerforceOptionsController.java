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
    private JCheckBox interceptEditBox;
    private JCheckBox interceptDeleteBox;
    private JCheckBox interceptAddBox;
    private JCheckBox confirmEditBox;
    private JCheckBox caseSensetiveWorkspaceBox;
    private JCheckBox printOutputBox;
    private ConnectionPanel connectionPanel;
    private JList connectionsList;
    private ConnectionsListModel listModel;
    private List<Connection> connections;
    private PerforcePreferences preferences;

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
        interceptDeleteBox = new JCheckBox("Intercept Delete operation");
        interceptEditBox = new JCheckBox("Intercept Edit operation");
        confirmEditBox = new JCheckBox("Confirmation before Edit");
        caseSensetiveWorkspaceBox = new JCheckBox("Case Sensetive workspaces");
        printOutputBox = new JCheckBox("Print output");

        Box box = Box.createVerticalBox();
        box.add(interceptEditBox);
        box.add(interceptAddBox);
        box.add(interceptDeleteBox);
        box.add(confirmEditBox);
        box.add(caseSensetiveWorkspaceBox);
        box.add(printOutputBox);

        // wrapping into panel because of different Box printing
        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.add(box);

        return panel;
    }

    private JComponent createListButtonsPanel() {
        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addConnectionAction();
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

        JPanel gridPanel = new JPanel(new GridLayout(4, 1, 0, 6));
        gridPanel.add(addButton);
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
        // saving modifications to previous selected connection:
        if (selectedRow != -1) {
            Connection conn = connections.get(selectedRow);
            connectionPanelToConnection(conn);
        }
        selectedRow = connectionsList.getSelectedIndex();
        if (selectedRow == -1) {
            return;
        }
        Connection conn = connections.get(selectedRow);
        connectionPanel.clientField.setText(conn.getClient());
        connectionPanel.serverField.setText(conn.getServer());
        connectionPanel.userField.setText(conn.getUser());
        connectionPanel.workspaceField.setText(conn.getWorkspacePath());
        connectionPanel.passwordField.setText(conn.getPassword());
    }

    private void addConnectionAction() {
        Connection c = new Connection();
        c.setWorkspacePath(System.getProperty("user.home"));
        int index = connections.size();
        connections.add(c);
        listModel.fireAdded(index);
        connectionsList.setSelectedIndex(index);
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
        // TODO implement
        return;
    }

    private void downConnectionAction() {
        // TODO impelement
        return;
    }

    private void fireWorkspaceChanged() {
        int index = connectionsList.getSelectedIndex();
        listModel.fireChanged(index, index);
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

    // <editor-fold defaultstate="collapsed" desc=" OptionsPanelController ">
    @Override
    public void update() {
        connections = PerforceVersioningSystem.getInstance().getConnections();
        preferences = PerforceVersioningSystem.getInstance().getPerforcePreferences();
        interceptAddBox.setSelected(preferences.isInterceptAdd());
        interceptDeleteBox.setSelected(preferences.isInterceptDelete());
        interceptEditBox.setSelected(preferences.isInterceptEdit());
        confirmEditBox.setSelected(preferences.isConfirmEdit());
        caseSensetiveWorkspaceBox.setSelected(preferences.isCaseSensetiveWorkspaces());
        printOutputBox.setSelected(preferences.isPrintOutput());
        if (connections.size() > 0) {
            connectionsList.setSelectedIndex(0);
        }
    }

    @Override
    public void applyChanges() {
        preferences.setCaseSensetiveWorkspaces(caseSensetiveWorkspaceBox.isSelected());
        preferences.setInterceptAdd(interceptAddBox.isSelected());
        preferences.setInterceptDelete(interceptDeleteBox.isSelected());
        preferences.setInterceptEdit(interceptEditBox.isSelected());
        preferences.setConfirmEdit(confirmEditBox.isSelected());
        preferences.setPrintOutput(printOutputBox.isSelected());
        int index = connectionsList.getSelectedIndex();
        if (index >= 0) {
            connectionPanelToConnection(connections.get(index));
        }
        PerforceVersioningSystem.getInstance().setConnections(connections);
        PerforceVersioningSystem.getInstance().setPerforcePreferences(preferences);
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
