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

/**
 * Bean to store preferences.
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class PerforcePreferences {

    // <editor-fold defaultstate="collapsed" desc=" constructors ">
    public PerforcePreferences() {
    }

    public PerforcePreferences(boolean interceptEdit, boolean interceptDelete, boolean interceptAdd, boolean confirmEdit, boolean caseSensetiveWorkspaces, boolean printOutput) {
        this.interceptEdit = interceptEdit;
        this.interceptDelete = interceptDelete;
        this.interceptAdd = interceptAdd;
        this.confirmEdit = confirmEdit;
        this.caseSensetiveWorkspaces = caseSensetiveWorkspaces;
        this.printOutput = printOutput;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" interceptors ">
    private boolean interceptEdit;
    private boolean interceptDelete;
    private boolean interceptAdd;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" settings ">
    private boolean confirmEdit;
    private boolean caseSensetiveWorkspaces;
    private boolean printOutput;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" getters/setters ">
    public boolean isCaseSensetiveWorkspaces() {
        return caseSensetiveWorkspaces;
    }

    public void setCaseSensetiveWorkspaces(boolean caseSensetiveWorkspaces) {
        this.caseSensetiveWorkspaces = caseSensetiveWorkspaces;
    }

    public boolean isPrintOutput() {
        return printOutput;
    }

    public void setPrintOutput(boolean printOutput) {
        this.printOutput = printOutput;
    }

    public boolean isInterceptAdd() {
        return interceptAdd;
    }

    public void setInterceptAdd(boolean interceptAdd) {
        this.interceptAdd = interceptAdd;
    }

    public boolean isInterceptDelete() {
        return interceptDelete;
    }

    public void setInterceptDelete(boolean interceptDelete) {
        this.interceptDelete = interceptDelete;
    }

    public boolean isInterceptEdit() {
        return interceptEdit;
    }

    public void setInterceptEdit(boolean interceptEdit) {
        this.interceptEdit = interceptEdit;
    }

    public boolean isConfirmEdit() {
        return confirmEdit;
    }

    public void setConfirmEdit(boolean confirmEdit) {
        this.confirmEdit = confirmEdit;
    }

    // </editor-fold>
}
