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
 *
 * @author Aekold Helbrass <Helbrass@gmail.com>
 */
public class Proc {

    private int exitValue;
    private String output;
    private String errors;

    public Proc() {
    }

    public Proc(int exitValue, String output, String errors) {
        this.exitValue = exitValue;
        this.output = output;
        this.errors = errors;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public int getExitValue() {
        return exitValue;
    }

    public void setExitValue(int exitValue) {
        this.exitValue = exitValue;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

}
