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

    public PerforcePreferences(boolean caseSensetiveWorkspaces, boolean confirmEdit, boolean interceptAdd, boolean printOutput, boolean showAction, String colorBase, String colorLocal, String colorUnknown, String colorAdd, String colorDelete, String colorEdit, String colorOutdated) {
        this.caseSensetiveWorkspaces = caseSensetiveWorkspaces;
        this.confirmEdit = confirmEdit;
        this.interceptAdd = interceptAdd;
        this.printOutput = printOutput;
        this.showAction = showAction;
        this.colorBase = colorBase;
        this.colorLocal = colorLocal;
        this.colorUnknown = colorUnknown;
        this.colorAdd = colorAdd;
        this.colorDelete = colorDelete;
        this.colorEdit = colorEdit;
        this.colorOutdated = colorOutdated;
    }
    // </editor-fold>

    private boolean caseSensetiveWorkspaces;
    private boolean confirmEdit;
    private boolean interceptAdd;
    private boolean printOutput;
    private boolean showAction;
    private boolean invalidateOnRefresh;
    private String colorBase = "000000";
    private String colorLocal = "999999";
    private String colorUnknown = "444444";
    private String colorAdd = "008000";
    private String colorDelete = "FF0000";
    private String colorEdit = "0000FF";
    private String colorOutdated = "999900";

    // <editor-fold defaultstate="collapsed" desc=" getters/setters ">
    public boolean isCaseSensetiveWorkspaces() {
        return caseSensetiveWorkspaces;
    }

    public void setCaseSensetiveWorkspaces(boolean caseSensetiveWorkspaces) {
        this.caseSensetiveWorkspaces = caseSensetiveWorkspaces;
    }

    public boolean isConfirmEdit() {
        return confirmEdit;
    }

    public void setConfirmEdit(boolean confirmEdit) {
        this.confirmEdit = confirmEdit;
    }

    public boolean isInterceptAdd() {
        return interceptAdd;
    }

    public void setInterceptAdd(boolean interceptAdd) {
        this.interceptAdd = interceptAdd;
    }

    public boolean isPrintOutput() {
        return printOutput;
    }

    public void setPrintOutput(boolean printOutput) {
        this.printOutput = printOutput;
    }

    public boolean isShowAction() {
        return showAction;
    }

    public void setShowAction(boolean showAction) {
        this.showAction = showAction;
    }

    public String getColorAdd() {
        return colorAdd;
    }

    public void setColorAdd(String colorAdd) {
        this.colorAdd = colorAdd;
    }

    public String getColorBase() {
        return colorBase;
    }

    public void setColorBase(String colorBase) {
        this.colorBase = colorBase;
    }

    public String getColorDelete() {
        return colorDelete;
    }

    public void setColorDelete(String colorDelete) {
        this.colorDelete = colorDelete;
    }

    public String getColorEdit() {
        return colorEdit;
    }

    public void setColorEdit(String colorEdit) {
        this.colorEdit = colorEdit;
    }

    public String getColorLocal() {
        return colorLocal;
    }

    public void setColorLocal(String colorLocal) {
        this.colorLocal = colorLocal;
    }

    public String getColorOutdated() {
        return colorOutdated;
    }

    public void setColorOutdated(String colorOutdated) {
        this.colorOutdated = colorOutdated;
    }

    public String getColorUnknown() {
        return colorUnknown;
    }

    public void setColorUnknown(String colorUnknown) {
        this.colorUnknown = colorUnknown;
    }

    public boolean isInvalidateOnRefresh() {
        return invalidateOnRefresh;
    }

    public void setInvalidateOnRefresh(boolean invalidateOnRefresh) {
        this.invalidateOnRefresh = invalidateOnRefresh;
    }

    // </editor-fold>
}
